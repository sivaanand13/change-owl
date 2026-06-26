from fastapi import FastAPI

from app.api.routes.chat import router
from app.api.middleware.request_logging import StructuredLoggingMiddleware

app = FastAPI()

app.add_middleware(StructuredLoggingMiddleware)

app.include_router(
    router,
    prefix="/api",
    tags=["api"]
)