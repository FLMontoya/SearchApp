package com.plexus.searchapp.data

import com.plexus.searchapp.data.cache.Clock
import com.plexus.searchapp.data.cache.InMemoryTtlCache
import com.plexus.searchapp.data.remote.ItunesApi
import com.plexus.searchapp.data.remote.dto.SearchResponseDto
import com.plexus.searchapp.data.remote.dto.TrackDto
import com.plexus.searchapp.data.repository.SearchRepositoryImpl
import com.plexus.searchapp.domain.Track
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test

private class FakeClock(var now: Long = 0) : Clock {
    override fun nowMillis() = now

    fun advance(millis: Long) {
        now += millis
    }
}

private class FakeItunesApi : ItunesApi {
    val requestedTerms = mutableListOf<String>()
    var results: List<TrackDto> = emptyList()
    var failure: Throwable? = null

    override suspend fun search(term: String, media: String, entity: String, limit: Int): SearchResponseDto {
        requestedTerms += term
        failure?.let { throw it }
        return SearchResponseDto(results)
    }
}

private fun dto(id: Long) = TrackDto(trackId = id, trackName = "Song $id", artistName = "Artist")

class SearchRepositoryImplTest {
    private val ttl = TimeUnit.MINUTES.toMillis(3)
    private val clock = FakeClock()
    private val api = FakeItunesApi()
    private val repository = SearchRepositoryImpl(api, InMemoryTtlCache<String, List<Track>>(ttl, clock))

    @Test
    fun `miss calls the network and maps the results`() = runTest {
        api.results = listOf(dto(1), dto(2))

        val tracks = repository.search("jack")

        assertEquals(listOf(1L, 2L), tracks.map { it.id })
        assertEquals(listOf("jack"), api.requestedTerms)
    }

    @Test
    fun `hit within the TTL does not call the network`() = runTest {
        api.results = listOf(dto(1))
        val first = repository.search("jack")

        clock.advance(ttl - 1)
        val second = repository.search("  JACK ")

        assertEquals(first, second)
        assertEquals(1, api.requestedTerms.size)
    }

    @Test
    fun `duplicate track ids are removed`() = runTest {
        api.results = listOf(dto(1), dto(1), dto(2))

        assertEquals(listOf(1L, 2L), repository.search("jack").map { it.id })
    }

    @Test
    fun `a different query is a miss`() = runTest {
        repository.search("jack")
        repository.search("johnson")

        assertEquals(listOf("jack", "johnson"), api.requestedTerms)
    }

    @Test
    fun `expired entry goes to the network and refreshes the cache`() = runTest {
        api.results = listOf(dto(1))
        repository.search("jack")

        clock.advance(ttl)
        api.results = listOf(dto(2))
        val refreshed = repository.search("jack")

        assertEquals(listOf(2L), refreshed.map { it.id })
        assertEquals(2, api.requestedTerms.size)

        // The refreshed entry is valid again for a full TTL.
        clock.advance(ttl - 1)
        assertEquals(listOf(2L), repository.search("jack").map { it.id })
        assertEquals(2, api.requestedTerms.size)
    }

    @Test
    fun `a failed request is not cached`() = runTest {
        api.failure = IOException("offline")
        try {
            repository.search("jack")
            fail("expected IOException")
        } catch (e: IOException) {
            // expected
        }

        api.failure = null
        api.results = listOf(dto(1))
        assertEquals(listOf(1L), repository.search("jack").map { it.id })
        assertEquals(2, api.requestedTerms.size)
    }

    @Test
    fun `a track stays resolvable after its query expires`() = runTest {
        api.results = listOf(dto(1))
        repository.search("jack")

        clock.advance(ttl * 2)

        assertNotNull(repository.getTrack(1))
    }
}
