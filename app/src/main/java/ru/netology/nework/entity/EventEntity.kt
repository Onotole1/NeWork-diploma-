package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.User
import ru.netology.nework.enumeration.EventType


@Entity(tableName = "events")
class EventEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coordinates: Coordinates?=null,
    val type: EventType,
    val attachment: Attachment? = null,
    val link: String? = null,
    val likeOwnerIds:  Set<Long> = emptySet(),
    val likedByMe: Boolean =false,
    val speakerIds: MutableSet<Long> = mutableSetOf(),
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean=false,
    val ownedByMe: Boolean=false,
    ) {
        fun toDto() = Event(
            id,
            authorId,
            author,
            authorAvatar,
            content,
            datetime,
            published,
            coordinates,
            type,
            attachment,
            link,
            likeOwnerIds,
            likedByMe,
            speakerIds,
            participantsIds,
            participatedByMe,
            ownedByMe,
        )

        companion object {
            fun fromDto(dto: Event) =
                EventEntity(
                    dto.id,
                    dto.authorId,
                    dto.author,
                    dto.authorAvatar,
                    dto.content,
                    dto.datetime,
                    dto.published,
                    dto.coordinates,
                    dto.type,
                    dto.attachment,
                    dto.link,
                    dto.likeOwnerIds,
                    dto.likedByMe,
                    dto.speakerIds,
                    dto.participantsIds,
                    dto.participatedByMe,
                    dto.ownedByMe,
                )

        }
    }

    fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
    fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity.Companion::fromDto)