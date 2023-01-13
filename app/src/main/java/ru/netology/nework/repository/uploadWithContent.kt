package ru.netology.nework.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException

suspend fun uploadWithContent(upload: MediaUpload,apiService: ApiService,): Attachment {
    try {
        val media = MultipartBody.Part.createFormData("file",
            upload.file.name,
            upload.file.asRequestBody())

        val content = MultipartBody.Part.createFormData("content", "text")

        val response = apiService.uploadMedia(media, content)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        val mediaResponse = response.body() ?: throw ApiError(response.code(), response.message())
        return Attachment(mediaResponse.url, AttachmentType.IMAGE)
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}

