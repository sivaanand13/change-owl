from fastapi import FastAPI

from app.api.routes.chat import router

app = FastAPI()

app.include_router(
    router,
    prefix="/api",
    tags=["api"]
)