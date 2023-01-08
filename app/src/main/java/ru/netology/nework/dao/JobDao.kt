package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.JobEntity

@Dao
interface  JobDao:FeedDao {
    @Query("SELECT * FROM jobs ORDER BY start DESC")
    fun getAll(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun getById(id: Long): JobEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE id = :id")
    override suspend fun removeById(id: Long)

    @Query("DELETE FROM jobs")
    override suspend fun removeAll()

    @Query("SELECT COUNT(*) == 0 FROM jobs")
    override suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM jobs")
    override suspend fun count(): Int
}