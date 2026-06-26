from pydantic import BaseModel
from typing import List
from app.models.artifact import Artifact


class ArtifactPage(BaseModel):
    artifacts: List[Artifact]
    limit: int
    offset: int
    total: int

    @property
    def has_more(self) -> bool:
        return self.offset + self.limit < self.total