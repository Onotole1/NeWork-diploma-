package ru.netology.nework.model

import android.net.Uri
import ru.netology.nework.enumeration.AttachmentType

import java.io.InputStream

data class PhotoModel(val uri: Uri? = null)

data class MediaModel(
    val uri: Uri? = null,
    val inputStream: InputStream? = null,
    val type: AttachmentType? = null
)

