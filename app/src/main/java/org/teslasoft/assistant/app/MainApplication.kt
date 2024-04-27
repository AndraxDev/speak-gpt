/**************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

package org.teslasoft.assistant.app

import android.app.Application

import cat.ereza.customactivityoncrash.config.CaocConfig

import com.google.android.material.color.DynamicColors
import org.conscrypt.Conscrypt

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Logger
import java.security.Security

/**
 * Called when the application is starting up. This method is responsible for setting up
 * the app and any necessary components.
 *
 * This implementation calls the [onCreate] method of the superclass [Application] and
 * applies dynamic colors to the activities of the app using the [DynamicColors] class.
 *
 * @see [Application.onCreate]
 * @see [DynamicColors.applyToActivitiesIfAvailable]
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        }

        // Clear event log on startup
        Logger.clearEventLog(this)

        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
            .enabled(true)
            .showErrorDetails(true)
            .showRestartButton(false)
            .logErrorOnRestart(true)
            .trackActivities(true)
            .minTimeBetweenCrashesMs(3000)
            .errorDrawable(R.mipmap.ic_launcher_round)
            .restartActivity(null)
            .errorActivity(org.teslasoft.core.CrashHandlerActivity::class.java)
            .eventListener(null)
            .customCrashDataCollector(null)
            .apply()
    }
}
