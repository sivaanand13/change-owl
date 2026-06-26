from pydantic import BaseModel, Field

from app.models.artifact_intelligence import (
    BehavioralImpact,
    ChangeSurface,
    ChangeType,
    Confidence,
    RiskLevel,
)


class ArtifactQuery(BaseModel):

    q: str | None = Field(
        default=None,
        description="""
Natural language semantic search query.

Use for concepts, technologies, problems, features, systems, or architectural topics.

Examples:
- authentication
- redis
- kafka
- caching
- latency
- customer identity
- feature flags
- retries

Combines full-text search with vector similarity search.
"""
    )

    limit: int = Field(
        default=10,
        description="""
Maximum number of artifacts to return.
"""
    )

    offset: int = Field(
        default=0,
        description="""
Pagination offset.
Usually left at 0 unless retrieving additional pages.
"""
    )

    changeType: ChangeType | None = Field(
        default=None,
        description="""
Filter by type of engineering change.

bugfix:
    Corrects incorrect behavior.

feature:
    Introduces new capabilities.

refactor:
    Internal restructuring without changing behavior.

arch_change:
    Architectural or system design changes.

chore:
    Maintenance or dependency work.

Use when the user asks specifically about features, bug fixes,
refactors, architecture changes, or maintenance work.
"""
    )

    surface: ChangeSurface | None = Field(
        default=None,
        description="""
Filter by affected area.

api:
    Public interfaces or endpoints.

runtime:
    Execution behavior and application logic.

dependency:
    Library or framework updates.

internal:
    Internal implementation details.

infra:
    Infrastructure, deployment, or configuration.

Use when the user asks what area of the system was modified.
"""
    )

    risk: RiskLevel | None = Field(
        default=None,
        description="""
Estimated risk level.

low:
    Safe and isolated changes.

medium:
    Moderate impact.

high:
    Changes likely to affect multiple systems or introduce instability.

Use when searching for risky changes or incident investigations.
"""
    )

    confidence: Confidence | None = Field(
        default=None,
        description="""
Confidence level of the AI enrichment.

high:
    Strong evidence from the artifact.

medium:
    Some uncertainty exists.

low:
    Limited information available.

Use when prioritizing highly reliable insights.
"""
    )

    impact: BehavioralImpact | None = Field(
        default=None,
        description="""
Behavioral effect of the change.

none:
    No externally visible behavior changes.

bugfix:
    Corrects incorrect behavior.

performance:
    Improves speed or efficiency.

functional_change:
    Introduces new behavior.

breaking_change:
    Introduces incompatible behavior changes.

Use when users ask about performance improvements,
breaking changes, or behavioral modifications.
"""
    )

    relatedTo: int | None = Field(
        default=None,
        description="""
Artifact ID used for similarity search.

Returns artifacts semantically related to the specified artifact.

Useful for discovering repeated patterns or architectural evolution.
"""
    )

    repoId: int | None = Field(
        default=None,
        description="""
Restrict search to a single repository.

Use when the user asks about a particular repository or service.
"""
    )