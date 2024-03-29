package org.teslasoft.assistant.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

class DeviceInfoProvider {
    companion object {

        /**
         * Get Android ID
         * */
        @SuppressLint("HardwareIds")
        fun getAndroidId(context: Context) : String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }

        /**
         * Get installation ID
         * */
        fun getInstallationId(context: Context) : String {
            return EncryptedPreferences.getEncryptedPreference(context, "device_info", "installation_id")
        }

        /**
         * Set installation ID
         * */
        private fun setInstallationId(id: String, context: Context) {
            EncryptedPreferences.setEncryptedPreference(context, "device_info", "installation_id", id)
        }

        /**
         * Assign installation ID if empty
         * */
        fun assignInstallationId(context: Context) {
            if (getInstallationId(context) == "") {
                setInstallationId(java.util.UUID.randomUUID().toString(), context)
            }
        }

        /**
         * Reset installation ID
         * */
        fun resetInstallationId(context: Context) {
            setInstallationId(java.util.UUID.randomUUID().toString(), context)
        }

        /**
         * Opt-out from installation ID
         * */
        fun revokeAuthorization(context: Context) {
            setInstallationId("00000000-0000-0000-0000-000000000000", context)
            Logger.deleteAllLogs(context)
        }
    }
}