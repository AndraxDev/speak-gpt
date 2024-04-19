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

package org.teslasoft.assistant.ui.fragments.tabs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.util.TestDevicesAds

class TipsFragment : Fragment() {

    private var ad: LinearLayout? = null

    private var onAttach: Boolean = false

    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences: Preferences = Preferences.getPreferences(mContext?: return, "")

        ad = view.findViewById(R.id.ad)

        Thread {
            while (!onAttach) {
                Thread.sleep(100)
            }

            (mContext as Activity?)?.runOnUiThread {
                if (preferences.getAdsEnabled()) {
                    MobileAds.initialize(mContext?: return@runOnUiThread) { /* unused */ }

                    val requestConfiguration = RequestConfiguration.Builder()
                        .setTestDeviceIds(TestDevicesAds.TEST_DEVICES)
                        .build()
                    MobileAds.setRequestConfiguration(requestConfiguration)

                    val adView = AdView(mContext ?: return@runOnUiThread)
                    adView.setAdSize(AdSize.LARGE_BANNER)
                    adView.adUnitId =
                        if (preferences.getDebugTestAds()) getString(R.string.ad_banner_unit_id_test) else getString(
                            R.string.ad_banner_unit_id
                        )

                    ad?.addView(adView)

                    val adRequest: AdRequest = AdRequest.Builder().build()

                    adView.loadAd(adRequest)

                    adView.adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            ad?.visibility = View.GONE
                        }

                        override fun onAdLoaded() {
                            ad?.visibility = View.VISIBLE
                        }
                    }
                } else {
                    ad?.visibility = View.GONE
                }
            }
        }.start()
    }

    override fun onAttach(context: Context) {
        mContext = context
        onAttach = true

        super.onAttach(context)
    }

    override fun onDetach() {
        mContext = null
        onAttach = false
        super.onDetach()
    }
}
