package ru.netology.nework.repository.event

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.repository.post.ENABLE_PLACE_HOLDERS
import ru.netology.nework.repository.post.PAGE_SIZE
import ru.netology.nework.repository.post.PostRemoteMediator
import java.io.IOException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@JvmSuppressWildcards
class EventRepositoryImpl  @Inject constructor(
    appDb: AppDb,
    private val eventDao: EventDao,
    private val apiService: ApiService,
    eventRemoteKeyDao: EventRemoteKeyDao,
) : EventRepository {
    override val data: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDERS),
        remoteMediator = EventRemoteMediator(apiService, appDb, eventDao, eventRemoteKeyDao),
        pagingSourceFactory = eventDao::pagingSource
    ).flow.map {it.map(PostEntity::toDto) }

    override suspend fun getAll() {
        try {
            val response = apiService.getAllEvent()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.getAll()
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun save(event: Event) {
        try {
            val response = apiService.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(event: Event, upload: MediaUpload) {
        try {
            val media = ru.netology.nework.repository.uploadWithContent(upload, apiService)
            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            save(eventWithAttachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = apiService.removePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getPostById(id: Long): Event {
        TODO("Not yet implemented")
    }

    override suspend fun getMaxId(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun joinById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun rejectById(id: Long) {
        TODO("Not yet implemented")
    }

}