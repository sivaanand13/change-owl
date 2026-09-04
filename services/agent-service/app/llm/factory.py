from langchain_google_genai import ChatGoogleGenerativeAI
from app.config import settings

def get_llm():
    return ChatGoogleGenerativeAI(
        model=settings.REASONING_MODEL_NAME,
        google_api_key=settings.REASONING_MODEL_API_KEY,
        temperature=0.2
    )

def get_planner_model():
    return ChatGoogleGenerativeAI(
        model=settings.PLANNER_MODEL_NAME,
        google_api_key=settings.PLANNER_MODEL_API_KEY,
        temperature=0
    )


def get_reasoning_model():
    return ChatGoogleGenerativeAI(
        model=settings.REASONING_MODEL_NAME,
        google_api_key=settings.REASONING_MODEL_API_KEY,
        temperature=0.2
    )