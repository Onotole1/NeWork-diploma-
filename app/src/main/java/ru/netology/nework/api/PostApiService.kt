package ru.netology.nework.api

import androidx.room.Query
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Post

interface PostApiService:ApiService  {
    @GET("posts")
    suspend fun getAllPost(): Response<List<Post>>

    @GET("posts/{post_id}/newer")
    suspend fun getNewerPost(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{post_id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @GET("posts/{post_id}/before")
    suspend fun getPostBefore(
        @Path("id") id: Long,
        @retrofit2.http.Query("count") count: Int,
    ): Response<List<Post>>

    @GET("posts/{post_id}/after")
    suspend fun getPostAfter(
        @Path("id") id: Long,
        @retrofit2.http.Query("count") count: Int,
    ): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getPostLatest(@retrofit2.http.Query("count") count: Int): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("posts/{post_id}")
    suspend fun removePostById(@Path("id") id: Long): Response<Unit>

    @Query(
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


}
/* @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>*/