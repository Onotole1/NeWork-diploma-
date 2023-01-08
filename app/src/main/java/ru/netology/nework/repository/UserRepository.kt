package ru.netology.nework.repository

import androidx.paging.PagingData
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.User
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.error.*
import java.io.IOException
import javax.inject.Inject

class UserRepository@Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
) {
    val data: Flow<List<User>> = userDao.getAll().map {
        it.toDto()
    }.flowOn(Dispatchers.Default)


    suspend fun getById(id: Long) : User{
        try {
            val response = apiService.getUserById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw Exception()
        }catch(e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun getAll() {
            try {
                val response = apiService.getAllUsers()
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                response.body() ?: throw ApiError(response.code(), response.message())
                userDao.getAll()
            } catch (e: ApiException) {
                throw e
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }
