package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post

interface WallApiService :ApiService {
    @GET("{ userId } / wall")
    suspend fun getWall(@Path("userId") userId: Long): Response<List<FeedItem>>

    @GET("{ userId } / wall / latest")
    suspend fun getWallLatest(
        @Path("userId") userId: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{userId}/wall/{id } / after")
    suspend fun getWallAfter(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{ userId } / wall / { id } / before")
    suspend fun getWallBefore(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{ userId } / wall / { id } / newer")
    suspend fun getNewerWall(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>
}