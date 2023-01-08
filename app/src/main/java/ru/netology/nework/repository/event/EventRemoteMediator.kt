package ru.netology.nework.repository.event

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.*
import ru.netology.nework.error.ApiError


@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    eventRemoteKeyDao.max()?.let {
                        service.getEventAfter(it, state.config.pageSize)
                    } ?: service.getEventLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getEventBefore(id, state.config.pageSize)
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
                        eventRemoteKeyDao.insert(
                            RemoteKeyEntity(
                                type = KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                        if (eventRemoteKeyDao.isEmpty()) {
                            eventRemoteKeyDao.insert(
                                RemoteKeyEntity(
                                    type =KeyType.BEFORE,
                                    id = body.last().id,
                                )
                            )
                        }
                    }
                    LoadType.PREPEND -> {
                        eventRemoteKeyDao.insert(
                            RemoteKeyEntity(
                                type = KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            RemoteKeyEntity(
                                type = KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                eventDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}