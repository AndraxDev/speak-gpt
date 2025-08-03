package org.teslasoft.core.auth

import android.util.Log

interface AccountSyncListener {
    /**
     * onAuthFinished triggers when authentication was successful.
     *
     * @param name First and last name of the account.
     * @param email Email of the account.
     * @param isDev Determines if user a developer. Can be used to deliver beta features.
     * @param token An auth token. Use it to sync settings and perform actions in your account.
     * */
    fun onAuthFinished(name: String, email: String, isDev: Boolean, token: String) {
        Log.i("org.teslasoft.core.auth.AccountSyncListener", "onAuthFinished")
        /* default implementation */
    }

    /**
     * onAuthCanceled triggers when user dismissed account picker dialog without selecting any options.
     * */
    fun onAuthCanceled() {
        Log.i("org.teslasoft.core.auth.AccountSyncListener", "onAuthCanceled")
        /* default implementation */
    }

    /**
     * onSignedOut triggers when user clicked "Turn off sync" button or user session has expired.
     * */
    fun onSignedOut() {
        Log.i("org.teslasoft.core.auth.AccountSyncListener", "onSignedOut")
        /* default implementation */
    }

    /**
     * onAuthFailed triggers when internal error is occurred. (ex. Teslasoft Core is not installed, no Internet connection
     * or account database is corrupted).
     *
     * @param state Reason of failure.
     * @param message Error message. Please use android string instead od this message. Android strings can be translated to other languages.
     * This message is for developers only.
     * */
    fun onAuthFailed(state: String, message: String) {
        Log.e("org.teslasoft.core.auth.AccountSyncListener", "onAuthFailed: $state, message: $message")
        /* default implementation */
    }
}