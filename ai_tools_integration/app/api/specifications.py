from fastapi import APIRouter
from fastapi.responses import JSONResponse
from app.models.product_details import ProductDetails
from app.services import openai_service

specifications_router = APIRouter(
    prefix="/specifications"
)

@specifications_router.post(path="/find", summary="Find specifications")
async def find_specifications(product_details: ProductDetails):
    specifications = await openai_service.find_specifications(product_details)
    return JSONResponse(
        status_code=200,
        content={
            "specifications": specifications
        }
    )
