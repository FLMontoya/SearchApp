package com.plexus.searchapp.domain

import com.plexus.searchapp.fakes.FakeSearchRepository
import com.plexus.searchapp.fakes.track
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SearchTracksUseCaseTest {
    private val repository = FakeSearchRepository()

    @Test
    fun `blank query returns empty without calling the repository`() = runTest {
        val result = SearchTracksUseCase(repository)("   ")

        assertTrue(result.isEmpty())
        assertTrue(repository.searchedTerms.isEmpty())
    }

    @Test
    fun `query is trimmed before reaching the repository`() = runTest {
        repository.onSearch = { listOf(track(1)) }

        val result = SearchTracksUseCase(repository)("  jack  ")

        assertEquals(listOf("jack"), repository.searchedTerms)
        assertEquals(listOf(track(1)), result)
    }

    @Test
    fun `repository errors propagate`() = runTest {
        repository.onSearch = { throw IOException("offline") }

        try {
            SearchTracksUseCase(repository)("jack")
            fail("expected IOException")
        } catch (e: IOException) {
            assertEquals("offline", e.message)
        }
    }

    @Test
    fun `get track delegates to the repository`() = runTest {
        repository.tracks = mapOf(7L to track(7))
        val getTrack = GetTrackUseCase(repository)

        assertEquals(track(7), getTrack(7))
        assertNull(getTrack(8))
    }
}
