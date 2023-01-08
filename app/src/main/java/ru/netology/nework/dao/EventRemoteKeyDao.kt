package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.nework.entity.RemoteKeyEntity

@Dao
interface EventRemoteKeyDao:FeedRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM RemoteKeyEntity")
    override suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(id) FROM RemoteKeyEntity")
    override suspend fun max(): Long?

    @Query("SELECT MIN(id) FROM RemoteKeyEntity")
    override suspend fun min(): Long?

    @Insert(onConflict = REPLACE)
    suspend fun insert(key: RemoteKeyEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insert(keys: List<RemoteKeyEntity>)

    @Query("DELETE FROM RemoteKeyEntity ")
    override suspend fun removeAll()

}