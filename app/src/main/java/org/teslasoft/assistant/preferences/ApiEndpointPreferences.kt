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

package org.teslasoft.assistant.preferences

import android.content.Context
import android.content.SharedPreferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.util.Hash

class ApiEndpointPreferences private constructor(private var preferences: SharedPreferences) {
    companion object {
        private var apiEndpointPreferences: ApiEndpointPreferences? = null

        fun getApiEndpointPreferences(context: Context): ApiEndpointPreferences {
            if (apiEndpointPreferences == null) {
                apiEndpointPreferences = ApiEndpointPreferences(context.getSharedPreferences("api_endpoint", Context.MODE_PRIVATE))
            }

            return apiEndpointPreferences!!
        }
    }

    private var listeners: ArrayList<OnApiEndpointChangeListener> = ArrayList()

    fun addOnApiEndpointChangeListener(listener: OnApiEndpointChangeListener) {
        listeners.add(listener)
    }

    fun getString(key: String, defValue: String): String {
        return preferences.getString(key, defValue)!!
    }

    fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun getApiEndpoint(context: Context, id: String): ApiEndpointObject {
        val label = getString(id + "_label", "")
        val host = getString(id + "_host", "")
        val apiKey: String = EncryptedPreferences.getEncryptedPreference(context, "api_endpoint", id + "_api_key")

        return ApiEndpointObject(label, host, apiKey)
    }

    fun deleteApiEndpoint(context: Context, id: String) {
        preferences.edit().remove(id + "_label").apply()
        preferences.edit().remove(id + "_host").apply()
        EncryptedPreferences.setEncryptedPreference(context, "api_endpoint", id + "_api_key", "null")

        for (listener in listeners) {
            listener.onApiEndpointChange()
        }
    }

    fun setApiEndpoint(context: Context, endpoint: ApiEndpointObject) {
        val id = Hash.hash(endpoint.label)
        putString(id + "_label", endpoint.label)
        putString(id + "_host", endpoint.host)
        EncryptedPreferences.setEncryptedPreference(context, "api_endpoint", id + "_api_key", endpoint.apiKey)

        for (listener in listeners) {
            listener.onApiEndpointChange()
        }
    }

    fun editEndpoint(context: Context, label: String, endpoint: ApiEndpointObject) {
        deleteApiEndpoint(context, Hash.hash(label))
        setApiEndpoint(context, endpoint)
    }

    fun migrateFromLegacyEndpoint(context: Context) {
        if (getApiEndpointsList(context).isEmpty()) {
            val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val label = "Default"
            val host = sp.getString("custom_host", "https://api.openai.com/v1/")
            val apiKey: String = EncryptedPreferences.getEncryptedPreference(context, "api", "api_key")

            setApiEndpoint(context, ApiEndpointObject(label, host!!, apiKey))
        }
    }

    fun getApiEndpointsList(context: Context): ArrayList<ApiEndpointObject> {
        val list = ArrayList<ApiEndpointObject>()
        for (key in preferences.all.keys) {
            if (key.contains("_label")) {
                val id = key.replace("_label", "")
                val label = getString(id + "_label", "")
                val host = getString(id + "_host", "")
                val apiKey: String = EncryptedPreferences.getEncryptedPreference(context, "api_endpoint", id + "_api_key")
                list.add(ApiEndpointObject(label, host, apiKey))
            }
        }

        // R8 bug fix
        if (list == null) {
            return ArrayList<ApiEndpointObject>()
        }

        return list
    }

    fun interface OnApiEndpointChangeListener {
        fun onApiEndpointChange()
    }
}