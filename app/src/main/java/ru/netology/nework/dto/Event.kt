package ru.netology.nework.dto

import ru.netology.nework.dto.User.Companion.nuller
import ru.netology.nework.enumeration.EventType

data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?=null,
    val content: String,
    /*дата проведения*/
    val datetime: String,
    val published: String,
    /*координаты события*/
    val coordinates: Coordinates?=null,
    val type: EventType,
    val attachment: Attachment? = null,
    val link: String? = null,
    /*кто лайкнул*/
    val likeOwnerIds:  Set<Long> = emptySet(),
    val likedByMe: Boolean =false,
    /*кто спикер*/
    val speakerIds: MutableSet<Long> = mutableSetOf(),
    /*кто участвует*/
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean=false,
    val ownedByMe: Boolean=false,
) : FeedItem {
companion object {
    val emptyEvent = Event(
        id = 0,
        author=nuller.name,
        authorId = nuller.id,
        content = "",
        datetime = "2021-08-17T16:46:58.887547Z",
        published ="2021-08-17T16:46:58.887547Z",
        type = EventType.ONLINE,
    )
}
}