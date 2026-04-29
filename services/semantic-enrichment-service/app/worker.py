from faststream import FastStream, Context
from faststream.kafka import KafkaBroker
from app.config import settings
from app.logger import log
from app.services.enrichment import enricher

broker = KafkaBroker(settings.KAFKA_BOOTSTRAP_SERVERS)
worker = FastStream(broker)
dlq_publisher = broker.publisher(settings.KAFKA_DLQ)
enriched_publisher = broker.publisher(settings.KAFKA_PRODUCER_TOPIC)

@broker.subscriber(
    settings.KAFKA_TOPIC_CANONICAL,
    group_id=settings.KAFKA_GROUP_ID,
    auto_offset_reset="earliest",
    auto_commit=False
)
async def handle_enrichment(
    msg: dict,
    msg_ctx=Context("message")
):
    artifact_id = msg.get("artifactId")
    log.info("processing_started", artifact_id=artifact_id)

    try:
        event = await enricher.process_artifact(artifact_id=artifact_id)
        await enriched_publisher.publish(event)

        await msg_ctx.ack()
        log.info("processing_complete", artifact_id=artifact_id)
        
    except Exception as e:
        log.error("processing_failed", artifact_id=artifact_id, error=str(e))
        await dlq_publisher.publish(msg)
        await msg_ctx.ack() 
        log.info("message_moved_to_dlq", artifact_id=artifact_id)
