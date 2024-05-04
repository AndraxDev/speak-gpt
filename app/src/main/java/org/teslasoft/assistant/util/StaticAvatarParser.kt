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

package org.teslasoft.assistant.util

import org.teslasoft.assistant.R

class StaticAvatarParser {
    companion object {
        fun parse(avatarId: String) : Int {
            return when (avatarId) {
                "speakgpt" -> {
                    R.drawable.assistant
                }
                "gpt" -> {
                    R.drawable.chatgpt_icon
                }
                "gemini" -> {
                    R.drawable.google_bard
                }
                "perplexity" -> {
                    R.drawable.perplexity_ai
                }
                else -> {
                    R.drawable.assistant
                }
            }
        }
    }
}
