package com.plexus.searchapp.presentation.search

import com.plexus.searchapp.domain.Track

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val tracks: List<Track>) : SearchUiState
    data object Empty : SearchUiState
    data object Error : SearchUiState
}
