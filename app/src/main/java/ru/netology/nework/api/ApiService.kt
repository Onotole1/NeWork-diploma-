package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.netology.nework.dto.Media

interface  ApiService {
    //Медиа
    @Multipart
    @POST("media")
    suspend fun uploadMedia(
        @Part part: MultipartBody.Part,
    ): Response<Media>

}