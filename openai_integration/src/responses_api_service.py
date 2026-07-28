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

response_id = None  # Initialize response_id to None for the first call

def call_openai(previous_response_id: str, user_input: str):
    global response_id  
    response = client.responses.create(
        model=deployment_name,
        instructions="You are a java tutor, be concise while answering.",
        input=user_input,
        previous_response_id=previous_response_id
    )
    response_text = response.output_text
    response_id = response.id
    return response_text

if __name__ == "__main__":
    user_question = input("User: ")
    while user_question.lower() != "exit":
        response = call_openai(response_id, user_question)
        print("Assistant: " + response)
        user_question = input("User: ")