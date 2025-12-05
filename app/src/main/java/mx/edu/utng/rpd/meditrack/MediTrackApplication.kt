package mx.edu.utng.rpd.meditrack

import android.app.Application
import com.google.firebase.FirebaseApp

class MediTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}