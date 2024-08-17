/*******************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.core.auth.client

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import org.teslasoft.core.auth.RequestNetwork
import org.teslasoft.core.auth.internal.Config.Companion.AUTH_SERVER

class TeslasoftIDClient(private val context: Activity, private val applicationSignature: String, private var apiKey: String, private var appId: String, private var settingsListener: SettingsListener?, private var syncListener: SyncListener?) {

    private var apiLoader: RequestNetwork? = null
    private var apiGetterListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            try {
                settingsListener?.onSuccess(message)
            } catch (e: Exception) {
                settingsListener?.onError("INTERNAL_ERROR", e.message.toString())
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            settingsListener?.onError("NO_INTERNET", "Failed to connect to the server. Please try again.")
        }
    }

    private var apiSetterListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            try {
                syncListener?.onSuccess()
            } catch (e: Exception) {
                syncListener?.onError("INTERNAL_ERROR", e.message.toString())
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            syncListener?.onError("NO_INTERNET", "Failed to connect to the server. Please try again.")
        }
    }

    init {
        apiLoader = RequestNetwork(context)
    }

    /**
     * Get account info.
     * */
    fun getAccount(): Map<String, String>? {
        val map: MutableMap<String, String> = mutableMapOf()

        val accountSettings = context.getSharedPreferences("account", FragmentActivity.MODE_PRIVATE)

        val uid: String? = accountSettings?.getString("user_id", null)
        val token: String? = accountSettings?.getString("token", null)

        if (uid == null) return null
        if (token == null) return null

        map["user_id"] = uid
        map["token"] = token

        return map
    }

    /**
     * Determine if user is signed in.
     * */
    fun doesUserSignedIn(): Boolean {
        val accountSettings = context.getSharedPreferences("account", FragmentActivity.MODE_PRIVATE)

        val uid: String? = accountSettings?.getString("user_id", null)
        val token: String? = accountSettings?.getString("token", null)

        return uid != null && token != null
    }

    /**
     * Retrieve app settings from the server.
     * */
    fun getAppSettings() {
        val account: Map<String, String> = getAccount()!!
        val url = "$AUTH_SERVER/GetSettings.php?s=$applicationSignature&k=$apiKey&appId=$appId&token=${account["token"]}&u=${account["user_id"]}"
        apiLoader?.startRequestNetwork("GET", url, "A", apiGetterListener)
    }

    /**
     * Send app settings to the server.
     *
     * @param settings JSON encoded settings.
     * */
    fun syncAppSettings(settings: String) {
        val account: Map<String, String> = getAccount()!!
        val url = "$AUTH_SERVER/SetSettings.php?s=$applicationSignature&k=$apiKey&appId=$appId&token=${account["token"]}&u=${account["user_id"]}&settings=$settings"
        apiLoader?.startRequestNetwork("GET", url, "A", apiSetterListener)
    }
}
