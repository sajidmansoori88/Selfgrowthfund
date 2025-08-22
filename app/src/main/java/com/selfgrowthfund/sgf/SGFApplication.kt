package com.selfgrowthfund.sgf

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SGFApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase only once here
        FirebaseApp.initializeApp(this)
    }
}
