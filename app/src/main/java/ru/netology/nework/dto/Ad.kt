package ru.netology.nework.dto

data class Ad(
    override val id: Long,
    val name : String,
) : FeedItem