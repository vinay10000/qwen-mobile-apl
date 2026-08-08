package com.pushoff

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PushoffApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
