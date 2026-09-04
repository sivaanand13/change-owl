from langgraph.graph import StateGraph, END
from langgraph.prebuilt import ToolNode
from langchain_core.messages import AIMessage
from langchain_core.messages import HumanMessage
from langgraph.checkpoint.memory import MemorySaver

from app.agents.search_agent.state import SearchState
from app.llm.factory import get_llm
from app.tools.artifacts import artifact_tools
from app.tools.repositories import repository_tools

_memory_singleton = MemorySaver()

tools = [
    tool.tool
    for tool in (artifact_tools + repository_tools)
]

llm = get_llm().bind_tools(tools)

tool_node = ToolNode(tools)



async def agent_node(state: SearchState):
    messages = state.get("messages", [])

    if not messages:
        messages = [HumanMessage(content="")]

    response = await llm.ainvoke(messages)

    return {"messages": messages + [response]}



AGENT = "agent"
TOOLS = "tools"

def should_call_tools(state: SearchState):
    last = state["messages"][-1]

    if isinstance(last, AIMessage) and last.tool_calls:
        return TOOLS

    return END


def build_graph():
    graph = StateGraph(SearchState)

    graph.add_node(AGENT, agent_node)
    graph.add_node(TOOLS, tool_node)

    graph.set_entry_point(AGENT)

    graph.add_conditional_edges(
        AGENT,
        should_call_tools,
        {
            TOOLS: TOOLS,
            END: END,
        },
    )

    graph.add_edge(TOOLS, AGENT)
    return graph.compile(checkpointer=_memory_singleton)