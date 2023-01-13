package ru.netology.nework.api

import androidx.room.Query
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.JobEntity

interface JobApiService:ApiService {

    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<JobEntity>>

    @GET("{user_id}/jobs")
    suspend fun getJobsByUserId(@Path("id") id: Long): Response<List<JobEntity>>


    @DELETE("my/jobs/{job_id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>
}