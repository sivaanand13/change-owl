from contextlib import asynccontextmanager
from fastapi import FastAPI, Header, HTTPException, Depends, Body
from app.logger import log
from app.services.database import pool
import asyncio
from app.worker import worker
from app.services.embedder import embedder
from app.config import settings
import re

@asynccontextmanager
async def lifespan(app: FastAPI):
    log.info("service_startup_initiated")
    await pool.open()

    kafka_consumer = asyncio.create_task(worker.start())

    yield

    log.info("service_shutdown_initiated")
    await worker.stop()
    kafka_consumer.cancel()
    pool.close()


app = FastAPI(lifespan=lifespan)

async def validate_api_key(x_api_key: str = Header(...)):
    if x_api_key != settings.INTERNAL_API_KEY:
        raise HTTPException(status_code=403, detail="Unauthorized")

def clean_query(text: str) -> str:
    text = " ".join(text.split())
    text = re.sub(r'[\x00-\x1f\x7f-\x9f]', '', text)
    return text.strip()[:500]

@app.post("/embed", dependencies=[Depends(validate_api_key)])
async def get_query_vector(payload: dict = Body(...)):
    query_text = payload.get("query")
    if not query_text or not isinstance(query_text, str):
        raise HTTPException(status_code=400, detail="Query text required")
    clean_text = clean_query(query_text)

    if len(clean_text) < 3:
        raise HTTPException(status_code=400, detail="Search query is too short")
    
    vector = embedder.generate_query_embedding(clean_text)
    return {"embedding": vector}