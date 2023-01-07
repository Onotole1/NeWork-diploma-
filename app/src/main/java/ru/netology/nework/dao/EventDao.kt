package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.EventEntity

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE events SET likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END WHERE id = :id AND likedByMe=:likedByMe")
    suspend fun likeById(id: Long, likedByMe: Boolean)

    @Query("UPDATE events SET participatedByMe=CASE WHEN participatedByMe THEN 0 ELSE 1 END WHERE id = :id AND participatedByMe=:participatedByMe")
    suspend fun participateById(id: Long,participatedByMe: Boolean)
}