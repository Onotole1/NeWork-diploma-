package ru.netology.nework.dto

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String?,
    val authorAvatar: String?=null,
    val content: String,
    val published: String,
    /*координаты события*/
    val coordinates: String? =null,
    val attachment: Attachment? = null,
    /**
     * Ссылка на связанный ресурс, например:
     * 1. событие (/events/{id})
     * 2. пользователя (/users/{id})
     * 3. другой пост (/posts/{id})
     * 4. внешний контент (https://youtube.com и т.д.)
     * 5. и т.д.
     */
    val link: String? = null,
    /*кто лайкнул*/
    val likeOwnerIds:  Set<Long> = emptySet(),
    val likedByMe: Boolean =false,
    /*кто упоминается*/
    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean =false,
    val ownedByMe: Boolean =false,
    val mentorsNames: List<String?>? = null,
    val jobs: List<String?>? = null

) : FeedItem
{
    companion object {
        val emptyPost = Post(
            id = 0,
            author = User.nuller.name,
            authorId = User.nuller.id,
            content = "",
            published = "2021-08-17T16:46:58.887547Z"
        )
    }
}