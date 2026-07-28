from dotenv import load_dotenv
from openai import OpenAI
import os

load_dotenv()

endpoint = os.getenv("AZURE_OPENAI_ENDPOINT")
deployment_name = os.getenv("AZURE_OPENAI_DEPLOYMENT_NAME")
api_key = os.getenv("AZURE_OPENAI_KEY")

client = OpenAI(
    base_url=endpoint,
    api_key=api_key,
)


def call_openai(previous_response_id: str | None, user_input: str):
    if deployment_name is None:
            raise ValueError("AZURE_OPENAI_DEPLOYMENT_NAME environment variable is not set")
    
    stream = client.responses.create(
        model=deployment_name,
        instructions="You are a java tutor, be concise while answering.",
        input=user_input,
        previous_response_id=previous_response_id,
        temperature=0.1,
        stream=True  # Enable streaming for the response
    )
    return stream

if __name__ == "__main__":
    user_question = input("User: ")
    response_id = None
    while user_question.lower() != "exit":
        stream = call_openai(response_id, user_question)
        print("Assistant: ")
        for event in stream:
            if event.type == "response.output_text.delta":
                print(event.delta, end="", flush=True)
            if getattr(event, "response", None) is not None:
                response_id = event.response.id
        print()  # Print a newline after the response is complete
        user_question = input("User: ")