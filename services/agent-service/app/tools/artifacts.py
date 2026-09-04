from langchain_core.tools import tool
from typing import Optional

from app.clients.artifact_gateway_client import ArtifactGatewayClient
from app.models.artifact_query import ArtifactQuery
from app.models.artifact import Artifact
from app.clients.artifact_gateway_client import client
from app.models.artifact_intelligence import (
    BehavioralImpact,
    ChangeSurface,
    ChangeType,
    Confidence,
    RiskLevel,
)
from app.tools.definitions import ToolDefinition

@tool
async def search_artifacts(
    q: Optional[str] = None,
    limit: int = 10,
    offset: int = 0,

    changeType: Optional[ChangeType] = None,
    surface: Optional[ChangeSurface] = None,
    risk: Optional[RiskLevel] = None,
    confidence: Optional[Confidence] = None,
    behavioralImpact: Optional[BehavioralImpact] = None,

    relatedTo: Optional[int] = None,
    repoId: Optional[int] = None,
):
    """
    Search engineering artifacts using semantic search and structured filters.

    Prefer semantic search (`q`) for:
    - concepts ("Kafka", "retry logic")
    - incidents ("payment failures")
    - features ("feature flags")

    Prefer metadata filters when the user specifies them:

    changeType:
    - bugfix
    - feature
    - refactor
    - arch_change
    - chore

    surface:
    - api
    - runtime
    - dependency
    - internal
    - infra

    risk:
    - low
    - medium
    - high

    confidence:
    - low
    - medium
    - high

    behavioralImpact:
    - none
    - bugfix
    - performance
    - functional_change
    - breaking_change

    Examples:
    - "Show high risk API changes"
        -> risk="high", surface="api"

    - "Find recent refactors"
        -> changeType="refactor"

    - "Show breaking changes related to Kafka"
        -> q="Kafka", behavioralImpact="breaking_change"

    - "Find infrastructure work"
        -> surface="infra"

    - "Show risky dependency upgrades"
        -> surface="dependency", risk="high"
    """
    return await client.search_artifacts(
        ArtifactQuery(
            q=q,
            limit=limit,
            offset=offset,
            changeType=changeType,
            surface=surface,
            risk=risk,
            confidence=confidence,
            impact=behavioralImpact,
            relatedTo=relatedTo,
            repoId=repoId,
        )
    )

@tool
async def get_artifact(
    artifact_id: int,
) -> Artifact:
    """
    Retrieve one artifact by ID.

    Useful after search when more details are needed.
    """

    return await client.get_artifact(
        artifact_id
    )

@tool
async def find_related_artifacts(
    artifact_id: int,
    limit: int = 5,
):
    """
    Retrieve artifacts most similar to a given artifact using semantic similarity.

    Useful for:
    - finding related design docs
    - discovering similar incidents or bugs
    - identifying recurring patterns
    - exploring the history of a topic across artifacts
    """

    return await client.find_related_artifacts(
        artifact_id,
        limit,
    )

artifact_tools = [
    ToolDefinition(
        tool=search_artifacts,
        start_message="Searching engineering artifacts...",
        end_message="Artifacts retrieved.",
        icon="search",
    ),
    ToolDefinition(
        tool=get_artifact,
        start_message="Reading artifact details...",
        end_message="Artifact loaded.",
        icon="file",
    ),
    ToolDefinition(
        tool=find_related_artifacts,
        start_message="Finding related changes...",
        end_message="Related changes found.",
        icon="network",
    ),
]