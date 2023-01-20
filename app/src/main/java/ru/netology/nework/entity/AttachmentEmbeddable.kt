package ru.netology.nework.entity

import ru.netology.nework.dto.Attachment
import ru.netology.nework.enumeration.AttachmentType

data class AttachmentEmbeddable(
    val uri: String,
    val type: AttachmentType?,
) {
    fun toDto() = Attachment(uri, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.uri, it.type)
        }
    }
}