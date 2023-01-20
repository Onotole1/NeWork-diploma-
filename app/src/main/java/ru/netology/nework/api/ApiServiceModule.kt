package ru.netology.nework.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nework.BuildConfig
import ru.netology.nework.auth.AppAuth
import javax.inject.Singleton

private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule {
    @Singleton
    @Provides
    fun provideEventApiService(auth: AppAuth): EventApiService {
        return retrofit(okhttp(loggingInterceptor(), authInterceptor(auth)))
            .create(EventApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideJobApiService(auth: AppAuth): JobApiService {
        return retrofit(okhttp(loggingInterceptor(), authInterceptor(auth)))
            .create(JobApiService::class.java)
    }

    @Singleton
    @Provides
    fun providePostApiService(auth: AppAuth): PostApiService {
        return retrofit(okhttp(loggingInterceptor(), authInterceptor(auth)))
            .create(PostApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideUserApiService(): UserApiService {
        return retrofit(okhttp(loggingInterceptor()))
            .create(UserApiService::class.java)
    }
}
fun loggingInterceptor() = HttpLoggingInterceptor()
    .apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

fun authInterceptor(auth: AppAuth) = fun(chain: Interceptor.Chain): Response {
    auth.authStateFlow.value.token?.let { token ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", token.toString())
            .build()
        return chain.proceed(newRequest)
    }

    return chain.proceed(chain.request())
}



fun okhttp(vararg interceptors: Interceptor): OkHttpClient = OkHttpClient.Builder()
    .apply {
        interceptors.forEach {
            this.addInterceptor(it)
        }
    }
    .build()

fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()
