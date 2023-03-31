package com.bcgg.service

import com.bcgg.network.NaverMapKey
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStreamReader
import java.io.Reader

object RetrofitServiceLoader {
    internal val naverMapKeyFile = File("src/main/resources/naver_map_key.json")
    private val naverMapKey =
        Gson().fromJson(InputStreamReader(naverMapKeyFile.inputStream()), NaverMapKey::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor{ chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", naverMapKey.clientId)
                .addHeader("X-NCP-APIGW-API-KEY", naverMapKey.clientSecret)
                .build()

            chain.proceed(request)
        }
        .build()

    internal val naverMapRetrofit = Retrofit.Builder()
        .baseUrl("https://naveropenapi.apigw.ntruss.com")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()


}

fun main() {
    if (RetrofitServiceLoader.naverMapKeyFile.exists()) {
        println("Naver map key file is exists")
    } else {
        println("Naver map key file is not exists")
    }
}