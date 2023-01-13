package ru.netology.nework.dao


interface FeedRemoteKeyDao {
    suspend fun isEmpty(): Boolean
    suspend fun max(): Long?
    suspend fun min(): Long?
    suspend fun removeAll()

}