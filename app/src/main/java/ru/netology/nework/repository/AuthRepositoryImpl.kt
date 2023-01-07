package ru.netology.nework.repository

import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryimpl@Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
):AuthRepository {

    override suspend fun authUser(login: String, password: String): User {
        try {
            val response = apiService.updateUser(login, password)
            if (!response.isSuccessful) {
                println("authorized")
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw Exception()
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registrationUser(login: String, password: String, name: String): User {
        try {
            val response = apiService.registrationUser(login, password, name)
            if (!response.isSuccessful) {
                println("register")
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}