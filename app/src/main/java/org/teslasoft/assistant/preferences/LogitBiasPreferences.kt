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
import org.teslasoft.assistant.preferences.dto.LogitBiasObject
import org.teslasoft.assistant.util.Hash

class LogitBiasPreferences(context: Context, id: String) {

    private var preferences: SharedPreferences? = null
    private var listeners: ArrayList<OnLogitBiasChangeListener> = ArrayList()

    init {
        preferences = context.getSharedPreferences("logit_bias_config_$id", Context.MODE_PRIVATE)
    }

    fun addOnLogitBiasChangeListener(listener: OnLogitBiasChangeListener) {
        listeners.add(listener)
    }

    fun getString(key: String, defValue: String): String {
        return preferences?.getString(key, defValue)!!
    }

    fun putString(key: String, value: String) {
        preferences?.edit()?.putString(key, value)?.apply()
    }

    fun getLogitBias(id: String): LogitBiasObject {
        val tokenId = getString(id + "_tokenId", "")
        val logitBias = getString(id + "_logitBias", "")
        return LogitBiasObject(tokenId, logitBias)
    }

    fun setLogitBias(logitBias: LogitBiasObject) {
        val id = Hash.hash(logitBias.tokenId)
        putString(id + "_tokenId", logitBias.tokenId)
        putString(id + "_logitBias", logitBias.logitBias)

        for (listener in listeners) {
            listener.onLogitBiasChange()
        }
    }

    fun removeLogitBias(id: String) {
        preferences?.edit()?.remove(Hash.hash(id) + "_tokenId")?.apply()
        preferences?.edit()?.remove(Hash.hash(id) + "_logitBias")?.apply()

        for (listener in listeners) {
            listener.onLogitBiasChange()
        }
    }

    fun getLogitBiasesList(): ArrayList<LogitBiasObject> {
        val logitBiases = ArrayList<LogitBiasObject>()
        val keys = preferences?.all?.keys
        if (keys != null) {
            for (key in keys) {
                if (key.contains("_tokenId")) {
                    val tokenId = preferences?.getString(key, "")
                    val logitBias = preferences?.getString(key.replace("_tokenId", "_logitBias"), "")
                    logitBiases.add(LogitBiasObject(tokenId!!, logitBias!!))
                }
            }
        }

        // R8 bug fix
        if (logitBiases == null) {
            return ArrayList<LogitBiasObject>()
        }

        return logitBiases
    }

    fun getLogitBiasesMap(): HashMap<String, Int> {
        val logitBiases = getLogitBiasesList()

        val map = HashMap<String, Int>()

        for (i in logitBiases) {
            map[i.tokenId] = i.logitBias.toInt()
        }

        return map
    }

    fun interface OnLogitBiasChangeListener {
        fun onLogitBiasChange()
    }
}