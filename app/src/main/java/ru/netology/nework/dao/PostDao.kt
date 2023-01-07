package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.PostEntity

interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE authorId = :id ORDER BY id DESC")
    fun getPagingSource(id: Long): PagingSource<Int, PostEntity>

    @Query("UPDATE posts SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE posts SET likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END WHERE id = :id AND likedByMe=:likedByMe")
    suspend fun likeById(id: Long, likedByMe: Boolean)

    @Query("SELECT * FROM posts ORDER BY id DESC LIMIT 1")
    suspend fun getPostMaxId(): PostEntity?

    @Query("DELETE FROM posts")
    suspend fun removeAll()
}