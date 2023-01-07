package ru.netology.nework.dto

import ru.netology.nework.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType?,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            Attachment(it.url, it.type)
        }
    }
}