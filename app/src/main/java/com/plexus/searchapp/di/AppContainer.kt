package com.plexus.searchapp.di

import com.plexus.searchapp.data.cache.InMemoryTtlCache
import com.plexus.searchapp.data.remote.createItunesApi
import com.plexus.searchapp.data.repository.SearchRepositoryImpl
import com.plexus.searchapp.domain.GetTrackUseCase
import com.plexus.searchapp.domain.SearchRepository
import com.plexus.searchapp.domain.SearchTracksUseCase
import java.util.concurrent.TimeUnit

/** Manual dependency injection: one instance per app process. */
class AppContainer {
    private val repository: SearchRepository = SearchRepositoryImpl(
        api = createItunesApi(),
        cache = InMemoryTtlCache(ttlMillis = TimeUnit.MINUTES.toMillis(3)),
    )

    val searchTracks = SearchTracksUseCase(repository)
    val getTrack = GetTrackUseCase(repository)
}
