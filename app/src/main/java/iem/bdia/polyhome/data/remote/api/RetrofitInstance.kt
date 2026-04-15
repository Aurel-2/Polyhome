package iem.bdia.polyhome.data.remote.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var tryCount = 0
            val maxLimit = 3

            while (!response.isSuccessful && response.code == 500 && tryCount < maxLimit - 1) {
                tryCount++
                response.close()
                Thread.sleep(500)
                response = chain.proceed(request)
            }
            response
        }
        .build()
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://polyhome.lesmoulinsdudev.com/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}