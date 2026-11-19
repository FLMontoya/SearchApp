package com.plexus.searchapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.plexus.searchapp.di.AppContainer
import com.plexus.searchapp.presentation.detail.DetailScreen
import com.plexus.searchapp.presentation.detail.DetailViewModel
import com.plexus.searchapp.presentation.search.SearchScreen
import com.plexus.searchapp.presentation.search.SearchViewModel

private const val SEARCH_ROUTE = "search"
private const val TRACK_ID_ARG = "trackId"
private const val DETAIL_ROUTE = "detail/{$TRACK_ID_ARG}"

@Composable
fun AppNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SEARCH_ROUTE) {
        composable(SEARCH_ROUTE) {
            val viewModel: SearchViewModel = viewModel(
                factory = viewModelFactory { initializer { SearchViewModel(container.searchTracks) } },
            )
            SearchScreen(
                viewModel = viewModel,
                // singleTop: a double tap must not stack two detail screens.
                onTrackClick = { id -> navController.navigate("detail/$id") { launchSingleTop = true } },
            )
        }
        composable(
            route = DETAIL_ROUTE,
            arguments = listOf(navArgument(TRACK_ID_ARG) { type = NavType.LongType }),
        ) { entry ->
            val trackId = checkNotNull(entry.arguments).getLong(TRACK_ID_ARG)
            val viewModel: DetailViewModel = viewModel(
                factory = viewModelFactory { initializer { DetailViewModel(trackId, container.getTrack) } },
            )
            DetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
