package ru.netology.nework.repository

import android.widget.ImageView
import com.bumptech.glide.Glide
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.R
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.MediaUploadFile
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException

suspend fun uploadWithContent(upload: MediaUploadFile, apiService: ApiService,): Attachment {
    try {
        val media = MultipartBody.Part.createFormData("file",
            upload.file.name,
            upload.file.asRequestBody())

        val response = apiService.uploadMedia(media)
        checkResponse(response)
        val mediaResponse = response.body() ?: throw ApiError(response.code(), response.message())
        return Attachment(mediaResponse.uri, AttachmentType.IMAGE)
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}
object Utils {
    fun uploadingAvatar(view: ImageView, avatar: String?) {
        Glide.with(view)
            .load(avatar)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar)
            .timeout(10_000)
            .into(view)
    }

    fun uploadingMedia(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .timeout(10_000)
            .into(view)
    }
}