package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String?,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    @Embedded
    val coordinates: Coordinates? = null,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val link: String? = null,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean = false,
    val ownedByMe: Boolean = false,
    val mentorsNames: List<String?>? = null,
    val jobs: List<String?>? = null

) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        content,
        published,
        coordinates,
        attachment = attachment?.toDto(),
        link,
        likeOwnerIds,
        likedByMe,
        mentionIds,
        mentionedMe,
        ownedByMe,
        mentorsNames,
        jobs

    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.coordinates,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.link,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.mentionIds,
                dto.mentionedMe,
                dto.ownedByMe,
                dto.mentorsNames,
                dto.jobs
            )

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity.Companion::fromDto)

