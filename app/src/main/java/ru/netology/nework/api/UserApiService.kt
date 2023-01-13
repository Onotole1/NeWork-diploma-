package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.AuthState
import ru.netology.nework.dto.PushToken
import ru.netology.nework.dto.User
import ru.netology.nework.entity.UserEntity

interface UserApiService :ApiService {

        //Юзеры
        @GET("users")
        suspend fun getAllUsers(): Response<List<UserEntity>>

        @GET("users/{user_id}")
        suspend fun getUserById(@Path("id") id: Long): Response<User>

        @FormUrlEncoded
        @POST("users/authentication")
        suspend fun updateUser(
            @Field("login") login: String,
            @Field("pass") pass: String,
        ): Response<AuthState>

        @FormUrlEncoded
        @POST("users/registration")
        suspend fun registrationUser(
            @Field("login") login: String,
            @Field("pass") pass: String,
            @Field("name") name: String,
        ): Response<AuthState>

        @Multipart
        @POST("users/registration")
        suspend fun registrationUserWithAvatar(
            @Part("login") login: RequestBody,
            @Part("pass") pass: RequestBody,
            @Part("name") name: RequestBody,
            @Part media: MultipartBody.Part
        ): Response<AuthState>

        @POST("users/push-tokens")
        suspend fun sendPushToken(@Body pushToken: PushToken): Response<Unit>
    }

