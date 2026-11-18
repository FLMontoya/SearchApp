package com.plexus.searchapp.domain

class GetTrackUseCase(private val repository: SearchRepository) {
    suspend operator fun invoke(id: Long): Track? = repository.getTrack(id)
}
