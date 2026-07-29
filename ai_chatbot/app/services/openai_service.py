from openai import OpenAI
import os
from app.core.config import settings
from app.utils.logger import logger

endpoint = settings.AZURE_OPENAI_ENDPOINT
deployment_name = settings.AZURE_OPENAI_DEPLOYMENT_NAME
api_key = settings.AZURE_OPENAI_KEY

if not endpoint:
    raise ValueError("AZURE_OPENAI_ENDPOINT is not set")

if not api_key:
    raise ValueError("AZURE_OPENAI_KEY is not set")

if not deployment_name:
    raise ValueError("AZURE_OPENAI_DEPLOYMENT_NAME is not set")

client = OpenAI(
    base_url=endpoint,
    api_key=api_key,
)


def stream_chat(chat_id: str | None, user_input: str):
    if deployment_name is None:
            raise ValueError("AZURE_OPENAI_DEPLOYMENT_NAME environment variable is not set")
    
    stream = client.responses.create(
        model=deployment_name,
        instructions="You are a java tutor, be concise while answering.",
        input=user_input,
        previous_response_id=chat_id,
        temperature=0.1,
        stream=True  # Enable streaming for the response
    )
    response_id_sent = False

    for event in stream:
        logger.info(f"Event: {event.type}, response: {getattr(event, 'response', None)}, Delta: {getattr(event, 'delta', None)}")  # Debugging line
        if not response_id_sent:
            response_id = getattr(getattr(event, "response", None), "id", None)
            if response_id:
                 yield f"event: response_id\ndata: {response_id}\n\n"
        if event.type == "response.output_text.delta":
            # yield event.delta
            yield f"data: {event.delta}\n\n"