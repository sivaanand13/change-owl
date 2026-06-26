from datetime import datetime
from typing import Optional
from pydantic import BaseModel
from app.models.technology import Technology


class Repository(BaseModel):
    id: int

    owner: str
    name: str

    isActive: bool

    technology: Optional[Technology] = None

    createdAt: datetime
    lastSyncedAt: Optional[datetime] = None

    @property
    def full_name(self) -> str:
        return f"{self.owner}/{self.name}"