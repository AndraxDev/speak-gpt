package org.teslasoft.assistant.app

import android.app.Application
import com.google.android.material.color.DynamicColors

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}