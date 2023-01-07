package ru.netology.nework.dto

data class TextItemSeparator(
    override val id: Long,
    val text: String,
) : FeedItem