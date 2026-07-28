from dotenv import load_dotenv
from openai import OpenAI
load_dotenv()

import os

endpoint = os.getenv("AZURE_OPENAI_ENDPOINT")
deployment_name = os.getenv("AZURE_OPENAI_DEPLOYMENT_NAME")
api_key = os.getenv("AZURE_OPENAI_KEY")

client = OpenAI(
    base_url=endpoint,
    api_key=api_key,
)

def call_openai(user_input: str):
    if deployment_name is None:
            raise ValueError("AZURE_OPENAI_DEPLOYMENT_NAME environment variable is not set")
    
    response = client.chat.completions.create(
        model=deployment_name,
        messages=[
            {"role": "system", "content": "You are a java tutor, be concise while answering."},
            {"role": "user", "content": user_input},
        ],
    )

    response_text = response.choices[0].message.content
    return response_text



if __name__ == "__main__":
    # Get input from the user and call the API
    user_question = input("Enter your question: ")
    response = call_openai(user_question)
    print(response)
