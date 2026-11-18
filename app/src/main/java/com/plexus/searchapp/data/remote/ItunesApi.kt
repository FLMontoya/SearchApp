package com.plexus.searchapp.data.remote

import com.plexus.searchapp.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50,
    ): SearchResponseDto
}
