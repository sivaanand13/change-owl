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
async def get_repositories():
    """
    List all engineering repositories available to the current user.

    Call this tool when you need to identify the appropriate repository before
    retrieving artifacts or answering repository-specific questions. Do not
    call it if the target repository is already known.

    Returns:
        list[Repository]: The available repositories with their identifiers,
        names, and associated metadata.
    """
    return await client.get_repositories()


repository_tools = [
    ToolDefinition(
        tool=get_repositories,
        start_message="Retrieving available engineering repositories...",
        end_message="Engineering repositories retrieved.",
        icon="search",
    ),
]