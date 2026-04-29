from app.logger import log
from app.services.database import fetch_artifact, persist_enriched_artifact
from app.services.summarizer import summarizer
from app.services.embedder import embedder
import asyncio
from app.config import settings
from app.model.artifact_enriched import ArtifactEnrichedEvent
from datetime import datetime, timezone

class EnrichmentService:
    async def process_artifact(self, artifact_id: int):
        context_log = log.bind(artifact_id=artifact_id)
        context_log.info("artifact_processing_started")

        artifact = await fetch_artifact(artifact_id=artifact_id)
        if not artifact:
            context_log.warning("enrichment_skipped", reason="artifact_not_found")
            return
        
        context_log.info("artifact_loaded", type=artifact.type)

        summary = await asyncio.to_thread(summarizer.summarize, artifact)
        context_log.info("summary_generated", risk=summary.risk_level)

        vector = await asyncio.to_thread(embedder.generate_embedding, artifact)
        context_log.info("embedding_generated", dims=len(vector))
        status = "COMPLETED"
        await persist_enriched_artifact(
            artifact_id,
            summary,
            vector,
            settings.EMBEDDING_MODEL_NAME,
            status,
        )

        return ArtifactEnrichedEvent(
           artifact_id=artifact_id,
           repo_id=artifact.repo_id,
           repo_name=artifact.repo_name,
           tech_id=artifact.tech_id,
           tech_name=artifact.tech_name,
           change_type=summary.change_type,
           risk_level=summary.risk_level,
           confidence=summary.confidence,
           embedding_model=settings.EMBEDDING_MODEL_NAME,
           summarizer_model=settings.SUMMARIZER_MODEL_NAME,
           status=status,
           createdAt=datetime.now(timezone.utc),
        )

enricher = EnrichmentService()