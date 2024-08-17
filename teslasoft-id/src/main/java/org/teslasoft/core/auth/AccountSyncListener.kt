/*******************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.teslasoft.core.auth

interface AccountSyncListener {
    /**
     * onAuthFinished triggers when authentication was successful.
     *
     * @param name First and last name of the account.
     * @param email Email of the account.
     * @param isDev Determines if user a developer. Can be used to deliver beta features.
     * @param token An auth token. Use it to sync settings and perform actions in your account.
     * */
    fun onAuthFinished(name: String, email: String, isDev: Boolean, token: String) { /* default implementation */ }

    /**
     * onAuthCanceled triggers when user dismissed account picker dialog without selecting any options.
     * */
    fun onAuthCanceled() { /* default implementation */ }

    /**
     * onSignedOut triggers when user clicked "Turn off sync" button or user session has expired.
     * */
    fun onSignedOut() { /* default implementation */ }

    /**
     * onAuthFailed triggers when internal error is occurred. (ex. Teslasoft Core is not installed, no Internet connection
     * or account database is corrupted).
     *
     * @param state Reason of failure.
     * @param message Error message. Please use android string instead od this message. Android strings can be translated to other languages.
     * This message is for developers only.
     * */
    fun onAuthFailed(state: String, message: String) { /* default implementation */ }

    /**
     * onAuthFailed triggers when internal error is occurred. (ex. Teslasoft Core is not installed, no Internet connection
     * or account database is corrupted).
     *
     * @param state Reason of failure.
     * */
    @Deprecated("Use onAuthFailed(state: String, message: String) instead.")
    fun onAuthFailed(state: String) { /* default implementation */ }
}
