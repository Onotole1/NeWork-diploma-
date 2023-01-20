package ru.netology.nework.repository.event

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType

interface EventRepository {
    val data: Flow<PagingData<Event>>

    suspend fun getAll()

    suspend fun getEventById(id: Long): Event

    fun getNewerCount(): Flow<Int>

    suspend fun likeEventById(id: Long, likedByMe: Boolean)

    suspend fun save(event: Event)

    suspend fun saveWithAttachment(event: Event, uri: Uri, type: AttachmentType)

    suspend fun removeById(id: Long)

    suspend fun getMaxId(): Long
    suspend fun joinById(id: Long)
    suspend fun leaveById(id: Long)

    suspend fun shareById(id: Long)

    suspend fun getLatest()
}