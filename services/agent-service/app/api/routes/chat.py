import uuid
import structlog
from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from app.api.schemas.chat_request import ChatRequest
from app.services.chat_service import ChatService
from app.config import settings

router = APIRouter()
chat_service = ChatService()
log = structlog.get_logger()


@router.post("/chat")
async def chat(req: ChatRequest):
    session_id = req.session_id
    
    log.info("api.endpoint.chat.invoked", session_id=session_id)
    
    return await chat_service.chat(
        question=req.question,
        session_id=session_id
    )


@router.post("/chat/stream")
async def chat_stream(req: ChatRequest):

    context = structlog.contextvars.get_contextvars()
    session_id = context.get("session_id")

    
    log.info("api.endpoint.stream.invoked", session_id=session_id)

    return StreamingResponse(
        chat_service.stream(
            question=req.question, 
            session_id=session_id
        ),
        media_type="text/event-stream",
        headers={
            "X-Session-ID": session_id,
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
        }
    )