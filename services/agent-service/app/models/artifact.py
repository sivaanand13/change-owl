from pydantic import BaseModel

from app.models.artifact_intelligence import ArtifactIntelligence


class Artifact(BaseModel):
    id: int

    title: str
    body: str | None = None
    url: str
    author: str

    createdAt: str
    state: str | None = None

    repoName: str
    repoId: int

    techName: str
    techId: int

    intelligence: ArtifactIntelligence | None = None

    similarityScore: float | None = None