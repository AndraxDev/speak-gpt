package org.teslasoft.assistant.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedPreferences {
    companion object {
        /**
         * Get encrypted preference
         * */
        fun getEncryptedPreference(context: Context, file: String, key: String) : String {
            try {
                val mainKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    file,
                    mainKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                return sharedPreferences.getString(key, "")!!
            } catch (e: Exception) {
                return ""
            }
        }

        /**
         * Set encrypted preference
         * */
        fun setEncryptedPreference(context: Context, file: String, key: String, value: String) {
            val mainKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                file,
                mainKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            with (sharedPreferences.edit()) {
                putString(key, value)
                apply()
            }
        }
    }
}