package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Job


interface JobApiService:ApiService {

    @GET("my/jobs")
    suspend fun getCurrentMyJobs(): Response<List<Job>>

    @GET("{user_id}/jobs")
    suspend fun getJobsByUserId(@Path("user_id") ownedId: Long): Response<List<Job>>


    @DELETE("my/jobs/{job_id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>
}