package ru.netology.nework.repository.auth

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.UserApiService
import ru.netology.nework.error.*
import ru.netology.nework.dto.MediaUploadFile
import ru.netology.nework.model.AuthState
import ru.netology.nework.repository.checkResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun updateUser(login: String, pass: String): AuthState
    suspend fun registrationUser(login: String, pass: String, name: String): AuthState
    suspend fun registrationUserWithAvatar(login: String, pass: String, name: String, upload: MediaUploadFile): AuthState
}
@Singleton
@JvmSuppressWildcards
class AuthRepositoryimpl@Inject constructor(
    private val apiService: UserApiService,
): AuthRepository {

    override suspend fun registrationUser(login: String, password: String, name: String): AuthState {
        try {
            val response = apiService.registrationUser(login, password, name)
            checkResponse(response)
            return (response.body() ?: throw Exception())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun registrationUserWithAvatar(
        login: String,
        password: String,
        name: String,
        upload: MediaUploadFile
    ): AuthState {
        try {

            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )
            val response = apiService.registrationUserWithAvatar(
                login.toRequestBody(),
                password.toRequestBody(),
                name.toRequestBody(),
                media
            )
            checkResponse(response)
            return response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    override suspend fun updateUser(login: String, password: String): AuthState {
            try {
                val response = apiService.updateUser(login, password)
                checkResponse(response)
                return response.body() ?: throw Exception()
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }

        }

}