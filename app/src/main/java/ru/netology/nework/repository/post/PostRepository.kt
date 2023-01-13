package ru.netology.nework.repository.post

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType


interface PostRepository {
    val data: Flow<PagingData<Post>>

    suspend fun getNewerCount(): Flow<Int>

    suspend fun getAll()

    suspend fun likeById(id: Long, likedByMe: Boolean)

    suspend fun save(post: Post, retry: Boolean)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload, type: AttachmentType, retry: Boolean)

    suspend fun removeById(id: Long)

    suspend fun getPostById(id: Long): Post
    suspend fun getMaxId(): Long

    suspend fun shareById(id: Long)

    suspend fun processWork(id: Long)

    suspend fun getLatest()
}