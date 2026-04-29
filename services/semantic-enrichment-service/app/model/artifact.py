from pydantic import BaseModel
from typing import Optional

class Artifact(BaseModel):
    id: int
    title: Optional[str] = "Untitled"
    body: Optional[str] = ""
    type: str
    author: Optional[str] = "Unknown"
    source: str
    state: Optional[str] = "active"
    tech_name: str
    repo_name: str
    url: Optional[str] = None
    repo_id: int
    tech_id: int

    def to_narrative(self) -> str:
        type_normalized = self.type.replace("_", " ")

        return (
            f"{type_normalized} in {self.repo_name} ({self.tech_name})\n"
            f"title: {self.title}\n"
            f"author: {self.author} | state: {self.state} | source: {self.source}\n"
            f"content:\n{self.body}"
        )