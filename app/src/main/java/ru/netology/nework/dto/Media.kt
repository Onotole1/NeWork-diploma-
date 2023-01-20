package ru.netology.nework.dto

import java.io.File
import java.io.InputStream

data class Media(
    val uri: String,
)

data class MediaUpload(val inputStream: InputStream)

data class MediaUploadFile(val file: File)