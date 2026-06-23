@tool
async def search_artifacts(
    query: str,
    limit: int = 10
):
    """
    Search ChangeOwl artifacts related to an engineering topic.
    """

    return await client.search_artifacts(
        q=query,
        limit=limit
    )