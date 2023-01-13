package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.*
import ru.netology.nework.entity.UserEntity

interface ApiService {
    //Медиа
    @Multipart
    @POST("media")
    suspend fun uploadMedia(
        @Part part: MultipartBody.Part,
    ): Response<Attachment>

}



