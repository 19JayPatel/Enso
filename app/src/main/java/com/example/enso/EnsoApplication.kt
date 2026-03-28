package com.example.enso

import android.app.Application
import com.google.firebase.FirebaseApp

class EnsoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}