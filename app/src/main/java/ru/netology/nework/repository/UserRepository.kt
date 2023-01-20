package ru.netology.nework.repository

import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.UserApiService
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.User
import ru.netology.nework.entity.toDto
import ru.netology.nework.error.*
import java.io.IOException
import javax.inject.Inject

class UserRepository@Inject constructor(
    private val apiService: UserApiService,
    private val userDao: UserDao,
) {
    val data: Flow<List<User>> = userDao.getAll().map {
        it.toDto()
    }.flowOn(Dispatchers.Default)


    suspend fun getUserById(id: Long) : User{
        try {
            val response = apiService.getUserById(id)
            checkResponse(response)
            return response.body() ?: throw Exception()
        }catch(e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun getAllUsers() {
            try {
                val response = apiService.getAllUsers()
                checkResponse(response)
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                userDao.insert(body)
            } catch (e: ApiException) {
                throw e
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }

