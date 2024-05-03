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