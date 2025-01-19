/*******************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.core.auth.client

import android.app.Activity
import org.teslasoft.core.auth.internal.ApplicationSignature

class TeslasoftIDClientBuilder(private val context: Activity) {
    private var teslasoftIDClient: TeslasoftIDClient? = null

    private var apiKey: String? = null
    private var appId: String? = null

    private var settingsListener: SettingsListener? = null
    private var syncListener: SyncListener? = null

    /**
     * Set app API key.
     *
     * @param apiKey API key.
     * */
    fun setApiKey(apiKey: String): TeslasoftIDClientBuilder {
        this.apiKey = apiKey
        return this
    }

    /**
     * Set app ID key.
     *
     * @param appId app ID.
     * */
    fun setAppId(appId: String): TeslasoftIDClientBuilder {
        this.appId = appId
        return this
    }

    /**
     * Listens for a settings requests.
     *
     * @param listener Settings listener.
     * */
    fun setSettingsListener(listener: SettingsListener): TeslasoftIDClientBuilder {
        this.settingsListener = listener
        return this
    }

    /**
     * Listens for a settings updates.
     *
     * @param listener Sync listener.
     * */
    fun setSyncListener(listener: SyncListener): TeslasoftIDClientBuilder {
        this.syncListener = listener
        return this
    }

    /**
     * Builds Teslasoft ID client.
     * */
    fun build() : TeslasoftIDClient {
        val applicationSignature = ApplicationSignature(context)
        teslasoftIDClient = TeslasoftIDClient(context, applicationSignature.getCertificateFingerprint("SHA256")!!, apiKey!!, appId!!, settingsListener, syncListener)
        return teslasoftIDClient!!
    }
}
