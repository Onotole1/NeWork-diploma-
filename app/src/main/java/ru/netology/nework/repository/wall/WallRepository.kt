package ru.netology.nework.repository

import androidx.paging.PagingConfig
import androidx.paging.*
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.netology.nework.api.WallApiService
import ru.netology.nework.dao.PostDao
import kotlinx.coroutines.flow.map
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.DbError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.repository.wall.WallRemoteMediator
import java.io.IOException
import javax.inject.Inject


interface WallRepository {
    suspend fun userWall(userId: Long): Flow<PagingData<Post>>
    suspend fun getWallNewerCount(): Flow<Int>
    suspend fun getWallLatest()
    suspend fun getPostWallMaxId(userId: Long): Long
}
class WallRepositoryImpl @Inject constructor(
    private val apiService: WallApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val db: AppDb,
    private val userId: Long
):WallRepository {

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun userWall(userId: Long): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(apiService, postDao, postRemoteKeyDao, db, userId),
        pagingSourceFactory = { postDao.getPagingSource(userId) },
    ).flow
        .map {it.map(PostEntity::toDto) }

    override suspend fun getWallNewerCount(): Flow<Int>  = flow {
        try {
            while (true) {
                val response = apiService.getNewerWall(userId, getPostWallMaxId(userId),10)
                checkResponse(response)
                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(body.toEntity())
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

    override suspend fun getWallLatest() {
        try {
            val response = apiService.getWallLatest(userId,10)
            checkResponse(response)
            val body = response.body() ?: throw Exception()
            postDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getPostWallMaxId(userId: Long) = postDao.getPostWallMaxId(userId)?.toDto()?.id ?: throw DbError

}
