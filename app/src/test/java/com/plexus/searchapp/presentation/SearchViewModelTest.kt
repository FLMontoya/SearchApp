package com.plexus.searchapp.presentation

import com.plexus.searchapp.domain.SearchTracksUseCase
import com.plexus.searchapp.domain.Track
import com.plexus.searchapp.fakes.FakeSearchRepository
import com.plexus.searchapp.fakes.track
import com.plexus.searchapp.presentation.search.SearchUiState
import com.plexus.searchapp.presentation.search.SearchViewModel
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val debounce = 400L
    private val repository = FakeSearchRepository()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = SearchViewModel(SearchTracksUseCase(repository), debounce)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.pastDebounce() {
        advanceTimeBy(debounce + 1)
        runCurrent()
    }

    @Test
    fun `starts idle and a blank query stays idle without searching`() = runTest {
        viewModel.onQueryChange("   ")
        pastDebounce()

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.searchedTerms)
    }

    @Test
    fun `typing searches after the debounce and shows the results`() = runTest {
        val results = listOf(track(1))
        repository.onSearch = { results }

        viewModel.onQueryChange("jack")
        advanceTimeBy(debounce - 1)
        runCurrent()
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)

        pastDebounce()
        assertEquals(SearchUiState.Success(results), viewModel.uiState.value)
    }

    @Test
    fun `search action runs immediately`() = runTest {
        repository.onSearch = { listOf(track(1)) }

        viewModel.onQueryChange("jack")
        viewModel.onSearch()
        runCurrent()

        assertEquals(SearchUiState.Success(listOf(track(1))), viewModel.uiState.value)
    }

    @Test
    fun `no results shows the empty state`() = runTest {
        viewModel.onQueryChange("zzz")
        pastDebounce()

        assertEquals(SearchUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `a failure shows the error state`() = runTest {
        repository.onSearch = { throw IOException("offline") }

        viewModel.onQueryChange("jack")
        pastDebounce()

        assertEquals(SearchUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `a new search cancels the previous one and old results never show`() = runTest {
        val pending = mutableMapOf<String, CompletableDeferred<List<Track>>>()
        repository.onSearch = { query -> CompletableDeferred<List<Track>>().also { pending[query] = it }.await() }

        viewModel.onQueryChange("a")
        pastDebounce()
        assertEquals(SearchUiState.Loading, viewModel.uiState.value)

        viewModel.onQueryChange("ab")
        runCurrent()
        // Cancelling the first search must not surface as an error.
        assertEquals(SearchUiState.Loading, viewModel.uiState.value)
        pastDebounce()

        pending.getValue("ab").complete(listOf(track(2)))
        runCurrent()
        pending.getValue("a").complete(listOf(track(1)))
        runCurrent()

        assertEquals(SearchUiState.Success(listOf(track(2))), viewModel.uiState.value)
    }

    @Test
    fun `clearing the query cancels the in flight search`() = runTest {
        val pending = CompletableDeferred<List<Track>>()
        repository.onSearch = { pending.await() }

        viewModel.onQueryChange("a")
        pastDebounce()
        viewModel.onQueryChange("")
        pending.complete(listOf(track(1)))
        runCurrent()

        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }
}
