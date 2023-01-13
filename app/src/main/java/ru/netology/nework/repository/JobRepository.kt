package ru.netology.nework.repository

import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.JobApiService
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dto.Job
import ru.netology.nework.entity.JobEntity

import ru.netology.nework.entity.toDto
import ru.netology.nework.error.*
import java.io.IOException
import javax.inject.Inject

class JobRepository @Inject constructor(
    private val apiService: JobApiService,
    private val jobDao: JobDao,
    private val  userId:Long
) {


    val data: Flow<List<Job>> = jobDao.getAll().map {
        it.toDto()
    }.flowOn(Dispatchers.Default)

    suspend fun getMyJobs() {
        try {
            val response = apiService.getMyJobs()
            checkResponse(response)
            response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.getAll()
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun saveJob(job: Job) {
        try {
            val response = apiService.saveJob(job)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeJobById(id)
            checkResponse(response)
            jobDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun getJobsByUserId(id: Long) {
        try {
            jobDao.removeAll()
            val response = apiService.getJobsByUserId(id)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: java.lang.Exception) {
            throw UnknownError()
        }
    }

    suspend fun getJobName(id: Long) =
        try {
            apiService.getJobsByUserId(id).body()?.map {
                it.name
            }
        } catch (e: IOException) {
            throw NetworkError
        }



}


