from fastapi import APIRouter

from app.agents.search_agent.graph import graph
from app.api.schemas.chat_request import ChatRequest

router = APIRouter()


@router.post("/chat")
async def chat(req: ChatRequest):
    result = graph.invoke({
        "question": req.question
    })

    return result