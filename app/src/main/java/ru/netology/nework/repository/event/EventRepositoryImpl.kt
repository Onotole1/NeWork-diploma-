package ru.netology.nework.repository.event

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.*
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.EventApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.*
import ru.netology.nework.repository.checkResponse
import ru.netology.nework.util.ENABLE_PLACE_HOLDERS
import ru.netology.nework.util.PAGE_SIZE
import java.io.IOException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@JvmSuppressWildcards
class EventRepositoryImpl  @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: EventApiService,
    mediator: EventRemoteMediator,
    @ApplicationContext private val context: Context
) : EventRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDERS),
        remoteMediator = mediator,
        pagingSourceFactory =  { eventDao.getPagingSource() },
    ).flow.map {it.map(EventEntity::toDto) }

    override suspend fun getAll() {
        try {
            val response = apiService.getAllEvent()
            checkResponse(response)
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
        try {
            val response = apiService.likeEventById(id, likedByMe)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.likeById(id, likedByMe)
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(event: Event) {
        try {
            val response = apiService.saveEvent(event)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(event: Event, uri: Uri, type: AttachmentType) {
            try {
                val media = if(!uri.toString().contains("http")) upload(uri).uri else uri.toString()

                val postWithAttachment = event.copy(
                    attachment = Attachment(
                        uri = media,
                        type = type
                    )
                )
                save(postWithAttachment)
            } catch (e: AppError) {
                throw e
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError()
            }
        }

    private suspend fun upload(uri: Uri): Attachment {
        try {
            val contentProvider = context.contentResolver

            val body = withContext(Dispatchers.IO) {
                contentProvider?.openInputStream(uri)?.readBytes()
            }?.toRequestBody("*/*".toMediaType()) ?: error("File not found")

            val media = MultipartBody.Part.createFormData(
                "file", "name", body
            )

            val response = apiService.uploadMedia(media)
            checkResponse(response)
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            eventDao.removeById(id)
            val response = apiService.removeEventById(id)
            checkResponse(response)
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(): Flow<Int> = flow {
        try {
            while (true) {
                val response = apiService.getNewerEvent(getMaxId())
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                eventDao.insert(body.toEntity())
                emit(body.size)
                delay(10_000L)
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
        .flowOn(Dispatchers.Default)

    override suspend fun getEventById(id: Long): Event = eventDao.getEventEventId(id)?.toDto() ?: throw DbError


    override suspend fun getMaxId(): Long = eventDao.getEventEventMaxId()?.toDto()?.id ?: throw DbError

    override suspend fun joinById(id: Long) {
        try {
            val response = apiService.addParticipantEvent(id)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: ApiError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun denyById(id: Long) {
        try {
            val response = apiService.removeParticipantEvent(id)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: ApiError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getLatest() {
        try {
            val response = apiService.getEventLatest(10)
            checkResponse(response)
            val body = response.body() ?: throw Exception()
            eventDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        Log.e("PostRepositoryImpl", "Share is not yet implemented")
    }
}