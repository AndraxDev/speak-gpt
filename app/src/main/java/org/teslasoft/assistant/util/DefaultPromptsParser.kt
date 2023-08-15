package org.teslasoft.assistant.util

class DefaultPromptsParser {
    private var explanationPrompt = HashMap<String, String>()
    private val languagesSupported = arrayListOf(
        "en",
        "es",
        "pl",
        "ru",
        "sk",
        "tr",
        "uk"
    )

    fun init() {
        explanationPrompt["en"] = "What does \"%s\" means?"
        explanationPrompt["es"] = "¿Qué significa \"%s\"?"
        explanationPrompt["pl"] = "Co oznacza \"%s\"?"
        explanationPrompt["ru"] = "Что означает \"%s\"?"
        explanationPrompt["sk"] = "Čo znamená \"%s\"?"
        explanationPrompt["tr"] = "\"%s\" ne anlama geliyor?"
        explanationPrompt["uk"] = "Що означає \"%s\"?"
    }

    fun parse(type: String, language: String) : String {
        if (type == "explanationPrompt") {
            var lng: String = "en"

            for (lang: String in languagesSupported) {
                if (lang == language) {
                    lng = language
                    break
                }
            }

            return explanationPrompt.getValue(lng)
        } else {
            throw IllegalArgumentException("Unsupported prompt type at org.teslasoft.assistant.util.DefaultPromptsParser.kt")
        }
    }
}