package com.plexus.searchapp.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.plexus.searchapp.R
import com.plexus.searchapp.domain.Track

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Long) -> Unit,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchContent(
        query = query,
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onTrackClick = onTrackClick,
    )
}

@Composable
private fun SearchContent(
    query: String,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Long) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.search_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .semantics { heading() },
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.search_label)) },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.search_clear),
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch()
                        focusManager.clearFocus()
                    },
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            SearchResults(
                state = state,
                query = query,
                onTrackClick = onTrackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState,
    query: String,
    onTrackClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SearchUiState.Success -> LazyColumn(modifier = modifier) {
            items(state.tracks, key = { it.id }) { track ->
                TrackRow(track = track, onClick = { onTrackClick(track.id) })
                HorizontalDivider()
            }
        }

        // Status messages live in a polite live region so TalkBack announces each change.
        else -> Box(
            modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.TopCenter,
        ) {
            when (state) {
                SearchUiState.Idle -> StatusMessage(stringResource(R.string.search_idle))
                SearchUiState.Loading -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.search_loading),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                SearchUiState.Empty -> StatusMessage(stringResource(R.string.search_empty, query.trim()))
                SearchUiState.Error -> StatusMessage(
                    text = stringResource(R.string.search_error),
                    isError = true,
                )

                is SearchUiState.Success -> Unit
            }
        }
    }
}

@Composable
private fun StatusMessage(text: String, isError: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(24.dp),
    ) {
        // The message text carries the meaning; the icon is decorative.
        if (isError) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit) {
    // clickable merges descendants, so TalkBack reads each row as a single item.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        AsyncImage(
            model = track.artworkUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.small),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
