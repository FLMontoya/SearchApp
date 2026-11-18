package com.plexus.searchapp.domain

class SearchTracksUseCase(private val repository: SearchRepository) {
    suspend operator fun invoke(query: String): List<Track> {
        val term = query.trim()
        if (term.isEmpty()) return emptyList()
        return repository.search(term)
    }
}
