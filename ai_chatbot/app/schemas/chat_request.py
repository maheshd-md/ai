from pydantic import BaseModel

class ChatRequest(BaseModel):
    chat_id: str | None = None
    user_input: str