package ru.netology.nework.dao


interface FeedDao {

    suspend fun updateContentById(id: Long, content: String)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun isEmpty(): Boolean
    suspend fun removeAll()
    suspend fun count(): Int
}
