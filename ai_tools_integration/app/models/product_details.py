from pydantic import BaseModel


class ProductDetails(BaseModel):
    product_name: str
    product_description: str