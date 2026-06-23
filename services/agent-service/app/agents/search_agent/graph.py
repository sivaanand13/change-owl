from langgraph.graph import StateGraph, START, END
from langchain_core.messages import HumanMessage, SystemMessage

from app.agents.search_agent.state import SearchState
from app.agents.search_agent.prompts import SEARCH_SYSTEM_PROMPT
from app.llm.factory import get_reasoning_model


llm = get_reasoning_model()


def answer_question(state: SearchState):

    response = llm.invoke([
        SystemMessage(content=SEARCH_SYSTEM_PROMPT),
        HumanMessage(content=state["question"])
    ])

    return {
        "answer": response.content
    }
 

builder = StateGraph(SearchState)

builder.add_node("answer", answer_question)

builder.add_edge(START, "answer")
builder.add_edge("answer", END)

graph = builder.compile()