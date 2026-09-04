import json
import structlog
from langchain_core.messages import HumanMessage

from app.agents.search_agent.graph import build_graph
from app.models.agent_event import AgentEvent
from app.tools.registry import tool_registry

log = structlog.get_logger()


class ChatService:
    def __init__(self):
        self.graph = build_graph()
    
    async def chat(
            self,
            question: str,
            session_id: str,
    ) -> AgentEvent:
        structlog.contextvars.bind_contextvars(session_id=session_id)
        log.info("agent.chat.start", question_length=len(question))

        try:
            result = await self.graph.ainvoke(
                {
                    "messages": [
                        HumanMessage(content=question)
                    ]
                }
            )

            final_message = result["messages"][-1]
            content = self._extract_content(final_message.content)
            
            log.info("agent.chat.complete")
            return AgentEvent(
                type="complete",
                content=content
            )
        except Exception as e:
            log.exception("agent.chat.failed", error_type=e.__class__.__name__)
            return AgentEvent(
                type="error",
                message="An error occurred while processing your request."
            )

    async def stream(
            self,
            question: str,
            session_id: str,
    ):
        structlog.contextvars.bind_contextvars(session_id=session_id)
        log.info("agent.stream.start", question_length=len(question))

        final_answer = ""

        try:
            async for event in self.graph.astream_events(
                {
                    "messages": [
                        HumanMessage(content=question)
                    ]
                },
                version="v2",
            ):
                event_type = event["event"]

                if event_type == "on_tool_start":
                    tool_name = event["name"]
                    tool_input = event["data"].get("input", {})
                    log.info("agent.tool.start", tool_name=tool_name, tool_input=tool_input)

                    definition = tool_registry.get(tool_name)
                    yield self._sse(
                        AgentEvent(
                            type="tool_start",
                            tool_name=tool_name,
                            message=(
                                definition.start_message
                                if definition
                                else f"Running {tool_name.replace('_', ' ')}..."
                            )
                        )
                    )
                
                elif event_type == "on_tool_end":
                    tool_name = event["name"]
                    tool_output = event["data"].get("output", {})
                    log.info("agent.tool.end", tool_name=tool_name, tool_output=str(tool_output)[:200])

                    definition = tool_registry.get(tool_name)
                    yield self._sse(
                        AgentEvent(
                            type="tool_end",
                            tool_name=tool_name,
                            message=(
                                definition.end_message
                                if definition
                                else f"Finished {tool_name.replace('_', ' ')}"
                            )
                        )
                    )
                
                elif event_type == "on_chat_model_stream":
                    chunk = event["data"]["chunk"]
                    text = self._extract_content(chunk.content)

                    if not text:
                        continue

                    final_answer += text
                    yield self._sse(
                        AgentEvent(
                            type="token",
                            content=text,
                        )
                    )
            
            log.info("agent.stream.complete", answer_length=len(final_answer))
            yield self._sse(
                AgentEvent(
                    type="complete",
                    content=final_answer,
                )
            )
        except Exception as e:
            log.exception("agent.stream.failed", error_type=e.__class__.__name__)
            yield self._sse(
                AgentEvent(
                    type="error",
                    message="An error occurred during execution."
                )
            )
    
    def _extract_content(
        self,
        content,
    ) -> str:
        if isinstance(content, str):
            return content

        if isinstance(content, list):
            text_parts = []
            for item in content:
                if isinstance(item, dict) and item.get("type") == "text":
                    text_parts.append(item.get("text", ""))
            return "".join(text_parts)

        return ""

    def _sse(
        self,
        event: AgentEvent,
    ) -> str:
        return f"data: {event.model_dump_json()}\n\n"