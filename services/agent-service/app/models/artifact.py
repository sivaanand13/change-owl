from pydantic import BaseModel

from app.models.artifact_intelligence import ArtifactIntelligence


class Artifact(BaseModel):
    id: str

    title: str
    body: str
    url: str
    author: str

    createdAt: str
    state: str | None = None

    repoName: str
    repoId: str

    techName: str
    techId: str

    intelligence: ArtifactIntelligence | None = None

    similarityScore: float | None = None