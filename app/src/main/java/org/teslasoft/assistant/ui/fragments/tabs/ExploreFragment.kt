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
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.gson.Gson
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.activities.TipsActivity
import org.teslasoft.assistant.ui.adapters.AISetAdapter
import org.teslasoft.core.api.network.RequestNetwork

class ExploreFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var mContext: Context? = null

    private var btnTips: ImageButton? = null

    private var setsList: ListView? = null
    private var aiSets: ArrayList<Map<String, String>> = ArrayList()
    private var setsAdapter: AISetAdapter? = null
    private var requestNetwork: RequestNetwork? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var loading: ProgressBar? = null
    private var btnRetry: MaterialButton? = null
    private var btnErrorDetails: MaterialButton? = null
    private var noInternet: LinearLayout? = null

    private var error = ""

    private var requestListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            error = ""
            refreshLayout?.isRefreshing = false
            loading?.visibility = View.GONE
            noInternet?.visibility = View.GONE
            val gson = Gson()

            try {
                var response: ArrayList<Map<String, String>> = gson.fromJson(message, ArrayList::class.java) as ArrayList<Map<String, String>>
                if (response == null) response = arrayListOf()

                aiSets.clear()
                aiSets.addAll(response)
                setsAdapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
                noInternet?.visibility = View.VISIBLE
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            error = message
            noInternet?.visibility = View.VISIBLE
            refreshLayout?.isRefreshing = false
            loading?.visibility = View.GONE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
    }

    override fun onDetach() {
        super.onDetach()

        mContext = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnTips = view.findViewById(R.id.btn_tips)
        setsList = view.findViewById(R.id.ai_sets_list)
        refreshLayout = view.findViewById(R.id.refresh_layout)
        loading = view.findViewById(R.id.loading)
        btnRetry = view.findViewById(R.id.btn_reconnect)
        btnErrorDetails = view.findViewById(R.id.btn_show_details)
        noInternet = view.findViewById(R.id.no_internet)

        refreshLayout?.setColorSchemeResources(R.color.accent_900)
        refreshLayout?.setProgressBackgroundColorSchemeColor(
            SurfaceColors.SURFACE_2.getColor(mContext ?: return)
        )

        refreshLayout?.setSize(SwipeRefreshLayout.LARGE)

        refreshLayout?.setOnRefreshListener(this)

        setsList?.divider = null

        aiSets = arrayListOf()

        setsAdapter = AISetAdapter(mContext ?: return, aiSets)

        setsList?.adapter = setsAdapter

        requestNetwork = RequestNetwork((mContext as Activity?) ?: return)

        runRequest()

        btnTips?.setOnClickListener {
            startActivity(Intent(mContext, TipsActivity::class.java))
        }

        btnRetry?.setOnClickListener {
            runRequest()
        }

        btnErrorDetails?.setOnClickListener {
            MaterialAlertDialogBuilder(mContext ?: return@setOnClickListener, R.style.App_MaterialAlertDialog)
                .setTitle("Error details")
                .setMessage(error)
                .setPositiveButton("Close") { _, _ -> }
                .show()
        }
    }

    private fun runRequest() {
        error = ""
        loading?.visibility = View.VISIBLE
        noInternet?.visibility = View.GONE
        requestNetwork?.startRequestNetwork("GET", "https://${Config.API_SERVER_NAME}/api/v1/explore", "A", requestListener)
    }

    override fun onRefresh() {
        runRequest()
    }
}