/**************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
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

import com.google.android.gms.ads.AdRequest

class TestDevicesAds {
    companion object {
        val TEST_DEVICES: MutableList<String> = arrayListOf(
            AdRequest.DEVICE_ID_EMULATOR,
            "10e46e1d-ccaa-4909-85bf-83994b920a9c",
            "c29eb9ca-6008-421f-b306-c559d96ea303",
            "5e03e1ee-7eb9-4b51-9d21-10ca1ad1abe1",
            "9AB1B18F59CF84AA",
            "27583FCB662C9F6D"
        )
    }
}