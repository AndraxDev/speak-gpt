/**************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.preferences

import android.content.Context
import android.content.SharedPreferences

class GlobalPreferences private constructor(private var gp: SharedPreferences) {
    companion object {
        private var preferences: GlobalPreferences? = null
        fun getPreferences(context: Context) : GlobalPreferences {
            if (preferences == null) preferences = GlobalPreferences(context.getSharedPreferences("settings", Context.MODE_PRIVATE))

            return preferences!!
        }
    }

    /**
     * Get amoled pitch black mode
     *
     * @return amoled pitch black mode
     * */
    fun getAmoledPitchBlack() : Boolean {
        return gp.getBoolean("amoled_pitch_black", false)
    }
}
