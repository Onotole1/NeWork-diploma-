package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.FeedDao
import ru.netology.nework.dao.FeedRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.FeedEnity
import ru.netology.nework.entity.KeyType
import ru.netology.nework.entity.RemoteKeyEntity
import ru.netology.nework.error.*

@OptIn(ExperimentalPagingApi::class)
interface FeedRemoteMediator (
    private val service: ApiService,
    private val db: AppDb,
    private val dao: FeedDao,
    private val remoteKeyDao: FeedRemoteKeyDao,
    ) : RemoteMediator<Int, FeedEnity>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, FeedEnity>,
        ): MediatorResult {
            try {
                val response = when (loadType) {
                    LoadType.REFRESH -> {
                        remoteKeyDao.max()?.let {
                            service.getPostAfter(it, state.config.pageSize)
                        } ?: service.getPostLatest(state.config.initialLoadSize)
                    }
                    LoadType.PREPEND -> {
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }
                    LoadType.APPEND -> {
                        val id = remoteKeyDao.min() ?: return MediatorResult.Success(
                            endOfPaginationReached = false
                        )
                        service.getPostBefore(id, state.config.pageSize)
                    }
                }

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(
                    response.code(),
                    response.message(),
                )

                db.withTransaction {
                    when (loadType) {
                        LoadType.REFRESH -> {
                            remoteKeyDao.insert(
                                RemoteKeyEntity(
                                    type = KeyType.AFTER,
                                    id = body.first().id,
                                )
                            )
                            if (remoteKeyDao.isEmpty()) {
                                remoteKeyDao.insert(
                                    RemoteKeyEntity(
                                        type = KeyType.BEFORE,
                                        id = body.last().id,
                                    )
                                )
                            }
                        }
                        LoadType.PREPEND -> {
                            remoteKeyDao.insert(
                                RemoteKeyEntity(
                                    type = KeyType.AFTER,
                                    id = body.first().id,
                                )
                            )
                        }
                        LoadType.APPEND -> {
                            remoteKeyDao.insert(
                                RemoteKeyEntity(
                                    type = KeyType.BEFORE,
                                    id = body.last().id,
                                )
                            )
                        }
                    }
                    dao.insert(body.toEntity())
                }
                return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
            } catch (e: Exception) {
                return MediatorResult.Error(e)
            }
        }
    }