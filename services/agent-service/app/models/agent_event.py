from typing import Literal
from pydantic import BaseModel


class AgentEvent(BaseModel):
    """
    Generic event sent from the agent service
    to the frontend via SSE.
    """

    type: Literal[
        "start",
        "status",
        "tool_start",
        "tool_end",
        "token",
        "source",
        "complete",
        "error",
    ]

    message: str | None = None

    content: str | None = None

    tool_name: str | None = None

    data: dict | None = None