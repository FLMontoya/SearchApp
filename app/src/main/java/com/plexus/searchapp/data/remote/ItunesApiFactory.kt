package com.plexus.searchapp.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

const val ITUNES_BASE_URL = "https://itunes.apple.com/"

fun createItunesApi(
    baseUrl: String = ITUNES_BASE_URL,
    client: OkHttpClient = OkHttpClient(),
): ItunesApi {
    // iTunes answers with "text/javascript", but the converter only parses the body.
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ItunesApi::class.java)
}
