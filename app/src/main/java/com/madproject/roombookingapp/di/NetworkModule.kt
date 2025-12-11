package com.madproject.roombookingapp.di

import com.madproject.roombookingapp.data.remote.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.madproject.roombookingapp.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpCookie
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCookieManager(): CookieManager {
        return CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
    }

    @Provides
    @Singleton
    fun provideCookieJar(cookieManager: CookieManager): CookieJar {
        return object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val uri = url.toUri()
                cookies.forEach { cookie ->
                    val httpCookie = HttpCookie(cookie.name, cookie.value).apply {
                        domain = cookie.domain
                        path = cookie.path
                        secure = cookie.secure
                        isHttpOnly = cookie.httpOnly
                        if (cookie.persistent) {
                            maxAge = (cookie.expiresAt - System.currentTimeMillis()) / 1000
                        }
                    }
                    cookieManager.cookieStore.add(uri, httpCookie)
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val uri = url.toUri()
                val httpCookies = cookieManager.cookieStore.get(uri) ?: emptyList()
                return httpCookies.map { httpCookie ->
                    Cookie.Builder()
                        .name(httpCookie.name)
                        .value(httpCookie.value)
                        .domain(httpCookie.domain ?: url.host)
                        .path(httpCookie.path ?: "/")
                        .apply {
                            if (httpCookie.secure) secure()
                            if (httpCookie.isHttpOnly) httpOnly()
                            if (httpCookie.maxAge > 0) {
                                expiresAt(System.currentTimeMillis() + httpCookie.maxAge * 1000)
                            }
                        }
                        .build()
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieJar: CookieJar,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}