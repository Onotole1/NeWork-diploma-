package ru.netology.nework.repository.event

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.*

interface EventRepository {
    val data: Flow<PagingData<Event>>

    suspend fun getAll()

    suspend fun likeById(id: Long, likedByMe: Boolean)

    suspend fun save(event: Event)

    suspend fun saveWithAttachment(event: Event, upload: MediaUpload)

    suspend fun removeById(id: Long)

    suspend fun getPostById(id: Long): Event
    suspend fun getMaxId(): Long
    suspend fun joinById(id: Long)
    suspend fun rejectById(id: Long)
}