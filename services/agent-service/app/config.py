from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PLANNER_MODEL_API_KEY: str
    PLANNER_MODEL_NAME: str
    REASONING_MODEL_API_KEY: str
    REASONING_MODEL_NAME: str
    ARTIFACT_GATEWAY_URL: str
    
    class Config:
            env_file=(
                ".env.local",
                #".env"
            )

settings = Settings()