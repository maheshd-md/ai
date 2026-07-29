from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from app.schemas.chat_request import ChatRequest
from app.services.openai_service import stream_chat


router = APIRouter(
    tags=["Chat"]
)

@router.post("/chat/stream")
async def chat_stream(
    request: ChatRequest
):
    """
    Endpoint for streaming chat responses.
    """
    # Implement the logic for streaming chat responses here
    return StreamingResponse(
        stream_chat(request.chat_id, request.user_input),
        # media_type="text/plain"
        media_type="text/event-stream"
    )