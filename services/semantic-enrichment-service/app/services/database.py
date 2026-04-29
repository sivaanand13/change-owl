from psycopg_pool import AsyncConnectionPool
from app.config import settings
from contextlib import asynccontextmanager
from app.model.artifact import Artifact
from psycopg.rows import dict_row
from app.model.artifact_summary import ArtifactSummary
import json

pool = AsyncConnectionPool(
    conninfo=settings.DATABASE_URL,
    min_size=1,
    max_size=5,
    open=False,
    kwargs={"row_factory": dict_row}
)

@asynccontextmanager
async def get_db():
    async with pool.connection() as conn:
        yield conn

async def fetch_artifact(artifact_id) -> Artifact:
    query = """
        SELECT
            a.id,
            a.repo_id,
            a.tech_id,
            a.title,
            a.body,
            a.type,
            a.source,
            a.author,
            a.state,
            t.name as tech_name,
            concat(r."owner" , '/', r."name" ) as repo_name,
            a.url
        FROM artifacts a
        JOIN technologies t ON a.tech_id = t.id
        JOIN tracked_repositories r ON a.repo_id = r.id
        WHERE a.id=%s
    """
    async with pool.connection() as conn:
        async with conn.cursor() as cur:
            await cur.execute(query, (artifact_id,))
            row = await cur.fetchone()
            
            if row:
                return Artifact(**row)
                    
    return None

async def persist_enriched_artifact(
    artifact_id: int,
    ai_summary: ArtifactSummary,
    embedding: list[float],
    model_version: str,
    processing_status: str = "COMPLETED",
):
    query = """
        INSERT INTO artifact_intelligence (
            artifact_id,
            change_type,
            risk_level,
            confidence,
            summarizer_model,
            embedding_model,
            ai_summary,
            embedding,
            processing_status
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (artifact_id) DO UPDATE SET
            change_type = EXCLUDED.change_type,
            risk_level = EXCLUDED.risk_level,
            confidence = EXCLUDED.confidence,
            summarizer_model = EXCLUDED.summarizer_model,
            embedding_model = EXCLUDED.embedding_model,
            ai_summary = EXCLUDED.ai_summary,
            embedding = EXCLUDED.embedding,
            processing_status = EXCLUDED.processing_status,
            enriched_at = NOW()
    """
    ai_summary_json = ai_summary.model_dump_json()
    async with pool.connection() as conn:
        async with conn.cursor() as cur:
            await cur.execute(
                query,
                (
                    artifact_id,
                    ai_summary.change_type,
                    ai_summary.risk_level,
                    ai_summary.confidence,
                    settings.SUMMARIZER_MODEL_NAME,
                    settings.EMBEDDING_MODEL_NAME,
                    ai_summary_json,
                    embedding,
                    processing_status,
                ),
            )
        await conn.commit()