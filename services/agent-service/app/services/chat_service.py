from langchain_core.messages import HumanMessage

from app.agents.search_agent.graph import build_graph
from app.models.agent_event import AgentEvent
from app.tools.registry import tool_registry
from app.logger import log
 
class ChatService:
    def __init__(self):
        self.graph = build_graph()
    
    async def chat(
            self,
            question: str,
            session_id: str | None = None,
    ) -> AgentEvent:
        """
        Non-streaming endpoint.

        Executes the graph to completion and returns
        only the final answer event.
        """

        result = await self.graph.ainvoke(
            {
                "messages": [
                    HumanMessage(content=question)
                ]
            }
        )

        final_message = result["messages"][-1]
        return AgentEvent(
            type="complete",
            content=self._extract_content(
                final_message.content
            )
        )

    async def stream(
            self,
            question: str,
            session_id: str,
    ):
        """
        Streaming endpoint.

        Converts LangGraph events into
        ChangeOwl AgentEvents.
        """

        final_answer = ""

        config = {
        "configurable": {
            "thread_id": session_id
        }
    }

        try:
            async for event in self.graph.astream_events(
                {
                    "messages": [
                        HumanMessage(content=question)
                    ]
                },
                config=config,
                version="v2",
            ):
                event_type = event["event"]

                # Tool event
                if event_type == "on_tool_start":
                    tool_name = event["name"]

                    definition = tool_registry.get(tool_name)

                    yield self._sse(
                        AgentEvent(
                            type="tool_start",
                            tool_name=tool_name,
                            message=(
                                definition.start_message
                                if definition
                                else f"Running {tool_name.replace("_", " ")}..."
                            )
                        )
                    )
                elif event_type == "on_chain_start":
                    tool_name = event["name"]

                    definition = tool_registry.get(tool_name)

                    yield self._sse(
                        AgentEvent(
                            type="start",
                            message="Planning next steps..."
                        )
                    )
                elif event_type == "on_tool_end":
                    tool_name = event["name"]

                    definition = tool_registry.get(tool_name)

                    yield self._sse(
                        AgentEvent(
                            type="tool_end",
                            tool_name=tool_name,
                            message=(
                                definition.end_message
                                if definition
                                else f"Finished {tool_name.replace("_", " ")}"
                            )
                        )
                    )
                elif event_type == "on_chat_model_stream":

                    chunk = event["data"]["chunk"]

                    text = self._extract_content(
                        chunk.content
                    )

                    if not text:
                        continue

                    final_answer += text

                    yield self._sse(
                        AgentEvent(
                            type="token",
                            content=text,
                        )
                    )
            yield self._sse(
                AgentEvent(
                    type="complete",
                    content=final_answer,
                )
            )
        except Exception as e:
            yield self._sse(
                AgentEvent(
                    type="error",
                    message="Oops! Someting went wrong."
                )
            )
            log.exception(
                "agent.pipeline.unhandled_exception",
                session_id=session_id,
                node_name="agent"
            )
    
    def _extract_content(
        self,
        content,
    ) -> str:
        """
        Normalizes provider-specific output formats.
        """

        if isinstance(content, str):
            return content

        if isinstance(content, list):

            text_parts = []

            for item in content:

                if (
                    isinstance(item, dict)
                    and item.get("type") == "text"
                ):
                    text_parts.append(
                        item.get("text", "")
                    )

            return "".join(text_parts)

        return ""

    def _sse(
        self,
        event: AgentEvent,
    ) -> str:
        """
        Converts AgentEvent to SSE format.
        """

        return (
            f"data: "
            f"{event.model_dump_json()}"
            f"\n\n"
        )
