package com.plexus.searchapp.fakes

import com.plexus.searchapp.domain.SearchRepository
import com.plexus.searchapp.domain.Track

fun track(id: Long, name: String = "Song $id") = Track(
    id = id,
    name = name,
    artist = "Artist",
    album = "Album",
    genre = "Pop",
    releaseDate = "2020-01-01T08:00:00Z",
    durationMillis = 185_000,
    artworkUrl = null,
)

class FakeSearchRepository : SearchRepository {
    val searchedTerms = mutableListOf<String>()
    var onSearch: suspend (String) -> List<Track> = { emptyList() }
    var tracks: Map<Long, Track> = emptyMap()

    override suspend fun search(query: String): List<Track> {
        searchedTerms += query
        return onSearch(query)
    }

    override suspend fun getTrack(id: Long): Track? = tracks[id]
}
