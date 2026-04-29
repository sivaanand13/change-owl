from pydantic import BaseModel
from typing import List, Optional

class ArtifactSummary(BaseModel):
    rationale: str
    intent: str
    change_type: str
    change_surface: Optional[str]
    behavioral_impact: Optional[str]
    impact_radius: List[str]
    key_points: List[str]
    risk_level: str
    confidence: str