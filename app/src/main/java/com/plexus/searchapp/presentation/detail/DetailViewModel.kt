package com.plexus.searchapp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexus.searchapp.domain.GetTrackUseCase
import com.plexus.searchapp.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Loaded(val track: Track) : DetailUiState
    data object NotFound : DetailUiState
}

class DetailViewModel(
    trackId: Long,
    getTrack: GetTrackUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val track = getTrack(trackId)
            _uiState.value = if (track == null) DetailUiState.NotFound else DetailUiState.Loaded(track)
        }
    }
}
