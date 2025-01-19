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

package org.teslasoft.assistant.util

import android.content.Context
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier

class DefaultPromptsParser {
    private var explanationPrompt = HashMap<String, String>()
    private var summarizationPrompt = HashMap<String, String>()
    private var languagesSupported = arrayListOf(
        "en",
        "es",
        "pl",
        "ru",
        "sk",
        "tr",
        "uk",
        "de"
    )

    private var languageIdentifier: LanguageIdentifier? = null

    fun init() {
        explanationPrompt["en"] = "What does \"%s\" means?"
        explanationPrompt["es"] = "¿Qué significa \"%s\"?"
        explanationPrompt["pl"] = "Co oznacza \"%s\"?"
        explanationPrompt["ru"] = "Что означает \"%s\"?"
        explanationPrompt["sk"] = "Čo znamená \"%s\"?"
        explanationPrompt["tr"] = "\"%s\" ne anlama geliyor?"
        explanationPrompt["uk"] = "Що означає \"%s\"?"
        explanationPrompt["de"] = "Was bedeutet \"%s\"?"

        summarizationPrompt["en"] = "Summarize the following text \"%s\". Write your answer in the same language as the text."
        summarizationPrompt["de"] = "Fasse den folgenden Text \"%s\" zusammen."
    }

    private var listener: OnCompletedListener? = null

    fun parse(type: String, text: String, context: Context) {
        if (type == "explanationPrompt") {
            languageIdentifier = LanguageIdentification.getClient()
            languageIdentifier?.identifyLanguage(text)
                ?.addOnSuccessListener { languageCode ->
                    val l = if (languageCode == "und") {
                        "en"
                    } else {
                        languageCode
                    }

                    var lng = "en"

                    /* R8 obfuscator fix */
                    if (languagesSupported == null) languagesSupported = arrayListOf(
                        "en",
                        "es",
                        "pl",
                        "ru",
                        "sk",
                        "tr",
                        "uk",
                        "de"
                    )

                    for (lang: String in languagesSupported) {
                        /* R8 obfuscator fix */
                        if (lang != null && lang == l) {
                            lng = lang
                            break
                        }
                    }

                    val d = String.format(explanationPrompt.getValue(lng).toString(), text)

                    listener!!.onCompleted(d)
                }?.addOnFailureListener {
                    listener!!.onCompleted(String.format(explanationPrompt.getValue("en"), text))
                }
        } else if (type == "summarizationPrompt") {
            languageIdentifier = LanguageIdentification.getClient()
            languageIdentifier?.identifyLanguage(text)
                ?.addOnSuccessListener { languageCode ->
                    val l = if (languageCode == "und") {
                        "en"
                    } else {
                        languageCode
                    }

                    var lng = "en"

                    if (languagesSupported == null) languagesSupported = arrayListOf(
                        "en",
                        "es",
                        "pl",
                        "ru",
                        "sk",
                        "tr",
                        "uk",
                        "de"
                    )

                    for (lang: String in languagesSupported) {
                        if (lang != null && lang == l) {
                            lng = lang
                            break
                        }
                    }

                    val prompt = String.format(summarizationPrompt.getValue(lng).toString(), text)
                    listener!!.onCompleted(prompt)
                }?.addOnFailureListener {
                    val prompt = String.format(summarizationPrompt.getValue("en"), text)
                    listener!!.onCompleted(prompt)
                }
        } else {
            throw IllegalArgumentException("Unsupported prompt type at org.teslasoft.assistant.util.DefaultPromptsParser.kt")
        }
    }

    fun addOnCompletedListener(listener: OnCompletedListener) {
        this.listener = listener
    }

    fun interface OnCompletedListener {
        fun onCompleted(text: String)
    }
}
