from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.logger import log
from app.services.database import pool
import asyncio
from app.worker import worker

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
