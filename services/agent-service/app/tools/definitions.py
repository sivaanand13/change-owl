from dataclasses import dataclass
from langchain_core.tools import BaseTool


@dataclass
class ToolDefinition:
    tool: BaseTool

    start_message: str
    end_message: str | None = None

    icon: str | None = None