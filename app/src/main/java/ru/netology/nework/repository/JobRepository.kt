package ru.netology.nework.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import ru.netology.nework.api.JobApiService
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dto.Job
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.*

import java.io.IOException
import javax.inject.Inject

class JobRepository @Inject constructor(
    private val apiService: JobApiService,
    private val jobDao: JobDao,
) {


    private val _data = MutableLiveData<List<Job>>()

    val data: LiveData<List<Job>> = _data

    suspend fun getMyJobs() {
        try {
            jobDao.removeAll()
            val response = apiService.getCurrentMyJobs()
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            _data.postValue(body)
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun saveJob(job: Job, userId: Long) {
        try {
            val response = apiService.saveJob(job)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))
            _data.postValue(_data.value?.map {
                if (it.id != body.id) it else body.copy(ownerId = userId)
            })
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
            _data.postValue(_data.value?.filter {
                it.id == id
            })
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun getJobsByUserId(userId: Long) {
        try {
            val response = apiService.getJobsByUserId(userId)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body.toEntity())
            _data.postValue(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: java.lang.Exception) {
            throw UnknownError()
        }
    }

    suspend fun getJobsById(userId: Long) {
        try {
            val response = apiService.getJobsByUserId(userId)
            checkResponse(response)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body.toEntity())
            _data.postValue(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: java.lang.Exception) {
            throw UnknownError()
        }
    }

   /* suspend fun getJobName(id: Long):String {
        try {
            val job: List<Job>? = data.value?.filter {
                it.id == id
            }
            return job.name
        } catch (e: IOException) {
            throw NetworkError
        }
        return
    }*/



}


