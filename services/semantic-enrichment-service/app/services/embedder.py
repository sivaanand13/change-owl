from sentence_transformers import SentenceTransformer
from app.config import settings
from app.logger import log
import torch
from app.model.artifact import Artifact

class EmbeddingService:
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"

        log.info(f"Loading {settings.EMBEDDING_MODEL_NAME} on {self.device}...")
        self.model = SentenceTransformer(
            settings.EMBEDDING_MODEL_NAME,
            trust_remote_code=True,
            device=self.device
        )
    
    def generate_embedding(self, artifact: Artifact):
        input = artifact.to_narrative()
        embedding = self.model.encode(input)
        return embedding.tolist()

embedder = EmbeddingService()