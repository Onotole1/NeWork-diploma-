package ru.netology.nework.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.*
import ru.netology.nework.entity.*

@Database(entities = [
    PostEntity::class,
    PostRemoteKeyEntity::class,
    EventEntity::class,
    EventRemoteKeyEntity::class,
    UserEntity::class,
    JobEntity::class,
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao

    companion object{
        fun buildDatabase(context: Context): AppDb{
            lateinit var db: AppDb
            try {
                db = Room.databaseBuilder(context, AppDb::class.java, "app.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }catch (t: Throwable){
                println("DB construction error is $t")
            }
            return db
        }
    }
}