package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.PostEntity

@Dao
interface EventDao:FeedDao {
    @Query("SELECT * FROM events ORDER BY id DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :id")
    override suspend fun removeById(id: Long)

    @Query("UPDATE events SET likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END WHERE id = :id AND likedByMe=:likedByMe")
    override suspend fun likeById(id: Long, likedByMe: Boolean)

    @Query("UPDATE events SET participatedByMe=CASE WHEN participatedByMe THEN 0 ELSE 1 END WHERE id = :id AND participatedByMe=:participatedByMe")
    suspend fun participateById(id: Long,participatedByMe: Boolean)

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    suspend fun getEventPostMaxId(): EventEntity?

    @Query("DELETE FROM events")
    override suspend fun removeAll()

    @Query("UPDATE events SET content = :content WHERE id = :id")
    override suspend fun updateContentById(id: Long, content: String)

    suspend fun save(event: EventEntity) =
        if (event.id == 0L) insert(event) else updateContentById(event.id, event.content)

    @Query("SELECT COUNT(*) == 0 FROM events")
    override suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM events")
    override suspend fun count(): Int

}