# AI Chatbot

FastAPI service exposing OpenAI streaming responses.

## Setup

Create virtual environment:

python -m venv .venv


Activate:

Mac/Linux:

source .venv/bin/activate


Install dependencies:

pip install -r requirements.txt


Create .env

OPENAI_API_KEY=<your-key>
AZURE_OPENAI_ENDPOINT=<your-resource-endpoint>
AZURE_OPENAI_DEPLOYMENT_NAME=<your-deployment-model>


Run:

uvicorn app.main:app --reload


## API

POST

/api/chat/stream


Request:

curl --location 'localhost:8000/api/chat/stream/' \
--header 'Content-Type: application/json' \
--data '{
    "chat_id": null,
    "user_input": "one more line"
}'


Response:

Streaming text response