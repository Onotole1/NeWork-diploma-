package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.PostEntity

@Dao
interface  JobDao {

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun getJobById(id: Long): JobEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT *  FROM jobs WHERE ownedId = :userId")
    suspend fun getJobByUserId(userId: Long): PostEntity?

    @Query("DELETE FROM jobs")
    suspend fun removeAll()

    @Query("SELECT COUNT(*) == 0 FROM jobs")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun count(): Int
}