from fastapi import FastAPI
from app.api.specifications import specifications_router


app = FastAPI(
    title="AI Tools Integration",
    version="1.0.0"
)

app.include_router(specifications_router,
                   prefix="/api",
                   tags=["Specifications"])