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

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class TipsFragment : Fragment() {

    private var ad: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences: Preferences = Preferences.getPreferences(requireActivity(), "")

        MobileAds.initialize(requireActivity()) { /* unused */ }

        ad = view.findViewById(R.id.ad)

        val adView = AdView(requireActivity())
        adView.setAdSize(AdSize.LARGE_BANNER)
        adView.adUnitId = if (preferences.getDebugTestAds()) "ca-app-pub-3940256099942544/9214589741" else "ca-app-pub-7410382345282120/1474294730"

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
    }
}
