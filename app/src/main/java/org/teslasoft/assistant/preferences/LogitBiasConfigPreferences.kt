/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.util.Hash
import java.lang.Exception
import java.lang.reflect.Type

class LogitBiasConfigPreferences private constructor(private var preferences: SharedPreferences) {
    companion object {
        private var logitBiasConfigPreferences: LogitBiasConfigPreferences? = null

        fun getLogitBiasConfigPreferences(context: Context): LogitBiasConfigPreferences {
            if (logitBiasConfigPreferences == null) {
                logitBiasConfigPreferences = LogitBiasConfigPreferences(context.getSharedPreferences("logit_bias_config", Context.MODE_PRIVATE))
            }

            return logitBiasConfigPreferences!!
        }
    }

    private var listeners: ArrayList<OnLogitBiasConfigChangeListener> = ArrayList()

    fun addOnLogitBiasConfigChangeListener(listener: OnLogitBiasConfigChangeListener) {
        listeners.add(listener)
    }

    fun getString(key: String, defValue: String): String {
        return preferences.getString(key, defValue)!!
    }

    fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun addConfig(label: String) {
        val gson = Gson()

        var list: ArrayList<HashMap<String, String>> = getAllConfigs()

        // Bugfix for R8 minifier, yes It make no sense for regular programmer, but it's a bug in R8 minifier
        if (list == null) list = arrayListOf()

        list.add(hashMapOf("label" to label, "id" to Hash.hash(label)))

        // Bugfix for R8 minifier, yes It make no sense for regular programmer, but it's a bug in R8 minifier
        if (list == null) list = arrayListOf()

        putString("configs", gson.toJson(list))

        for (listener in listeners) {
            listener.onLogitBiasConfigChange()
        }
    }

    fun editConfig(oldLabel: String, newLabel: String) {
        deleteConfig(Hash.hash(oldLabel))
        addConfig(newLabel)
    }

    fun deleteConfig(configId: String) {
        val gson = Gson()

        var list: ArrayList<HashMap<String, String>> = getAllConfigs()

        // Bugfix for R8 minifier, yes It make no sense for regular programmer, but it's a bug in R8 minifier
        if (list == null) list = arrayListOf()

        list = list.filter {
            it["id"] != configId
        } as ArrayList<HashMap<String, String>>

        // Bugfix for R8 minifier, yes It make no sense for regular programmer, but it's a bug in R8 minifier
        if (list == null) list = arrayListOf()

        putString("configs", gson.toJson(list))

        for (listener in listeners) {
            listener.onLogitBiasConfigChange()
        }
    }

    fun getAllConfigs(): ArrayList<HashMap<String, String>> {
        val gson = Gson()
        val json = getString("configs", "[]")
        val type: Type = TypeToken.getParameterized(ArrayList::class.java, HashMap::class.java).type

        var list =  try {
            gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, String>>
        } catch (e: Exception) {
            arrayListOf()
        }

        // R8 bug fix
        if (list == null) list = arrayListOf()

        return list ?: arrayListOf()
    }

    fun getConfigById(configId: String): HashMap<String, String>? {
        val list = getAllConfigs()
        return list.find {
            it["id"] == configId
        }
    }

    fun interface OnLogitBiasConfigChangeListener {
        fun onLogitBiasConfigChange()
    }

    fun movePreferences(oldId: String, newId: String, context: Context) {
        val oldPrefs = context.getSharedPreferences("logit_bias_config_$oldId", Context.MODE_PRIVATE)
        val newPrefs = context.getSharedPreferences("logit_bias_config_$newId", Context.MODE_PRIVATE)

        val oldPrefsEditor = oldPrefs.edit()
        val newPrefsEditor = newPrefs.edit()

        oldPrefs.all.forEach {
            newPrefsEditor.putString(it.key, it.value.toString())
        }

        newPrefsEditor.apply()
        oldPrefsEditor.clear().apply()
    }
}