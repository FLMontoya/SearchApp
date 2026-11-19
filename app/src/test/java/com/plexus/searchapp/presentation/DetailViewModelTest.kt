package com.plexus.searchapp.presentation

import com.plexus.searchapp.domain.GetTrackUseCase
import com.plexus.searchapp.fakes.FakeSearchRepository
import com.plexus.searchapp.fakes.track
import com.plexus.searchapp.presentation.detail.DetailUiState
import com.plexus.searchapp.presentation.detail.DetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val repository = FakeSearchRepository()

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts loading and then shows the track`() = runTest {
        repository.tracks = mapOf(7L to track(7))
        val viewModel = DetailViewModel(7, GetTrackUseCase(repository))
        assertEquals(DetailUiState.Loading, viewModel.uiState.value)

        runCurrent()

        assertEquals(DetailUiState.Loaded(track(7)), viewModel.uiState.value)
    }

    @Test
    fun `an unknown id shows not found`() = runTest {
        val viewModel = DetailViewModel(99, GetTrackUseCase(repository))

        runCurrent()

        assertEquals(DetailUiState.NotFound, viewModel.uiState.value)
    }
}
