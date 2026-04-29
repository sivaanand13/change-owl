import asyncio
import os
from app.logger import log
from app.services.database import fetch_artifact
from app.services.summarizer import summarizer
from app.config import settings
from app.services.database import pool
import json

async def process_batch(start_id: int, end_id: int, batch_size: int = 5):
    os.makedirs("./outputs", exist_ok=True)
    output_file = "./outputs/qwen_summaries.txt"
    semaphore = asyncio.Semaphore(batch_size)

    async def bounded_process(artifact_id: int):
        async with semaphore:
            context_log = log.bind(artifact_id=artifact_id)
            try:
                artifact = await fetch_artifact(artifact_id=artifact_id)
                if not artifact:
                    return
                context_log.info(
                    "fetched",
                    artifact_id=artifact_id,
                    title=artifact.title,
                    type=artifact.type,
                )
                summary = await asyncio.to_thread(summarizer.summarize, artifact)
                narrative = artifact.to_narrative()
                content = f"""
=== ARTIFACT ID: {artifact_id} ===
TITLE: {artifact.title}
TYPE: {artifact.type}

--- SUMMARY ---
{json.dumps(summary.model_dump(), indent=2)}

--- ORIGINAL (TRUNCATED) ---
{narrative[:2000]}

{"="*80}
"""

                def write():
                    with open(output_file, "a", encoding="utf-8") as f:
                        f.write(content)

                await asyncio.to_thread(write)

                context_log.info("processed")

            except Exception as e:
                context_log.error("failed", error=str(e), exc_info=True)

    tasks = [bounded_process(i) for i in range(start_id, end_id + 1)]
    await asyncio.gather(*tasks)

if __name__ == "__main__":
    async def main():
        await pool.open()
        await process_batch(1, 15, batch_size=settings.BATCH_SIZE if hasattr(settings, "BATCH_SIZE") else 5)
        await pool.close()
    asyncio.run(main())