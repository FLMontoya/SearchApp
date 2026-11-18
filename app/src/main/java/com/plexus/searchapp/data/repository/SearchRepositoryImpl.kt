package com.plexus.searchapp.data.repository

import com.plexus.searchapp.data.cache.InMemoryTtlCache
import com.plexus.searchapp.data.remote.ItunesApi
import com.plexus.searchapp.data.remote.dto.toDomain
import com.plexus.searchapp.domain.SearchRepository
import com.plexus.searchapp.domain.Track
import java.util.concurrent.ConcurrentHashMap

class SearchRepositoryImpl(
    private val api: ItunesApi,
    private val cache: InMemoryTtlCache<String, List<Track>>,
) : SearchRepository {

    // Lets the detail screen resolve a track after its query's TTL has expired.
    private val tracksById = ConcurrentHashMap<Long, Track>()

    override suspend fun search(query: String): List<Track> {
        val term = query.trim()
        val key = term.lowercase()
        cache.get(key)?.let { return it }

        val tracks = api.search(term).results.mapNotNull { it.toDomain() }
        cache.put(key, tracks)
        tracks.forEach { tracksById[it.id] = it }
        return tracks
    }

    override suspend fun getTrack(id: Long): Track? = tracksById[id]
}
