from app.tools.artifacts import artifact_tools

tool_registry = {
    definition.tool.name: definition
    for definition in artifact_tools
}