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

package org.teslasoft.assistant.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.util.Hash
import java.lang.Exception
import java.lang.reflect.Type

class ChatPreferences private constructor() {
    companion object {
        private var preferences: ChatPreferences? = null

        fun getChatPreferences() : ChatPreferences {
            if (preferences == null) preferences = ChatPreferences()
            return preferences!!
        }
    }

    fun clearChat(context: Context, chatId: String) {
        context.getSharedPreferences("chat_$chatId", Context.MODE_PRIVATE).edit().putString("chat", "[]").apply()
    }

    fun deleteChat(context: Context, chatName: String) {
        val list = getChatList(context)

        for (map: HashMap<String, String> in list) {
            if (map["name"] == chatName) {
                list.remove(map)
                break
            }
        }

        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)
        settings.edit().putString("data", json).apply()

        val settings2: SharedPreferences = context.getSharedPreferences("chat_${Hash.hash(chatName)}", Context.MODE_PRIVATE)
        settings2.edit().clear().apply()
    }

    fun getChatList(context: Context) : ArrayList<HashMap<String, String>> {
        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)

        val gson = Gson()
        val json = settings.getString("data", null)
        val type: Type = TypeToken.getParameterized(ArrayList::class.java, HashMap::class.java).type

        var list: ArrayList<HashMap<String, String>> = try {
            gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, String>>
        } catch (e: Exception) {
            arrayListOf()
        }

        if (list == null) list = arrayListOf()

        return list
    }

    fun getChatById(context: Context, chatId: String) : ArrayList<HashMap<String, Any>> {
        val chat: SharedPreferences = context.getSharedPreferences("chat_$chatId",
            Context.MODE_PRIVATE
        )



        var list: ArrayList<HashMap<String, Any>> = try {
            val gson = Gson()
            val json = chat.getString("chat", null)
            val type: Type = TypeToken.getParameterized(ArrayList::class.java, HashMap::class.java).type

            gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, Any>>
        } catch (e: Exception) {
            arrayListOf()
        }

        if (list == null) list = arrayListOf()

        return list
    }

    fun getAvailableChatId(context: Context) : String {
        var x = 1

        val list = getChatList(context)

        while (true) {
            var isFound = false
            for (map: HashMap<String, String> in list) {
                if (map["name"] == "New chat $x") {
                    isFound = true
                    break
                }
            }

            if (!isFound) break

            x++
        }

        return x.toString()
    }

    fun addChat(context: Context, chatName: String) {
        val list = getChatList(context)

        val map: HashMap<String, String> = HashMap()

        map["name"] = chatName
        map["id"] = Hash.hash(chatName)

        list.add(map)
        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)
        settings.edit().putString("data", json).apply()

        val settings2: SharedPreferences = context.getSharedPreferences("chat_${Hash.hash(chatName)}", Context.MODE_PRIVATE)
        settings2.edit().putString("chat", "[]").apply()
    }

    fun checkDuplicate(context: Context, chatName: String) : Boolean {
        val list = getChatList(context)

        var isFound = false
        for (map: HashMap<String, String> in list) {
            if (map["id"] == Hash.hash(chatName)) {
                isFound = true
                break
            }
        }

        return isFound
    }

    fun editChat(context: Context, chatName: String, previousName: String) {
        val list = getChatList(context)

        for (map: HashMap<String, String> in list) {
            if (map["id"] == Hash.hash(previousName)) {
                map["name"] = chatName
                map["id"] = Hash.hash(chatName)

                val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)

                val json: String = Gson().toJson(list)

                settings.edit().putString("data", json).apply()

                val settings1: SharedPreferences = context.getSharedPreferences("chat_${Hash.hash(previousName)}", Context.MODE_PRIVATE)

                val str = settings1.getString("chat", "")
                settings1.edit().clear().apply()

                val settings2: SharedPreferences = context.getSharedPreferences("chat_${Hash.hash(chatName)}", Context.MODE_PRIVATE)
                settings2.edit().putString("chat", str).apply()

                break
            }
        }
    }
}