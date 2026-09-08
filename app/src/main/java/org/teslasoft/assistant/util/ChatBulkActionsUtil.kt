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

package org.teslasoft.assistant.util

class ChatBulkActionsUtil {
    companion object {

        fun filterSelection(
            chat: ArrayList<HashMap<String, Any>>,
            selectionMask: ArrayList<Boolean>
        ): ArrayList<HashMap<String, Any>> {
            return chat.filterIndexed { index, _ ->
                selectionMask.getOrElse(index) { false }
            }.toCollection(arrayListOf())
        }

        fun selectedMessagesToString(
            chat: ArrayList<HashMap<String, Any>>,
            selectionMask: ArrayList<Boolean>
        ): String {
            val filtered = filterSelection(chat, selectionMask)

            return buildString {
                for (message in filtered) {
                    if (message["isBot"] == true) {
                        append("[Bot] >\n")
                    } else {
                        append("[User] >\n")
                    }

                    append(message["message"])
                    append("\n\n")
                }
            }
        }

        fun shareString(context: android.content.Context, string: String) {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, string)
            }

            val chooser = android.content.Intent.createChooser(intent, "Share via")

            if (context !is android.app.Activity) {
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        }

        fun copyStringToClipboard(
            context: android.content.Context,
            string: String
        ) {
            val clipboard = context.getSystemService(
                android.content.Context.CLIPBOARD_SERVICE
            ) as android.content.ClipboardManager

            val clip = android.content.ClipData.newPlainText(
                "Copied Text",
                string
            )

            clipboard.setPrimaryClip(clip)
        }
    }
}
