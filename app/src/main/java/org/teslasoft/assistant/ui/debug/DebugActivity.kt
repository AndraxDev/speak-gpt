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

package org.teslasoft.assistant.ui.debug

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.EditText
import android.widget.TextView

import androidx.fragment.app.FragmentActivity

import com.google.android.material.button.MaterialButton

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.util.LocaleParser

class DebugActivity : FragmentActivity() {
    private var btnDebug: MaterialButton? = null
    private var fieldDebug: EditText? = null
    private var lng: TextView? = null

    private var isTTSInitialized = false

    // Init TTS
    private var tts: TextToSpeech? = null
    private val ttsListener: TextToSpeech.OnInitListener =
        TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(LocaleParser.parse(Preferences.getPreferences(this@DebugActivity, "").getLanguage()))

                isTTSInitialized = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)

                val voices: Set<Voice> = tts!!.voices
                for (v: Voice in voices) {
                    if (v.name.equals("en-us-x-iom-local") && Preferences.getPreferences(this@DebugActivity, "").getLanguage() == "en") {
                        tts!!.voice = v
                    }
                }

                /*
                * Voice models (english: en-us-x):
                * sfg-local
                * iob-network
                * iom-local
                * iog-network
                * tpc-local
                * tpf-local
                * sfg-network
                * iob-local
                * tpd-network
                * tpc-network
                * iol-network
                * iom-network
                * tpd-local
                * tpf-network
                * iog-local
                * iol-local
                * */
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_debug)

        btnDebug = findViewById(R.id.btnDebug)
        fieldDebug = findViewById(R.id.fieldDebug)
        lng = findViewById(R.id.lng)

        lng?.text = LocaleParser.parse(Preferences.getPreferences(this, "").getLanguage()).language

        tts = TextToSpeech(this, ttsListener)

        btnDebug?.setOnClickListener {
            tts!!.speak(fieldDebug!!.text, TextToSpeech.QUEUE_FLUSH, null,"")
        }
    }

    public override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}
