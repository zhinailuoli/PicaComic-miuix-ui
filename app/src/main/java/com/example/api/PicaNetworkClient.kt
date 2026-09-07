package com.example.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

class PicaNetworkClient(
    private val getBaseUrl: () -> String,
    private val getToken: () -> String?
) {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url
        val pathAndQuery = if (!url.encodedQuery.isNullOrBlank()) {
            "${url.encodedPath.removePrefix("/")}?${url.encodedQuery}"
        } else {
            url.encodedPath.removePrefix("/")
        }

        val time = System.currentTimeMillis() / 1000
        val nonce = UUID.randomUUID().toString().replace("-", "")
        val method = original.method
        val signature = PicaSignature.calculate(pathAndQuery, time, nonce, method)

        val builder = original.newBuilder()
            .header("api-key", PicaSignature.API_KEY)
            .header("app-version", "2.2.1.3.3.4")
            .header("app-uuid", "defaultUuid")
            .header("app-platform", "android")
            .header("app-build-version", "45")
            .header("app-channel", "2")
            .header("time", time.toString())
            .header("nonce", nonce)
            .header("signature", signature)
            .header("image-quality", "original")
            .header("Accept", "application/vnd.picacomic.com.v1+json")
            .header("User-Agent", "okhttp/3.8.1")

        val token = getToken()
        if (!token.isNullOrBlank()) {
            builder.header("authorization", token)
        }

        chain.proceed(builder.build())
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun createService(): PicaApiService {
        val rawUrl = getBaseUrl().trim().trimEnd('/') + "/"
        return Retrofit.Builder()
            .baseUrl(rawUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PicaApiService::class.java)
    }
}
