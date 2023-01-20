package ru.netology.nework.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.dao.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DbModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDb {
        return Room.databaseBuilder(context, AppDb::class.java,"app_db")
            .fallbackToDestructiveMigration()
            .build()
    }
  @Provides
fun providePostDao(
    appDb: AppDb
): PostDao = appDb.postDao()

@Provides
fun providePostRemoteKeyDao(
    appDb: AppDb
): PostRemoteKeyDao = appDb.postRemoteKeyDao()

@Provides
fun provideUserDao(
    appDb: AppDb
): UserDao = appDb.userDao()

@Provides
fun provideEventDao(
    appDb: AppDb
): EventDao = appDb.eventDao()

@Provides
fun provideEventRemoteKeyDao(
    appDb: AppDb
): EventRemoteKeyDao = appDb.eventRemoteKeyDao()

@Provides
fun provideJobDao(
    appDb: AppDb
): JobDao = appDb.jobDao()


@Provides
fun provideUserWallDao(
    appDb: AppDb
): UserDao = appDb.userDao()

}
