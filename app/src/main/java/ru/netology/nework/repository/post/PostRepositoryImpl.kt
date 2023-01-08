package ru.netology.nework.repository.post

import androidx.paging.*
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.*
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.*
import ru.netology.nework.repository.uploadWithContent

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

const val PAGE_SIZE = 8
const val ENABLE_PLACE_HOLDERS = false

@Singleton
@JvmSuppressWildcards
class PostRepositoryImpl @Inject constructor(
    appDb: AppDb,
    private val postDao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    ) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDERS),
        remoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao),
        pagingSourceFactory = postDao::pagingSource,
    ).flow.map {it.map(PostEntity::toDto) }


    override suspend fun getAll() {
        try {
            val response = apiService.getAllPost()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

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

    override suspend fun getNewerCount()= flow {
        try {
            while (true) {
                val response = apiService.getNewerPost(getMaxId())
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
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


    override suspend fun save(post: Post) {
        try {
            val response = apiService.savePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = uploadWithContent(upload,apiService)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            save(postWithAttachment)
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

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        try {
            val response = apiService.likePostById(id, likedByMe)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.likeById(id, likedByMe)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun updateUser(login: String, pass: String): User {
        try {
            val response = apiService.updateUser(login, pass)
            checkResponse(response)
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUser(login: String, pass: String, name: String): User {
        try {
            val response = apiService.registrationUser(login, pass, name)
            checkResponse(response)
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun getPostById(id: Long) = postDao.getPostById(id)?.toDto() ?: throw DbError

    override suspend fun getMaxId() = postDao.getPostMaxId()?.toDto()?.id ?: throw DbError
}

private fun checkResponse(response: Response<out Any>) {
    if (!response.isSuccessful) {
        throw ApiError(response.code(), response.message())
    }
}

