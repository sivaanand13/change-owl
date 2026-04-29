from app.config import settings
from app.logger import log
from app.model.artifact_summary import ArtifactSummary
from transformers import pipeline
import torch
import re
from app.model.artifact import Artifact
import json

class TechnicalSummarizer:
    def __init__(self):
        self.model_id = settings.SUMMARIZER_MODEL_NAME
        self._pipe = None

    @property
    def pipe(self):
        """Lazy loader for the transformer pipeline."""
        if self._pipe is None:
            log.info("lazy_loading_summarizer_start", model=self.model_id)
            self._pipe = pipeline(
                "text-generation",
                model=self.model_id,
                dtype=torch.float16,
                device=-1,
                #device_map="auto"
            )
            log.info("lazy_loading_summarizer_complete")
        return self._pipe

    def summarize(self, artifact: Artifact) -> ArtifactSummary:
        prompt = self._build_prompt(artifact=artifact)
        try:
            outputs = self.pipe(
                prompt,
                do_sample=False,
                max_new_tokens=200,
                max_length=None,
                pad_token_id=self.pipe.tokenizer.eos_token_id
            )
            raw_text = outputs[0]['generated_text'].split("<|im_start|>assistant")[-1]

            raw_json = self._extract_json(raw_text)
            return ArtifactSummary(**json.loads(raw_json))
            
        except Exception as e:
            log.error("summarization_failed", error=str(e))
            raise

    def _extract_json(self, text: str) -> str:
        start = text.find("{")
        end = text.rfind("}")
        return text[start:end+1] if start != -1 and end != -1 else text
    
    def _build_prompt(self, artifact: Artifact) -> str:
        content = artifact.to_narrative()

        return (
            "<|im_start|>system\n"
            "You are a strict technical summarizer. Output ONLY valid JSON.\n"
            "\n"
            "RULES:\n"
            "- Only use information explicitly present in the input.\n"
            "- Do NOT infer or guess missing motivations.\n"
            "- If rationale is not explicitly stated, output: \"Not explicitly stated in PR\".\n"
            "- Avoid vague phrases like 'improves performance' unless explicitly stated.\n"
            "- Prefer concrete technical details (functions, APIs, behaviors).\n"
            "- impact_radius must use short technical domains (e.g., 'auth', 'streaming', 'dependency.nbconvert').\n"
            "- key_points must describe specific changes (functions, APIs, versions, behaviors).\n"
            "\n"
            "OUTPUT SCHEMA (JSON only):\n"
            "Keys: rationale, intent, change_type, change_surface, behavioral_impact, impact_radius, key_points, risk_level, confidence\n"
            "\n"
            "Allowed change_types: bugfix, feature, refactor, arch_change, chore\n"
            "Allowed change_surface: api, runtime, dependency, internal, infra\n"
            "Allowed behavioral_impact: none, bugfix, performance, functional_change, breaking_change\n"
            "Allowed risk_levels: low, medium, high\n"
            "Allowed confidence: high, medium, low\n"
            "STRICT OUTPUT RULES:\n"
            "- Output ONLY valid JSON\n"
            "- Must match schema exactly\n"
            "- Do NOT output values outside allowed enums\n"
            "- If unknown, use 'unknown' only if allowed, otherwise infer best match\n"
            "- Never abbreviate values (e.g. 'feat' is invalid)\n"
            "<|im_end|>\n"

            # --- EXAMPLE 1: BUGFIX ---
            "<|im_start|>user\n"
            "Analyze pull_request: fix: resolve null pointer in user auth (#101)\n"
            "Content: Fixed a crash when user is not logged in but tries to access /profile.<|im_end|>\n"
            "<|im_start|>assistant\n"
            "{\"rationale\": \"Not explicitly stated in PR\", \"intent\": \"Prevent crash when unauthenticated users access /profile\", \"change_type\": \"bugfix\", \"change_surface\": \"runtime\", \"behavioral_impact\": \"bugfix\", \"impact_radius\": [\"auth\", \"profile\"], \"key_points\": [\"Added null check for unauthenticated user\", \"Prevents crash on /profile route\"], \"risk_level\": \"low\", \"confidence\": \"high\"}<|im_end|>\n"

            # --- EXAMPLE 2: ARCH CHANGE ---
            "<|im_start|>user\n"
            "Analyze design_doc: Migrating to Kafka for Events\n"
            "Content: Moving from polling the DB to a pub/sub model with Kafka to handle higher load.<|im_end|>\n"
            "<|im_start|>assistant\n"
            "{\"rationale\": \"The existing database polling mechanism is hitting scaling limits.\", \"intent\": \"Replace polling with pub/sub to handle higher load\", \"change_type\": \"arch_change\", \"change_surface\": \"infra\", \"behavioral_impact\": \"functional_change\", \"impact_radius\": [\"data_pipeline\", \"kafka\", \"event_consumers\"], \"key_points\": [\"Introduce Kafka pub/sub model\", \"Replace database polling mechanism\"], \"risk_level\": \"high\", \"confidence\": \"high\"}<|im_end|>\n"

            # --- EXAMPLE 3: DEPENDENCY UPDATE (SECURITY) ---
            "<|im_start|>user\n"
            "Analyze pull_request: chore: bump nbconvert from 7.17.0 to 7.17.1\n"
            "Content: Security release fixing CVE-2026-39377 and CVE-2026-39378.<|im_end|>\n"
            "<|im_start|>assistant\n"
            "{\"rationale\": \"Security vulnerabilities in nbconvert (CVE-2026-39377, CVE-2026-39378)\", \"intent\": \"Upgrade dependency to patched version\", \"change_type\": \"chore\", \"change_surface\": \"dependency\", \"behavioral_impact\": \"none\", \"impact_radius\": [\"dependency.nbconvert\"], \"key_points\": [\"Bumped nbconvert from 7.17.0 to 7.17.1\", \"Includes fixes for CVE-2026-39377 and CVE-2026-39378\"], \"risk_level\": \"low\", \"confidence\": \"high\"}<|im_end|>\n"

            # --- EXAMPLE 4: PERFORMANCE ---
            "<|im_start|>user\n"
            "Analyze pull_request: perf(core): defer tracer imports\n"
            "Content: Moves tracer imports to TYPE_CHECKING and defers imports at call sites to reduce import overhead (~76ms).<|im_end|>\n"
            "<|im_start|>assistant\n"
            "{\"rationale\": \"Import overhead from tracer modules impacted performance.\", \"intent\": \"Reduce import-time latency\", \"change_type\": \"refactor\", \"change_surface\": \"runtime\", \"behavioral_impact\": \"performance\", \"impact_radius\": [\"core\", \"tracing\"], \"key_points\": [\"Moved tracer imports to TYPE_CHECKING\", \"Deferred imports to call sites\", \"Reduced import overhead by ~76ms\"], \"risk_level\": \"low\", \"confidence\": \"high\"}<|im_end|>\n"

            # --- EXAMPLE 5: LOW INFORMATION ---
            "<|im_start|>user\n"
            "Analyze pull_request: chore: update dependencies\n"
            "Content: Minor updates.<|im_end|>\n"
            "<|im_start|>assistant\n"
            "{\"rationale\": \"Not explicitly stated in PR\", \"intent\": \"Update project dependencies\", \"change_type\": \"chore\", \"change_surface\": \"dependency\", \"behavioral_impact\": \"none\", \"impact_radius\": [\"dependency\"], \"key_points\": [\"Updated unspecified dependencies\"], \"risk_level\": \"low\", \"confidence\": \"low\"}<|im_end|>\n"

            # --- ACTUAL TASK ---
            "<|im_start|>user\n"
            f"Analyze {artifact.type}: {artifact.title}\n"
            f"Content: {content[:2000]}\n"
            "Return ONLY JSON.<|im_end|>\n"
            "<|im_start|>assistant\n"
        )

summarizer = TechnicalSummarizer()