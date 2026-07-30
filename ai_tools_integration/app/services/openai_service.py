from pydantic import tools

from app.config.settings import settings
from app.models.product_details import ProductDetails
from openai import AsyncOpenAI


client = AsyncOpenAI(
    api_key=settings.AZURE_OPENAI_KEY,
    base_url=settings.AZURE_OPENAI_ENDPOINT
)

async def find_specifications(product_details: ProductDetails):

    response = await client.responses.create(
        model=settings.AZURE_OPENAI_DEPLOYMENT_NAME,
        instructions="""
            Find specifications of given product.
            Do not helusinate or make up any specifications. 
            Do web search, find the brand of the product, go to official website, find the specifications of the product. If you don't find any specifications, say "I don't know".
            At max, return 5 specifications. Stop web search after 5 specifications. Do not return any other information.
        """,
        input=f"Find specifications for {product_details.product_name} with description {product_details.product_description}.",
        tools=[
                { 
                    "type": "web_search" 
                }
            ]
    )
    return response.output_text


if __name__ == "__main__":
    product_name = input("Enter the product name: ")
    product_description = input("Enter the product description: ")
    product_details = ProductDetails(product_name=product_name, product_description=product_description)
    specifications = find_specifications(product_details)
    print("Specifications: " + specifications)