"""File upload and download API endpoints."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from fastapi.responses import JSONResponse
from pydantic import BaseModel

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.db import supabase as db

router = APIRouter(prefix="/files", tags=["files"])

# Max file size: 10MB
MAX_FILE_SIZE = 10 * 1024 * 1024


class FileResponse(BaseModel):
    id: str
    filename: str
    size: int
    storage_path: str
    created_at: str | None = None


class FileListResponse(BaseModel):
    files: list[FileResponse]


@router.post("/{conversation_id}/upload", response_model=FileResponse)
async def upload_file(
    conversation_id: str,
    file: UploadFile = File(...),
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Upload a file for use in a conversation."""
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    content = await file.read()
    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(status_code=413, detail="File too large (max 10MB)")

    filename = file.filename or "upload"
    storage_path = f"user-files/{user.user_id}/{conversation_id}/{filename}"

    # Store in Supabase Storage
    sb = db.get_supabase()
    try:
        sb.storage.from_("files").upload(
            storage_path,
            content,
            {"content-type": file.content_type or "application/octet-stream"},
        )
    except Exception:
        # File might already exist, try update
        sb.storage.from_("files").update(
            storage_path,
            content,
            {"content-type": file.content_type or "application/octet-stream"},
        )

    # Record in DB
    file_record = await db.insert_file(
        user_id=user.user_id,
        conversation_id=conversation_id,
        filename=filename,
        size=len(content),
        storage_path=storage_path,
    )

    return FileResponse(**file_record)


@router.get("/{conversation_id}", response_model=FileListResponse)
async def list_files(
    conversation_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """List all files in a conversation."""
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    files = await db.get_files(conversation_id)
    return FileListResponse(files=[FileResponse(**f) for f in files])


@router.get("/{conversation_id}/{file_id}/url")
async def get_file_url(
    conversation_id: str,
    file_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Get a signed download URL for a file."""
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    file_record = await db.get_file(file_id)
    if not file_record or file_record.get("user_id") != user.user_id:
        raise HTTPException(status_code=404, detail="File not found")

    sb = db.get_supabase()
    signed = sb.storage.from_("files").create_signed_url(
        file_record["storage_path"], 3600  # 1 hour expiry
    )

    return {"url": signed.get("signedURL", ""), "filename": file_record["filename"]}
