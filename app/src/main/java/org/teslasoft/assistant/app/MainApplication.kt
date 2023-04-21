/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
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
import com.google.android.material.color.DynamicColors

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
    }
}