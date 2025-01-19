/*******************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.teslasoft.core.auth

import android.annotation.SuppressLint

class SystemInfo {
    companion object {
        /********************************************************************************************
         * Version code explanation
         * First 3 digits represent the major version, the last digit represents the build flavor
         * Build flavors:
         * 1XX - Stable
         * 2XX - Beta
         * 3XX - Alpha
         * 4XX - Dev
         * 5XX - Canary
         * 6XX - Nightly
         * where X is the custom build parameter
         * Custom params:
         * 00 - Main build (Maven release)
         * 01 - SpeakGPT build
         ********************************************************************************************/

        const val NAME = "Teslasoft ID for SpeakGPT"
        const val MODIFIER = "-speakgpt"
        const val VERSION_CODE = 148101
        const val MAJOR_VERSION = VERSION_CODE / 100000
        const val MINOR_VERSION = VERSION_CODE % 100000 / 10000
        const val PATCH_VERSION = VERSION_CODE % 10000 / 1000

        @SuppressLint("DefaultLocale")
        val VERSION = String.format("%d.%d.%d%s", MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION, MODIFIER)
        val VERSION_STRING = "$NAME: $VERSION ($VERSION_CODE)"
    }
}
