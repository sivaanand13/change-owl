from typing import Literal

from pydantic import BaseModel


ChangeType = Literal[
    "bugfix",
    "feature",
    "refactor",
    "arch_change",
    "chore",
]

ChangeSurface = Literal[
    "api",
    "runtime",
    "dependency",
    "internal",
    "infra",
]

BehavioralImpact = Literal[
    "none",
    "bugfix",
    "performance",
    "functional_change",
    "breaking_change",
]

RiskLevel = Literal[
    "low",
    "medium",
    "high",
]

Confidence = Literal[
    "high",
    "medium",
    "low",
]


class ArtifactIntelligence(BaseModel):
    rationale: str
    intent: str

    change_type: ChangeType
    change_surface: ChangeSurface
    behavioral_impact: BehavioralImpact

    impact_radius: list[str]
    key_points: list[str]

    risk_level: RiskLevel
    confidence: Confidence