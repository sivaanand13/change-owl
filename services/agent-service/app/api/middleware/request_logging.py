import time
import uuid
from fastapi import Request, Response
from starlette.middleware.base import BaseHTTPMiddleware
import structlog
from app.logger import log

class StructuredLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next) -> Response:

        structlog.contextvars.clear_contextvars()

        session_id = (
            request.headers.get("X-Session-ID") 
            or request.query_params.get("session_id")
        )
        
        if not session_id:
            session_id = f"sess-{uuid.uuid4()}"

        structlog.contextvars.bind_contextvars(
            session_id=session_id,
            method=request.method,
            path=request.url.path,
            client_ip=request.client.host if request.client else "unknown"
        )

        start_time = time.perf_counter()
        
        try:
            response = await call_next(request)
            
            duration_ms = round((time.perf_counter() - start_time) * 1000, 2)
            
            log.info(
                "http.request.completed",
                status_code=response.status_code,
                duration_ms=duration_ms
            )
            
            response.headers["X-Session-ID"] = session_id
            return response
            
        except Exception as e:
            duration_ms = round((time.perf_counter() - start_time) * 1000, 2)
            
            log.exception(
                "http.request.failed",
                error_type=e.__class__.__name__,
                duration_ms=duration_ms
            )
            raise e