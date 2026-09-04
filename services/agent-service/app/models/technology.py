from pydantic import BaseModel

class Technology(BaseModel):
    id: int
    name: str
    slug: str