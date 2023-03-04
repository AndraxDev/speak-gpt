package org.teslasoft.assistant.util

import java.util.Locale

class LocaleParser {
    public fun parse(language: String) : Locale {
        return Locale.forLanguageTag(language)
    }
}