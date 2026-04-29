# semantic-enrichment-service

Semantic enrichment worker for ChangeOwl. This service consumes canonical artifact events, turns them into a narrative technical summary, generates a vector embedding, stores the enriched result, and republishes an `ArtifactEnrichedEvent` for downstream consumers.

## What it does

- Consumes messages from Kafka topic `technical-artifacts-canonical`.
- Loads the source artifact from Postgres using the artifact ID in the canonical event.
- Produces a structured summary with `Qwen/Qwen2.5-1.5B-Instruct`.
- Generates embeddings with `nomic-ai/nomic-embed-text-v1.5`.
- Persists enriched output back to Postgres.
- Publishes the enriched event to Kafka topic `technical-artifacts-enriched`.
- Sends failures to the DLQ topic `technical-artifacts-canonical_dlq`.

## Runtime

- Python 3.12
- FastAPI for process lifecycle management
- FastStream Kafka consumer for the worker loop
- Uvicorn on port `8050`

## Configuration

The service reads configuration from `.env`.

Required settings:

- `DATABASE_URL`
- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_TOPIC_CANONICAL`
- `KAFKA_GROUP_ID`
- `KAFKA_DLQ`
- `KAFKA_PRODUCER_TOPIC`
- `EMBEDDING_MODEL_NAME`
- `SUMMARIZER_MODEL_NAME`

## Local run

Run the service from this directory after Postgres and Kafka are available:

```bash
uv run uvicorn app.main:app --host 0.0.0.0 --port 8050
```

## Notes

- The service currently exposes the FastAPI application primarily for lifecycle management.
- `scripts/batch_process.py` can be used to replay artifacts through the summarization path for offline inspection.
