package com.plexus.searchapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexus.searchapp.domain.SearchTracksUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracks: SearchTracksUseCase,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(text: String) {
        _query.value = text
        search(text, debounceMillis)
    }

    fun onSearch() = search(_query.value, 0)

    // Cancelling the previous job guarantees a stale response never reaches the UI.
    // Loading is set before the debounce so results of the previous query are never
    // shown next to the new text.
    private fun search(text: String, delayMillis: Long) {
        searchJob?.cancel()
        if (text.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            delay(delayMillis)
            _uiState.value = try {
                val tracks = searchTracks(text)
                if (tracks.isEmpty()) SearchUiState.Empty else SearchUiState.Success(tracks)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SearchUiState.Error
            }
        }
    }

    private companion object {
        const val DEFAULT_DEBOUNCE_MILLIS = 400L
    }
}
