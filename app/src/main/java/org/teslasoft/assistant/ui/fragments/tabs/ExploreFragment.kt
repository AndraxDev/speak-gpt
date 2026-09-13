/**************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.gson.Gson
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.ui.activities.ChatActivity
import org.teslasoft.assistant.ui.activities.TipsActivity
import org.teslasoft.assistant.ui.adapters.AISetAdapterNew
import org.teslasoft.assistant.ui.fragments.dialogs.AddChatDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.EditApiEndpointDialogFragment
import org.teslasoft.assistant.util.Hash
import org.teslasoft.core.api.network.RequestNetwork
import androidx.core.net.toUri
import com.example.liquidglass.LiquidGlassView
import org.teslasoft.assistant.ui.liquidglass.LiquidGlassUtil
import org.teslasoft.assistant.util.WindowInsetsUtil
import java.util.EnumSet

class ExploreFragment : Fragment(), AISetAdapterNew.OnInteractionListener {

    private var btnTips: LiquidGlassView? = null
    private var loading: LoadingIndicator? = null
    private var btnRetry: MaterialButton? = null
    private var btnErrorDetails: MaterialButton? = null
    private var noInternet: LinearLayout? = null

    private var setsList: RecyclerView? = null
    private var aiSets: ArrayList<Map<String, String>> = ArrayList()
    private var aiSetsAdapter: AISetAdapterNew? = null
    private var requestNetwork: RequestNetwork? = null

    private var apiEndpointPreferences: ApiEndpointPreferences? = null
    private var preferences: Preferences? = null

    private var mContext: Context? = null

    private var error = ""

    private var isInitialized = false

    private var requestFinished = 0

    private var requestListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onResponse(tag: String, message: String) {
            requestFinished = 1
            error = ""
            loading?.visibility = View.GONE
            noInternet?.visibility = View.GONE
            setsList?.visibility = View.VISIBLE
            val gson = Gson()

            try {
                var response: ArrayList<Map<String, String>> = gson.fromJson(message, ArrayList::class.java) as ArrayList<Map<String, String>>
                if (response == null) response = arrayListOf()

                aiSets.clear()
                aiSets.addAll(response)
                aiSetsAdapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
                noInternet?.visibility = View.VISIBLE
                setsList?.visibility = View.GONE
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            requestFinished = 2
            error = message
            noInternet?.visibility = View.VISIBLE
            loading?.visibility = View.GONE
            setsList?.visibility = View.GONE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context

        applyWindowInsets()
        workaround()

        if (requestFinished == 1) {
            loading?.visibility = View.GONE
            noInternet?.visibility = View.GONE
            setsList?.visibility = View.VISIBLE
        } else if (requestFinished == 2) {
            noInternet?.visibility = View.VISIBLE
            loading?.visibility = View.GONE
            setsList?.visibility = View.GONE
        }
    }

    override fun onDetach() {
        super.onDetach()

        mContext = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    private var rootView: View? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view

        btnTips = view.findViewById(R.id.btn_tips)
        setsList = view.findViewById(R.id.ai_sets_list)
        loading = view.findViewById(R.id.loading)
        btnRetry = view.findViewById(R.id.btn_reconnect)
        btnErrorDetails = view.findViewById(R.id.btn_show_details)
        noInternet = view.findViewById(R.id.no_internet)

        setsList?.background = SurfaceColors.SURFACE_0.getColor(mContext ?: return).toDrawable()

        LiquidGlassUtil.scanForLiquidGlassAndInitSettings(view, mContext ?: return, null, false)
        LiquidGlassUtil.setAccentColorMutedForLiquidGlassView(R.id.btn_tips, view, mContext ?: return)
        LiquidGlassUtil.setAccentColorMutedForLiquidGlassView(R.id.title, view, mContext ?: return)

        applyWindowInsets()

        preferences = Preferences.getPreferences(mContext ?: return, "")
        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(mContext ?: return)

        setsList?.layoutManager = LinearLayoutManager(mContext)

        aiSets = arrayListOf()

        aiSetsAdapter = AISetAdapterNew(mContext ?: return, aiSets, this)

        setsList?.adapter = aiSetsAdapter

        requestNetwork = RequestNetwork((mContext as Activity?) ?: return)

        if (!isInitialized || aiSets == null || aiSets.isEmpty()) {
            runRequest()
            isInitialized = true
        } else {
            loading?.visibility = View.GONE
            noInternet?.visibility = View.GONE
            setsList?.visibility = View.VISIBLE
            requestFinished = 1
        }

        btnTips?.setOnClickListener {
            startActivity(Intent(mContext, TipsActivity::class.java))
        }

        btnRetry?.setOnClickListener {
            runRequest()
        }

        btnErrorDetails?.setOnClickListener {
            MaterialAlertDialogBuilder(mContext ?: return@setOnClickListener, R.style.App_MaterialAlertDialog)
                .setTitle(getString(R.string.label_error_details))
                .setMessage(error)
                .setPositiveButton(R.string.btn_close) { _, _ -> }
                .show()
        }

        Thread {
            val th = Thread {
                while (mContext == null) { /* wait */ }
            }

            th.start()
            th.join()

            try {
                btnTips?.background = getDisabledDrawable(ResourcesCompat.getDrawable(mContext?.resources ?: return@Thread, R.drawable.btn_accent_tonal, mContext?.theme) ?: return@Thread)
            } catch (_: NullPointerException) { /* ignored */ }
        }.start()
    }

    private fun workaround() {
        if (mContext != null) {
            // Temporary workaround... (or maybe permanent, who knows...)
            // If you wonder what it does, it just forcibly re-renders the screen contents to let liquid glass views build captures of backdrops.
            // Fragment (tab) switch animations causes capture freeze which leads to liquid glass views not being able to render their backdrops properly.
            Handler(Looper.getMainLooper()).postDelayed({
                rootView?.findViewById<View>(R.id.reload_pixel)?.background = 0x11000000.toDrawable()
                Handler(Looper.getMainLooper()).postDelayed({
                    rootView?.findViewById<View>(R.id.reload_pixel)?.background = 0x00000000.toDrawable()
                    workaround()
                }, 50)
            }, 100)
        }
    }

    fun applyWindowInsets() {
        if (rootView != null && mContext != null) {
            WindowInsetsUtil.adjustPaddings((mContext as Activity?) ?: return, rootView, R.id.header_keeper, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS))
            WindowInsetsUtil.adjustPaddings((mContext as Activity?) ?: return, rootView, R.id.ai_sets_list, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.NAVIGATION_BAR))
        }
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return if (isDarkThemeEnabled() && preferences!!.getAmoledPitchBlack()) {
            ResourcesCompat.getColor(mContext?.resources!!, R.color.amoled_accent_50,  mContext?.theme)
        } else if (mContext != null) {
            SurfaceColors.SURFACE_5.getColor(mContext!!)
        } else 0
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    private fun runRequest() {
        requestFinished = 0
        error = ""
        loading?.visibility = View.VISIBLE
        setsList?.visibility = View.GONE
        noInternet?.visibility = View.GONE
        requestNetwork?.startRequestNetwork("GET", "https://${Config.API_SERVER_NAME}/api/v1/explore", "A", requestListener)
    }

    override fun onUseGloballyClick(model: String, endpointUrl: String, endpointName: String, avatarType: String, avatarId: String, assistantName: String) {
        performAction(model, endpointUrl, endpointName, "", avatarType, avatarId, assistantName) { en, _, m, at, ai, an ->
            setGlobally(en, m, at, ai, an)
        }
    }

    override fun onCreateChatClick(model: String, endpointUrl: String, endpointName: String, suggestedChatName: String, avatarType: String, avatarId: String, assistantName: String) {
        performAction(model, endpointUrl, endpointName, suggestedChatName, avatarType, avatarId, assistantName) { en, scn, m, at, ai, an ->
            createChat(en, scn, m, at, ai, an)
        }
    }

    override fun onGetApiKeyClicked(apiKeyUrl: String) {
        val i = Intent().setAction(Intent.ACTION_VIEW)
        i.data = apiKeyUrl.toUri()
        startActivity(i)
    }

    private fun setGlobally(endpointName: String, model: String, avatarType: String, avatarId: String, assistantName: String) {
        preferences?.setApiEndpointId(Hash.hash(endpointName))
        preferences?.setModel(model)
        preferences?.setAvatarType(avatarType)
        preferences?.setAvatarId(avatarId)
        preferences?.setAssistantName(assistantName)
        Toast.makeText(mContext, getString(R.string.label_api_endpoint_set_globally), Toast.LENGTH_SHORT).show()
    }

    private fun createChat(endpointName: String, suggestedChatName: String, model: String, avatarType: String, avatarId: String, assistantName: String) {
        val chatDialogFragment: AddChatDialogFragment = AddChatDialogFragment.newInstance(false, suggestedChatName,
            fromFile = false,
            disableAutoName = true,
            saveChat = true,
            endpointId = Hash.hash(endpointName),
            model = model,
            avatarType = avatarType,
            avatarId = avatarId,
            assistantName = assistantName,
            position = -1
        )
        chatDialogFragment.setStateChangedListener(object : AddChatDialogFragment.StateChangesListener {
            override fun onAdd(name: String, id: String, fromFile: Boolean) {
                val i = Intent(
                    mContext ?: return,
                    ChatActivity::class.java
                ).setAction(Intent.ACTION_VIEW)

                i.putExtra("name", name)
                i.putExtra("chatId", id)

                startActivity(i)
            }

            override fun onEdit(name: String, id: String, position: Int) {
                val i = Intent(
                    mContext ?: return,
                    ChatActivity::class.java
                ).setAction(Intent.ACTION_VIEW)

                i.putExtra("name", name)
                i.putExtra("chatId", id)

                startActivity(i)
            }

            override fun onError(fromFile: Boolean, position: Int) {
                chatDialogFragment.show(parentFragmentManager.beginTransaction(), "AddChatDialog")
            }

            override fun onCanceled() {
                /* unused */
            }

            override fun onDelete(position: Int) {
                /* unused */
            }

            override fun onDuplicate(position: Int) {
                /* unused */
            }
        })
        chatDialogFragment.show(parentFragmentManager.beginTransaction(), "AddChatDialog")
    }

    private fun performAction(model: String, endpointUrl: String, endpointName: String, suggestedChatName: String, avatarType: String, avatarId: String, assistantName: String, function: (endpointName: String, suggestedChatName: String, model: String, avatarType: String, avatarId: String, assistantName: String) -> Unit) {
        val apiObject = apiEndpointPreferences?.getApiEndpointByUrlOrNull(mContext ?: return, endpointUrl)

        if (apiObject != null) {
            function(apiObject.label, suggestedChatName, model, avatarType, avatarId, assistantName)
        } else {
            val apiEndpointDialog: EditApiEndpointDialogFragment = EditApiEndpointDialogFragment.newInstance(endpointName, endpointUrl, "", -1)
            apiEndpointDialog.setListener(object : EditApiEndpointDialogFragment.StateChangesListener {
                override fun onAdd(apiEndpoint: ApiEndpointObject) {
                    apiEndpointPreferences?.setApiEndpoint(mContext ?: return, apiEndpoint)
                    function(apiEndpoint.label, suggestedChatName, model, avatarType, avatarId, assistantName)
                }

                override fun onEdit(oldLabel: String, apiEndpoint: ApiEndpointObject, position: Int) {
                    /* unused */
                }

                override fun onDelete(position: Int, id: String) {
                    /* unused */
                }

                override fun onError(message: String, position: Int) {
                    apiEndpointDialog.show(parentFragmentManager, "EditApiEndpointDialogFragment")
                }
            })
            apiEndpointDialog.show(parentFragmentManager, "EditApiEndpointDialogFragment")
        }
    }
}
