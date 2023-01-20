package ru.netology.nework.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.ApiError2
import ru.netology.nework.repository.auth.AuthRepository
import ru.netology.nework.repository.auth.AuthRepositoryimpl
import ru.netology.nework.repository.event.EventRepository
import ru.netology.nework.repository.event.EventRepositoryImpl
import ru.netology.nework.repository.post.PostRepository
import ru.netology.nework.repository.post.PostRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(impl: JobRepository): JobRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryimpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindWallRepository(impl: WallRepositoryImpl): WallRepository
}

fun checkResponse(response: Response<out Any>) {
    if (!response.isSuccessful) {
        throw ApiError(response.code(), response.message())
    }
fun checkResponse2 (response: Response<out Any>) {
        if (!response.isSuccessful) {
            throw ApiError2(response.code(), response.message(), response.errorBody())
        }
    }
}