package ru.netology.nework.entity

import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.User

interface FeedEnity{
    fun toDto(): FeedItem
    companion object : FeedEnity {
        fun fromDto(dto: FeedItem) = FeedEnity
    }
    fun List<FeedEnity>.toDto(): List<FeedItem> = map(FeedEnity::toDto)
    fun List<FeedItem>.toEntity(): List<FeedEnity> = map(FeedEnity.Companion::fromDto)
}
