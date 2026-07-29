from dotenv import load_dotenv
from pydantic_settings import BaseSettings

load_dotenv()  # Load environment variables from .env file

class Settings(BaseSettings):
    AZURE_OPENAI_ENDPOINT: str
    AZURE_OPENAI_DEPLOYMENT_NAME: str
    AZURE_OPENAI_KEY: str

    class Config:
        env_file = ".env"

settings = Settings()