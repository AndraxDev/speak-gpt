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
import com.google.gson.Gson
import org.teslasoft.assistant.preferences.dto.FavoriteModelObject
import androidx.core.content.edit

class FavoriteModelsPreferences private constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val KEY_FAVORITE_MODELS = "favorite_models"

        private lateinit var sharedPreferences: SharedPreferences

        private var instance: FavoriteModelsPreferences? = null

        fun getPreferences(context: Context): FavoriteModelsPreferences {
            sharedPreferences = context.getSharedPreferences("favorite_models", Context.MODE_PRIVATE)

            if (instance == null) {
                instance = FavoriteModelsPreferences(sharedPreferences)
            }

            return instance!!
        }
    }

    fun setFavoriteModels(models: ArrayList<Map<String, String>>) {
        sharedPreferences.edit { putString(KEY_FAVORITE_MODELS, Gson().toJson(models)) }
    }

    fun getFavoriteModels(): ArrayList<Map<String, String>> {
        val models = sharedPreferences.getString(KEY_FAVORITE_MODELS, "[]")

        var list = try {
            Gson().fromJson(models, ArrayList<Map<String, String>>()::class.java)
        } catch (_: Exception) {
            arrayListOf()
        }

        if (list == null) list = arrayListOf()

        return list
    }

    fun addFavoriteModel(model: FavoriteModelObject) {
        var models = getFavoriteModels()

        if (models == null) models = arrayListOf()

        val modelExists: Boolean = models.any { m -> m["modelId"] == model.modelId && m["endpointId"] == model.endpointId }
        if (!modelExists) {
            models.add(hashMapOf("modelId" to model.modelId, "endpointId" to model.endpointId))
            setFavoriteModels(models)
        }
    }
}
