package com.selfgrowthfund.sgf

import android.app.Application
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SGFApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Initialize ThreeTenABP for LocalDate/Instant support on API < 26
        AndroidThreeTen.init(this)
    }
}
