
package com.example.neurology_project_android

import android.app.Application
import com.meta.wearable.dat.core.Wearables
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    // The body can be empty. Hilt uses this annotation for setup.
    override fun onCreate() {
        super.onCreate()
        Wearables.initialize(this)
    }
}