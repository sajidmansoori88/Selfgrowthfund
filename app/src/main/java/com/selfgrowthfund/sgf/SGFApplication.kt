package com.selfgrowthfund.sgf

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.selfgrowthfund.sgf.data.repository.RealtimeSyncRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SGFApplication : Application() {

    @Inject
    lateinit var realtimeSyncRepository: RealtimeSyncRepository

    override fun onCreate() {
        super.onCreate()

        // 🔹 Initialize Firebase explicitly (for clarity)
        FirebaseApp.initializeApp(this)
        Timber.i("✅ FirebaseApp initialized for project: ${FirebaseApp.getInstance().options.projectId}")

        // 🔹 Enable Firestore offline persistence and verbose debug logging
        FirebaseFirestore.setLoggingEnabled(true)
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        Timber.i("✅ Firestore persistence enabled. Starting realtime sync listeners...")

        // 🔹 Start realtime listeners

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Timber.i("✅ Authenticated as ${currentUser.email}, starting realtime sync.")
                realtimeSyncRepository.startRealtimeSync()
            } else {
                Timber.w("⚠️ No authenticated user; delaying realtime sync.")
            }
        }


        // 🔹 Optionally push unsynced local data on app start
        CoroutineScope(Dispatchers.IO).launch {
            realtimeSyncRepository.pushAllUnsynced()
            Timber.i("🔁 Initial unsynced data push triggered.")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        realtimeSyncRepository.stopRealtimeSync()
        Timber.i("🛑 Realtime sync listeners stopped (app terminated).")
    }
}
