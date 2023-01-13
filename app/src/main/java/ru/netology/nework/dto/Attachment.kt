package ru.netology.nework.dto

import android.net.Uri
import ru.netology.nework.enumeration.AttachmentType
import java.io.File
data class Attachment(
    val uri: String,
    val type: AttachmentType?,
) {
    fun toDto() = Attachment(uri, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            Attachment(it.uri, it.type)
        }
    }
}

data class MediaUpload(val uri: Uri)

data class MediaUploadFile(val file: File)