package ru.netology.nework.repository.post

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.*


interface PostRepository {
    val data: Flow<PagingData<Post>>

    suspend fun getNewerCount(): Flow<Int>

    suspend fun getAll()

    suspend fun likeById(id: Long, likedByMe: Boolean)

    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)

    suspend fun updateUser(login: String, pass: String): User

    suspend fun registerUser(login: String, pass: String, name: String): User

    suspend fun removeById(id: Long)

    suspend fun getPostById(id: Long): Post
    suspend fun getMaxId(): Long
}