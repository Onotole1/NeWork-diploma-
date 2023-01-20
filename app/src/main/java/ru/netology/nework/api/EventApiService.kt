package ru.netology.nework.api

import androidx.room.Query
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event

interface EventApiService:ApiService {
    @GET("events")
    suspend fun getAllEvent(): Response<List<Event>>

    @GET("events/{event_id}/newer")
    suspend fun getNewerEvent(@Path("id") id: Long): Response<List<Event>>

    @GET("events/{event_id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @GET("events/{event_id}/before")
    suspend fun getEventBefore(
        @Path("id") id: Long,
        @retrofit2.http.Query("count") count: Int,
    ): Response<List<Event>>

    @GET("events/{event_id}/after")
    suspend fun getEventAfter(
        @Path("id") id: Long,
        @retrofit2.http.Query("count") count: Int,
    ): Response<List<Event>>

    @GET("events/latest")
    suspend fun getEventLatest(@retrofit2.http.Query("count") count: Int): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("events/{event_id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @Query(
        """
                UPDATE events SET
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id AND likedByMe=:likedByMe
                """
    )
    suspend fun likeEventById(
        @Path("id") id: Long,
        @Path("likedByMe") likedByMe: Boolean,
    ): Response<Event>

    @POST("events/{event_id}/participants")
    suspend fun addParticipantEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{event_id}/participants")
    suspend fun removeParticipantEvent(@Path("id") id: Long): Response<Event>

}