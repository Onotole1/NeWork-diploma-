package ru.netology.nework.repository.post

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.paging.*
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
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
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dto.*
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.*
import ru.netology.nework.repository.checkResponse
import ru.netology.nework.repository.uploadWithContent
import ru.netology.nework.util.ENABLE_PLACE_HOLDERS
import ru.netology.nework.util.PAGE_SIZE
import java.io.IOException


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@JvmSuppressWildcards
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: PostApiService,
    mediator: PostRemoteMediator,
    @ApplicationContext private val context: Context
    ) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDERS),
        remoteMediator = mediator,
        pagingSourceFactory =  { postDao.getPagingSource() },
    ).flow.map {it.map(PostEntity::toDto) }

    override suspend fun getAll() {
        try {
            val response = apiService.getAllPost()
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.getAll()
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun shareById(id: Long) {
        Log.e("PostRepositoryImpl", "Share is not yet implemented")
    }

    override suspend fun getNewerCount()= flow {
        try {
            while (true) {
                val response = apiService.getNewerPost(getMaxId())
                checkResponse(response)
                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(body.toEntity())
                emit(body.size)
                delay(10_000L)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
        .flowOn(Dispatchers.Default)


    override suspend fun save(post: Post, retry: Boolean) {
        try {
            val response = apiService.savePost(post)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveWithAttachment(post: Post, uri: Uri, type: AttachmentType) {
        try {
            val media = if(!uri.toString().contains("http")) upload(uri).uri else uri.toString()

            val postWithAttachment = post.copy(
                attachment = Attachment(
                    uri = media,
                    type = type
                )
            )
            save(postWithAttachment,false)
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

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload, type: AttachmentType,retry: Boolean) {
        try {
            val media = uploadWithContent(upload,apiService)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.uri, media.type))
            save(postWithAttachment,false)
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
            checkResponse(response)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        try {
            val response = apiService.likePostById(id, likedByMe)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.likeById(id, likedByMe)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getPostById(id: Long) = postDao.getPostById(id)?.toDto() ?: throw DbError

    override suspend fun getMaxId() = postDao.getPostMaxId()?.toDto()?.id ?: throw DbError

    override suspend fun processWork(id: Long) {
        try {
            val entity = postDao.getPostById(id)
            val post = entity?.toDto()
            val uri = entity?.attachment?.uri?.toUri()
            val type = entity?.attachment?.type
            if (uri != null && type != null) {
                post?.let { saveWithAttachment(it, uri, type) }
            } else {
                post?.let { save(it,false) }
            }
            postDao.removeById(id)
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun getLatest() {
        try {
            val response = apiService.getPostLatest(10)
            checkResponse(response)
            val body = response.body() ?: throw Exception()
            postDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}


