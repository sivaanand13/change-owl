from pydantic import BaseModel

from app.models.artifact_intelligence import (
    BehavioralImpact,
    ChangeSurface,
    ChangeType,
    Confidence,
    RiskLevel,
)


class ArtifactQuery(BaseModel):
    q: str | None = None

    limit: int = 10
    offset: int = 0

    changeType: ChangeType | None = None
    surface: ChangeSurface | None = None

    risk: RiskLevel | None = None
    confidence: Confidence | None = None

    impact: BehavioralImpact | None = None

    relatedTo: int | None = None
    repoId: int | None = None