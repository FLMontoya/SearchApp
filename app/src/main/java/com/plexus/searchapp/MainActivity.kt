package com.plexus.searchapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.plexus.searchapp.presentation.navigation.AppNavGraph
import com.plexus.searchapp.presentation.theme.SearchAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SearchApp).container
        setContent {
            SearchAppTheme {
                AppNavGraph(container)
            }
        }
    }
}
