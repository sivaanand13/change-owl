# ChangeOwl 🦉
> **The System Design Reasoning Engine**

ChangeOwl is a multi-module, event-driven platform for turning engineering activity into structured technical intelligence. The current v1 pipeline ingests GitHub pull requests and discussions, normalizes them into canonical events, streams them through Kafka, and persists them for downstream enrichment and analysis.

## What’s in this repo

### Microservices & Module Breakdown

- **`github-ingestion-service`**
  - Spring Boot service that periodically pulls GitHub PRs and discussions.
  - Maps GitHub API responses into the shared `ArtifactEvent` model.
  - Publishes events to Kafka topic `technical-artifacts`.
  - Exposes basic Micrometer/Prometheus metrics and health endpoints.

- **`storage-service`**
  - Kafka consumer that receives `ArtifactEvent` messages.
  - Persists artifacts to Postgres via Spring Data JPA.
  - Stores raw artifact payload and repository/technology linkage data.
  - Republishes a canonical event to `technical-artifacts-canonical` after successful persistence.

- **`changeowl-shared`**
  - Shared contract module used by both services.
  - Contains common event interfaces, canonical event models, and Kafka topic names.

- **`semantic-enrichment-service`**
	- FastAPI + FastStream worker that consumes canonical artifacts from Kafka topic `technical-artifacts-canonical`.
	- Fetches the source artifact from Postgres, generates a technical summary with the Qwen instruct model, and creates an embedding with the Nomic text model.
	- Persists enriched results back to Postgres and publishes `ArtifactEnrichedEvent` messages to Kafka topic `technical-artifacts-enriched`.
	- Sends failed messages to the DLQ topic `technical-artifacts-canonical_dlq`.

### Infrastructure

- **Kafka** in KRaft mode for event streaming
- **Kafdrop** for topic and consumer inspection
- **Postgres** with **pgvector** support for storage and future vector/semantic workflows
- **Python semantic worker** for model-driven enrichment over canonical artifacts
- **Docker Compose** for local infra bootstrapping

## High-level architecture

```mermaid
flowchart LR
	GH[GitHub API\nPRs + Discussions] --> ING[github-ingestion-service]
	ING -->|ArtifactEvent| K[(Kafka\ntechnical-artifacts)]
	K --> ST[storage-service]
	ST --> DB[(Postgres)]
	ST -->|CanonicalArtifactEvent| KC[(Kafka\ntechnical-artifacts-canonical)]
	ST --> METRICS[Actuator + Micrometer]
	ING --> METRICS
	K --> KD[Kafdrop]
	KC --> SEM[semantic-enrichment-service]
	SEM -->|ArtifactEnrichedEvent| KE[(Kafka\ntechnical-artifacts-enriched)]
	SEM --> DB
```

## Event flow

```mermaid
sequenceDiagram
	participant GitHub as GitHub API
	participant Ingestion as github-ingestion-service
	participant Kafka as Kafka
	participant Storage as storage-service
	participant Semantic as semantic-enrichment-service
	participant Postgres as Postgres

	GitHub->>Ingestion: Fetch PRs / discussions
	Ingestion->>Ingestion: Map API DTOs to ArtifactEvent
	Ingestion->>Kafka: Publish technical-artifacts
	Kafka->>Storage: Deliver ArtifactEvent
	Storage->>Postgres: Persist artifact + payload + repo metadata
	Storage->>Kafka: Publish technical-artifacts-canonical
	Kafka->>Semantic: Deliver CanonicalArtifactEvent
	Semantic->>Postgres: Load artifact + persist enriched result
	Semantic->>Kafka: Publish technical-artifacts-enriched
```

## Current implementation status

### Phase 1: Data foundation and ingestion pipeline

What is already implemented:

- Maven multi-module setup with a shared parent POM
- Shared event and topic definitions in `changeowl-shared`
- GitHub ingestion client for PRs and discussions
- DTO-to-domain mapping layer for canonical artifact events
- Kafka producer for the ingestion service
- Kafka consumer, persistence layer, and canonical republishing in storage
- Basic observability with logs, timers, counters, and actuator endpoints
- Semantic enrichment worker with summarization, embeddings, persistence, and DLQ handling
- Local infrastructure for Kafka, Kafdrop, and Postgres/pgvector

### Active work

- Building the Next.js front-end for reviewing, exploring, and operationalizing the canonical and enriched artifact streams
- Following that, adding the RAG layer for retrieval-backed reasoning, synthesis, and guided analysis over repository knowledge

## Tech stack

- **Language:** Java 21
- **Build tool:** Maven multi-module
- **Framework:** Spring Boot 3.x, FastAPI, FastStream
- **Messaging:** Apache Kafka
- **Persistence:** PostgreSQL + Spring Data JPA (Hibernate)
- **Semantic models:** SentenceTransformers, Transformers, Torch
- **Observability:** Spring Boot Actuator, Micrometer
- **Serialization:** Jackson, Spring Kafka JSON serializers/deserializers
- **Utilities:** Lombok
- **Local infra:** Docker Compose, Kafdrop, pgvector

## Repository layout

```text
changeowl/
├── changeowl-shared/
├── services/
│   ├── github-ingestion-service/
│   └── storage-service/
├── infra/
└── docs/
```

## Data sources

- GitHub pull requests
- GitHub discussions

## Current delivery focus

The repo is currently in a stable state for end-to-end ingestion, storage, and semantic enrichment. The next milestone of v1 is the Next.js front-end, followed by the RAG layer for retrieval-backed reasoning on top of the canonical and enriched artifact streams.
