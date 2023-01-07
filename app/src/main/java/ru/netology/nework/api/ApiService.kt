package ru.netology.nework.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nework.BuildConfig
import ru.netology.nework.dto.*
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.UserEntity

import androidx.room.Query as Query1


private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"

fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()


interface ApiService {
    //Посты
    @GET("posts")
    suspend fun getAllPost(): Response<List<Post>>

    @GET("posts/{post_id}/newer")
    suspend fun getNewerPost(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{post_id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @GET("posts/{post_id}/before")
    suspend fun getPostBefore(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("posts/{post_id}/after")
    suspend fun getPostAfter(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getPostLatest(@Query("count") count: Int): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("posts/{post_id}")
    suspend fun removePostById(@Path("id") id: Long): Response<Unit>

    @Query1(
        """
                UPDATE posts SET
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id AND likedByMe=:likedByMe
                """
    )
    suspend fun likePostById(
        @Path("id") id: Long,
        @Path("likedByMe") likedByMe: Boolean,
    ): Response<Post>

    //Медиа
    @Multipart
    @POST("media")
    suspend fun uploadMedia(
        @Part part: MultipartBody.Part,
        @Part content: MultipartBody.Part,
    ): Response<Media>

    //Юзеры
    @GET("users")
    suspend fun getAllUsers(): Response<List<UserEntity>>

    @GET("users/{user_id}")
    suspend fun getUsersById(@Path("id") id: Long): Response<User>

    /*@POST("users/push-tokens")
    suspend fun sendPushToken(@Body pushToken: PushToken): Response<Unit>*/

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
    ): Response<User>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registrationUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String,
    ): Response<User>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("users/authentication")
    @FormUrlEncoded
    suspend fun getToken(
        @Field("login") login: String,
        @Field("pass") pass: String,
    ): Response<User>

    //Работы
    @GET("my/jobs")
    suspend fun getAllJobs(): Response<List<JobEntity>>

    @GET("{user_id}/jobs")
    suspend fun getUsersJobsById(@Path("id") id: Long): Response<List<JobEntity>>

    @DELETE("my/jobs/{job_id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    //Собыия
    @GET("events")
    suspend fun getAllEvent(): Response<List<Event>>

    @GET("events/{event_id}/newer")
    suspend fun getNewerEvent(@Path("id") id: Long): Response<List<Event>>

    @GET("events/{event_id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @GET("events/{event_id}/before")
    suspend fun getEventBefore(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("events/{event_id}/after")
    suspend fun getEventAfter(
        @Path("id") id: Long,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("events/latest")
    suspend fun getEventLatest(@Query("count") count: Int): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("events/{event_id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @Query1(
        """
                UPDATE events SET
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id AND likedByMe=:likedByMe
                """
    )
    suspend fun likeEventById(
        @Path("id") id: Long,
        @Path("likedByMe") likedByMe: Boolean,
    ): Response<Event>

    @POST("events/{event_id}/participants")
    suspend fun addParticipantEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{event_id}/participants")
    suspend fun removeParticipantEvent(@Path("id") id: Long): Response<Event>

    //Стена

    @GET("{ userId } / wall")
    suspend fun getWall(@Path("userId") userId: Long): Response<List<FeedItem>>

    @GET("{ userId } / wall / latest")
    suspend fun getWallLatest(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<FeedItem>>

    @GET("{userId}/wall/{id } / after")
    suspend fun getWallAfter(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<FeedItem>>

    @GET("{ userId } / wall / { id } / before")
    suspend fun getWallBefore(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<FeedItem>>

    @GET("{ userId } / wall / { id } / newer")
    suspend fun getNewerWall(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<FeedItem>>
}

/* @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>*/

