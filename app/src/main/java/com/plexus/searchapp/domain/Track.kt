package com.plexus.searchapp.domain

data class Track(
    val id: Long,
    val name: String,
    val artist: String,
    val album: String?,
    val genre: String?,
    val releaseDate: String?,
    val durationMillis: Long?,
    val artworkUrl: String?,
)
