package ru.netology.nework.entity

import androidx.room.*
import ru.netology.nework.dao.Converters
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.EventType


@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String?,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    @TypeConverters(Converters::class)
    @Embedded
    val coordinates: String?=null,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "_type")
    val type: EventType,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val link: String? = null,
    val likeOwnerIds:  MutableSet<Long> = mutableSetOf(),
    val likedByMe: Boolean =false,
    val speakerIds: MutableSet<Long> = mutableSetOf(),
    val participantsIds: MutableSet<Long> = mutableSetOf(),
    val participatedByMe: Boolean=false,
    val ownedByMe: Boolean=false,
    ){
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
            attachment = attachment?.toDto(),
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
                    AttachmentEmbeddable.fromDto(dto.attachment),
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