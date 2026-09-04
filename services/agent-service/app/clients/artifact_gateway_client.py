from typing import Any
from app.models.artifact import Artifact
from app.models.artifact_query import ArtifactQuery
from app.models.artifact_page import ArtifactPage
from app.models.repository import Repository

import httpx
from app.config import settings


class ArtifactGatewayClient:
    def __init__(self) -> None:
        self.base_url = settings.ARTIFACT_GATEWAY_URL.rstrip("/")

    
    async def get_repositories(self) -> list[Repository]:

        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{self.base_url}/api/repositories",
            )

            response.raise_for_status()
            data = response.json()
            return [Repository(**repo) for repo in data]
    
    async def search_artifacts(
        self,
        query: ArtifactQuery,
    ) -> ArtifactPage:

        params = query.model_dump(exclude_none=True)

        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{self.base_url}/api/artifacts",
                params=params,
            )

            response.raise_for_status()
            json = response.json()
            return ArtifactPage.model_validate(json)

    async def get_artifact(
        self,
        artifact_id: int,
    ) -> Artifact:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{self.base_url}/api/artifacts/{artifact_id}"
            )

            response.raise_for_status()

            return Artifact.model_validate(response.json())

    async def find_related_artifacts(
        self,
        artifact_id: int,
        limit: int = 10,
    ) -> list[Artifact]:

        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{self.base_url}/api/artifacts/{artifact_id}/similar",
                params={"limit": limit},
            )

            response.raise_for_status()

            return [
                Artifact.model_validate(item)
                for item in response.json()
            ]

client = ArtifactGatewayClient()