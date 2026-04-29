from pydantic import BaseModel
from datetime import datetime
from typing import Optional

class ArtifactEnrichedEvent(BaseModel):
    artifact_id: int
    repo_id: int
    repo_name: str
    tech_id: int
    tech_name: str
    change_type: str
    risk_level: str
    confidence: Optional[str]
    embedding_model: str
    summarizer_model: str
    status: str
    createdAt: datetime