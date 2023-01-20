package ru.netology.nework.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.JobEntity

@Dao
interface  JobDao {
    @Query("SELECT * FROM jobs ORDER BY start DESC")
    fun getAll(): LiveData<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun getById(id: Long): JobEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun removeById(id: Long)

   // @Query("SELECT *  FROM jobs WHERE ownedId = :ownedId")
   // suspend fun getJobByUserId(ownedId: Long): Flow<List<JobEntity>>

    @Query("DELETE FROM jobs")
    suspend fun removeAll()

}