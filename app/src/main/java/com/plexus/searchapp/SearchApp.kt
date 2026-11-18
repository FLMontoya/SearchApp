package com.plexus.searchapp

import android.app.Application
import com.plexus.searchapp.di.AppContainer

class SearchApp : Application() {
    val container = AppContainer()
}
