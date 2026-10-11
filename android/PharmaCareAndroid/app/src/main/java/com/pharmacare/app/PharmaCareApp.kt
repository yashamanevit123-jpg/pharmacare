package com.pharmacare.app

import android.app.Application

class PharmaCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application-level singletons are initialized lazily
    }
}
