package com.plexus.searchapp.domain

interface SearchRepository {
    suspend fun search(query: String): List<Track>

    suspend fun getTrack(id: Long): Track?
}
