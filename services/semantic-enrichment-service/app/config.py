from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    DATABASE_URL: str
    KAFKA_BOOTSTRAP_SERVERS: str
    KAFKA_TOPIC_CANONICAL: str
    EMBEDDING_MODEL_NAME: str
    SUMMARIZER_MODEL_NAME: str
    KAFKA_GROUP_ID: str
    KAFKA_DLQ: str
    KAFKA_PRODUCER_TOPIC: str
    INTERNAL_API_KEY: str
    model_config = SettingsConfigDict(env_file=".env")

settings = Settings()