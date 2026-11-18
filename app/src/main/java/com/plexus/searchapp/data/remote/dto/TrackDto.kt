package com.plexus.searchapp.data.remote.dto

import com.plexus.searchapp.domain.Track
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(val results: List<TrackDto> = emptyList())

@Serializable
data class TrackDto(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val primaryGenreName: String? = null,
    val releaseDate: String? = null,
    val trackTimeMillis: Long? = null,
    val artworkUrl100: String? = null,
)

/** Returns null for entries without the fields a track needs to be listed. */
fun TrackDto.toDomain(): Track? {
    val id = trackId ?: return null
    val name = trackName ?: return null
    return Track(
        id = id,
        name = name,
        artist = artistName.orEmpty(),
        album = collectionName,
        genre = primaryGenreName,
        releaseDate = releaseDate,
        durationMillis = trackTimeMillis,
        artworkUrl = artworkUrl100,
    )
}
