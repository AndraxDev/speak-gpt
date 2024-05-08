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

    /**
     * Clears all chat messages for a given chat ID.
     *
     * @param context The context of the application.
     * @param chatId The ID of the chat to clear.
     */
    fun clearChat(context: Context, chatId: String) {
        context.getSharedPreferences("chat_$chatId", Context.MODE_PRIVATE).edit().putString("chat", "[]").apply()
    }

    /**
     * Deletes a chat, including all messages, from the chat list.
     *
     * @param context The context of the application.
     * @param chatName The name of the chat to delete.
     */
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

    /**
     * Retrieves a list of all available chats.
     *
     * @param context The context of the application.
     * @return An ArrayList of HashMap objects, where each HashMap represents a chat with key-value pairs for the chat name and ID.
     */
    fun getChatList(context: Context) : ArrayList<HashMap<String, String>> {
        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)

        val gson = Gson()
        val json = settings.getString("data", "[]")
        val type: Type = TypeToken.getParameterized(ArrayList::class.java, HashMap::class.java).type

        var list: ArrayList<HashMap<String, String>> = try {
            gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, String>>
        } catch (e: Exception) {
            arrayListOf()
        }

        // Bugfix for R8 minifier, yes It make no sense for regular programmer, but it's a bug in R8 minifier
        if (list == null) list = arrayListOf()

        // Dumb things goes gere
        if (list.isNullOrEmpty()) return arrayListOf()

        for (chat in list) {
            val messagesList = getChatById(context, Hash.hash(chat["name"].toString()))

            if (messagesList.isNotEmpty()) {
                val firstMessage = messagesList[0]["message"].toString()
                chat["first_message"] = firstMessage
            } else {
                chat["first_message"] = "No messages yet."
            }
        }

        // Bugfix for R8 minifier, yes It make no sense for regular programmer, but it's a bug in R8 minifier
        if (list == null) list = arrayListOf()

        return list
    }

    fun switchPinState(context: Context, chatId: String) {
        val list = getChatList(context)

        for (map in list) {
            if (Hash.hash(map["name"].toString()) == chatId) {
                if (map["pinned"] == "true") {
                    map["pinned"] = "false"
                } else {
                    map["pinned"] = "true"
                }
                break
            }
        }

        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)
        settings.edit().putString("data", json).apply()
    }

    fun pinChatById(context: Context, chatId: String) {
        putMetadataToChatById(context, chatId, "pinned", "true")
    }

    fun unpinChatById(context: Context, chatId: String) {
        putMetadataToChatById(context, chatId, "pinned", "false")
    }

    fun putTimestampToChatById(context: Context, chatId: String) {
        val timestamp = System.currentTimeMillis().toString()

        putMetadataToChatById(context, chatId, "timestamp", timestamp)
    }

    private fun putMetadataToChatById(context: Context, chatId: String, key: String, value: String) {
        val list = getChatList(context)

        for (map in list) {
            if (Hash.hash(map["name"].toString()) == chatId) {
                map[key] = value
                break
            }
        }

        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)
        settings.edit().putString("data", json).apply()
    }

    /**
     * Retrieves all chat messages for a given chat ID.
     *
     * @param context The context of the application.
     * @param chatId The ID of the chat to retrieve messages for.
     * @return An ArrayList of HashMap objects, where each HashMap represents a message with key-value pairs for the message content and sender ID.
     */
    fun getChatById(context: Context, chatId: String) : ArrayList<HashMap<String, Any>> {
        val chat: SharedPreferences = context.getSharedPreferences("chat_$chatId",
            Context.MODE_PRIVATE
        )

        var list: ArrayList<HashMap<String, Any>> = try {
            val gson = Gson()
            val json = chat.getString("chat", "[]")
            val type: Type = TypeToken.getParameterized(ArrayList::class.java, HashMap::class.java).type

            gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, Any>>
        } catch (e: Exception) {
            arrayListOf()
        }

        // Bugfix for R8 minifier
        if (list == null) list = arrayListOf()

        return list
    }

    fun getChatByIdAsString(context: Context, chatId: String) : String {
        val chat: SharedPreferences = context.getSharedPreferences("chat_$chatId",
            Context.MODE_PRIVATE
        )

        return chat.getString("chat", "[]").toString()
    }

    fun clearChatById(context: Context, chatId: String) {
        val chat: SharedPreferences = context.getSharedPreferences("chat_$chatId",
            Context.MODE_PRIVATE
        )

        chat.edit().putString("chat", "[]").apply()
    }

    /**
     * Generates a unique chat ID for a new chat.
     *
     * @param context The context of the application.
     * @return A unique chat ID as a String.
     */
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

    /**
     * Generates a unique chat ID for a new chat.
     *
     * @param context The context of the application.
     * @param prefix The prefix to use for the chat name.
     * @return A unique chat ID as a String.
     */
    fun getAvailableChatIdByPrefix(context: Context, prefix: String) : String {
        var x = 1

        val list = getChatList(context)

        while (true) {
            var isFound = false
            for (map: HashMap<String, String> in list) {
                if (map["name"] == "$prefix $x") {
                    isFound = true
                    break
                }
            }

            if (!isFound) break

            x++
        }

        return x.toString()
    }

    fun editMessage(context: Context, chatId: String, position: Int, newMessage: String) {
        val list = getChatById(context, chatId)

        list[position]["message"] = newMessage

        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_$chatId", Context.MODE_PRIVATE)
        settings.edit().putString("chat", json).apply()
    }

    fun deleteMessage(context: Context, chatId: String, position: Int) {
        val list = getChatById(context, chatId)

        list.removeAt(position)

        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_$chatId", Context.MODE_PRIVATE)
        settings.edit().putString("chat", json).apply()
    }

    /**
     * Generates a unique chat ID for a new chat (autoname).
     *
     * @param context The context of the application.
     * @return A unique chat ID as a String.
     */
    fun getAvailableChatIdForAutoname(context: Context) : String {
        var x = 1

        var list = getChatList(context)

        // R8 Bugfix
        if (list == null) list = arrayListOf()

        // Dumb things goes gere
        if (list.isEmpty()) list = arrayListOf()

        while (true) {
            var isFound = false
            for (map: HashMap<String, String> in list) {
                if (map["name"] == "_autoname_$x") {
                    isFound = true
                    break
                }
            }

            if (!isFound) break

            x++
        }

        return x.toString()
    }

    /**
     * Adds a new chat to the chat list.
     *
     * @param context The context of the application.
     * @param chatName The name of the chat to add.
     */
    fun addChat(context: Context, chatName: String) {
        val list = getChatList(context)

        val map: HashMap<String, String> = HashMap()

        map["name"] = chatName
        map["id"] = Hash.hash(chatName)
        map["timestamp"] = System.currentTimeMillis().toString()
        map["pinned"] = "false"

        list.add(map)
        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)
        settings.edit().putString("data", json).apply()

        val settings2: SharedPreferences = context.getSharedPreferences("chat_${Hash.hash(chatName)}", Context.MODE_PRIVATE)
        settings2.edit().putString("chat", "[]").apply()
    }

    /**
     * Checks if a chat with the given name already exists in the chat list.
     *
     * @param context The context of the application.
     * @param chatName The name of the chat to check for duplicates.
     * @return True if a chat with the given name already exists in the chat list, false otherwise.
     */
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

    fun getChatName(context: Context, chatId: String) : String {
        val list = getChatList(context)

        var name = ""
        for (map: HashMap<String, String> in list) {
            if (map["id"] == chatId) {
                name = map["name"].toString()
                break
            }
        }

        return name
    }

    /**
     * Edits the name of a chat and updates the chat list and chat data accordingly.
     *
     * @param context The context of the application.
     * @param chatName The new name of the chat.
     * @param previousName The previous name of the chat.
     */
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

    fun deleteChatById(context: Context, chatId: String) {
        val list = getChatList(context)

        for (map: HashMap<String, String> in list) {
            if (map["id"] == chatId) {
                list.remove(map)
                break
            }
        }

        val json: String = Gson().toJson(list)

        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)
        settings.edit().putString("data", json).apply()

        val settings2: SharedPreferences = context.getSharedPreferences("chat_$chatId", Context.MODE_PRIVATE)
        settings2.edit().clear().apply()
    }
}
