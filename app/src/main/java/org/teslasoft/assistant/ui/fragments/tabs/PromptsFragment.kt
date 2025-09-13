/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eightbitlab.com.blurview.BlurView
import org.teslasoft.assistant.Api
import org.teslasoft.assistant.Config.Companion.API_ENDPOINT
import org.teslasoft.assistant.R
import org.teslasoft.assistant.model.SimpleResponseModel
import org.teslasoft.assistant.preferences.DeviceInfoProvider
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.adapters.PromptAdapterNew
import org.teslasoft.assistant.ui.fragments.dialogs.PostPromptDialogFragment
import org.teslasoft.assistant.util.Hash
import org.teslasoft.assistant.util.WindowInsetsUtil
import org.teslasoft.core.api.network.RequestNetwork
import java.net.URLEncoder
import java.util.EnumSet
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class PromptsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var fieldSearch: EditText? = null
    private var btnPost: ExtendedFloatingActionButton? = null
    private var promptsList: RecyclerView? = null
    private var promptsAdapter: PromptAdapterNew? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var refreshButton: MaterialButton? = null
    private var btnDetails: MaterialButton? = null
    private var noInternetLayout: LinearLayout? = null
    private var progressbar: LoadingIndicator? = null
    private var catAll: LinearLayout? = null
    private var catDevelopment: LinearLayout? = null
    private var catMusic: LinearLayout? = null
    private var catArt: LinearLayout? = null
    private var catCulture: LinearLayout? = null
    private var catBusiness: LinearLayout? = null
    private var catGaming: LinearLayout? = null
    private var catEducation: LinearLayout? = null
    private var catHistory: LinearLayout? = null
    private var catFood: LinearLayout? = null
    private var catTourism: LinearLayout? = null
    private var catProductivity: LinearLayout? = null
    private var catTools: LinearLayout? = null
    private var catEntertainment: LinearLayout? = null
    private var catSport: LinearLayout? = null
    private var catHealth: LinearLayout? = null
    private var searchBar: ConstraintLayout? = null
    private var btnAllModels: MaterialButton? = null
    private var btnTextModel: MaterialButton? = null
    private var btnImageModel: MaterialButton? = null
    private var btnSearch: ImageButton? = null
    private var headerBackground: BlurView? = null
    private var promptsContainer: NestedScrollView? = null

    private var query = ""
    private var selectedCategory = "all"
    private var model = "all"
    private var networkError = ""
    private var onAttach: Boolean = false
    private var prompts: ArrayList<HashMap<String, String>> = arrayListOf()

    private var mContext: Context? = null
    private var requestNetwork: RequestNetwork? = null

    private var isInitialized: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_prompts, container, false)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private val postPromptListener: PostPromptDialogFragment.StateChangesListener = object : PostPromptDialogFragment.StateChangesListener {
        override fun onFormFilled(
            name: String,
            title: String,
            desc: String,
            prompt: String,
            type: String,
            category: String
        ) {
            val androidId = DeviceInfoProvider.getAndroidId(mContext ?: return)
            val deviceIdHash = Hash.hash(androidId) // Add a layer of privacy by hashing the Android ID
            val appVersionCode: String = (mContext?.packageManager?.getPackageInfo(mContext?.packageName ?: "", 0)?.longVersionCode ?: 0L).toString()

            // Device version and app version are used to ensure compatibility and track changes and prevent abuse.
            // Android ID is unique for each app installed on the device and does not disclose any information about the device.
            // Android ID is persistent and used to track abuse and ban devices from posting since no personal information about user (like name or email) is collected.
            // The system can detect abuse by reading the other values like information about prompt send by the user.
            // App version will be used to prevent posting from the old app versions and gracefully notify users about update requirements.
            // Android ID does not used for analytical purposes nor shared with AI models or third-party services.
            // Using Android ID instead of installation ID will prevent users from abusing the system by reinstalling the app.
            // Additionally, if user connects from the another device sharing the same IP address, a ban will be immediately issued to the new device.

            // These changes will be implemented gradually so users will not receive sudden errors when posting prompts.
            // Another layer of security and abuse prevention will be achieved by running the moderation API.

            requestNetwork?.startRequestNetwork(
                "GET",
                "${API_ENDPOINT}/post.php?api_key=${Api.TESLASOFT_API_KEY}&name=${Base64.encode(name.toByteArray())}&title=${Base64.encode(title.toByteArray())}&desc=${Base64.encode(desc.toByteArray())}&prompt=${Base64.encode(prompt.toByteArray())}&type=$type&category=$category&deviceId=$deviceIdHash&appVersion=$appVersionCode&mode=base64",
                "A",
                promptPostListener
            )
        }

        override fun onFormError(
            name: String,
            title: String,
            desc: String,
            prompt: String,
            type: String,
            category: String
        ) {
            Toast.makeText(mContext?: return, getString(R.string.label_error_fill_all_blanks), Toast.LENGTH_SHORT).show()

            val promptDialog: PostPromptDialogFragment =
                PostPromptDialogFragment.newInstance(name, title, desc, prompt, type, category)
            promptDialog.setStateChangedListener(this)
            promptDialog.show(parentFragmentManager.beginTransaction(), "PromptDialog")
        }

        override fun onCanceled() {
            /* unused */
        }
    }

    private val searchDataListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            try {
                prompts = Gson().fromJson(
                    message, TypeToken.getParameterized(ArrayList::class.java, HashMap::class.java).type
                )

                filter(prompts)

                networkError = ""
            } catch (e: Exception) {
                networkError = e.stackTraceToString()

                noInternetLayout?.visibility = View.VISIBLE
                promptsContainer?.visibility = View.GONE
                progressbar?.visibility = View.GONE
                Toast.makeText(mContext?: return, getString(R.string.label_server_error), Toast.LENGTH_SHORT).show()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            networkError = message

            noInternetLayout?.visibility = View.VISIBLE
            promptsContainer?.visibility = View.GONE
            progressbar?.visibility = View.GONE
        }
    }

    private val promptPostListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            try {
                val response = Gson().fromJson(message, TypeToken.getParameterized(SimpleResponseModel::class.java).type) as SimpleResponseModel

                if (response.code == 200) {
                    loadData()
                } else if (response.code == 104) {
                    MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                        .setTitle(R.string.label_error)
                        .setMessage(getString(R.string.msg_post_prompt_spam))
                        .setPositiveButton(R.string.btn_close) { _, _ -> }
                        .show()
                } else if (response.code == 103) {
                    MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                        .setTitle(R.string.label_error)
                        .setMessage(getString(R.string.msg_post_prompt_inappropriate))
                        .setPositiveButton(R.string.btn_close) { _, _ -> }
                        .show()
                } else if (response.code == 102) {
                    MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                        .setTitle(R.string.label_error)
                        .setMessage(getString(R.string.msg_post_prompt_ban))
                        .setPositiveButton(R.string.btn_close) { _, _ -> }
                        .show()
                } else if (response.code == 101) {
                    MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                        .setTitle(R.string.label_error)
                        .setMessage(getString(R.string.msg_post_prompt_outdated))
                        .setPositiveButton(R.string.btn_close) { _, _ -> }
                        .show()
                } else if (response.code == 400) {
                    MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                        .setTitle(R.string.label_error)
                        .setMessage(getString(R.string.msg_post_prompt_weirdest_error))
                        .setPositiveButton(R.string.btn_close) { _, _ -> }
                        .show()
                } else if (response.code == 401) {
                    MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                        .setTitle(R.string.label_error)
                        .setMessage(getString(R.string.msg_post_prompt_api_key_invalid))
                        .setPositiveButton(R.string.btn_close) { _, _ -> }
                        .show()
                }
            } catch (e: Exception) {
                MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                    .setTitle(R.string.label_error)
                    .setMessage(getString(R.string.msg_failed_to_post_prompt) + getString(R.string.msg_post_prompt_report_error) + e.toString() + e.stackTraceToString())
                    .setPositiveButton(R.string.btn_close) { _, _ -> }
                    .show()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            MaterialAlertDialogBuilder(mContext?: return, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_error)
                .setMessage(getString(R.string.msg_failed_to_post_prompt))
                .setPositiveButton(R.string.btn_close) { _, _ -> }
                .show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(plist: ArrayList<HashMap<String, String>>) {
        if (selectedCategory == "all") {
            promptsAdapter = PromptAdapterNew(plist, this)

            promptsList?.adapter = promptsAdapter

            promptsAdapter?.notifyDataSetChanged()
        } else {
            val filtered = arrayListOf<HashMap<String, String>>()

            for (map: HashMap<String, String> in plist) {
                if (selectedCategory == map["category"]) {
                    filtered.add(map)
                }
            }

            promptsAdapter = PromptAdapterNew(filtered, this)
            promptsList?.adapter = promptsAdapter
            promptsAdapter?.notifyDataSetChanged()
        }

        noInternetLayout?.visibility = View.GONE
        promptsContainer?.visibility = View.VISIBLE
        progressbar?.visibility = View.GONE
    }

    private var rootView: View? = null
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view

        WindowInsetsUtil.adjustPaddings((mContext as Activity?) ?: return, rootView, R.id.prompts_header, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS))
        WindowInsetsUtil.adjustPaddings((mContext as Activity?) ?: return, rootView, R.id.refresh_search, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS))

        btnSearch = view.findViewById(R.id.btn_search)
        fieldSearch = view.findViewById(R.id.field_search)
        btnPost = view.findViewById(R.id.btn_add_prompt)
        promptsList = view.findViewById(R.id.prompts)
        refreshLayout = view.findViewById(R.id.refresh_search)
        refreshButton = view.findViewById(R.id.btn_reconnect)
        btnDetails = view.findViewById(R.id.btn_show_details)
        noInternetLayout = view.findViewById(R.id.no_internet)
        progressbar = view.findViewById(R.id.loading)
        catAll = view.findViewById(R.id.cat_all)
        catDevelopment = view.findViewById(R.id.cat_development)
        catMusic = view.findViewById(R.id.cat_music)
        catArt = view.findViewById(R.id.cat_art)
        catCulture = view.findViewById(R.id.cat_culture)
        catBusiness = view.findViewById(R.id.cat_business)
        catGaming = view.findViewById(R.id.cat_gaming)
        catEducation = view.findViewById(R.id.cat_education)
        catHistory = view.findViewById(R.id.cat_history)
        catFood = view.findViewById(R.id.cat_food)
        catTourism = view.findViewById(R.id.cat_tourism)
        catProductivity = view.findViewById(R.id.cat_productivity)
        catTools = view.findViewById(R.id.cat_tools)
        catEntertainment = view.findViewById(R.id.cat_entertainment)
        catSport = view.findViewById(R.id.cat_sport)
        catHealth = view.findViewById(R.id.cat_health)
        searchBar = view.findViewById(R.id.search_bar)

        btnAllModels = view.findViewById(R.id.btn_all_models)
        btnTextModel = view.findViewById(R.id.btn_text_model)
        btnImageModel = view.findViewById(R.id.btn_image_model)

        headerBackground = view.findViewById(R.id.prompts_header)
        promptsContainer = view.findViewById(R.id.prompts_container)

        val decorView: View = activity?.window?.decorView?: return
        val rootView = decorView.findViewById<View>(android.R.id.content)
        val windowBackground: Drawable = decorView.background

        headerBackground?.setupWith(rootView as ViewGroup)?.setFrameClearDrawable(windowBackground)?.setBlurRadius(16f)

        promptsList?.layoutManager = LinearLayoutManager(mContext)

        Thread {
            while (!onAttach) {
                Thread.sleep(100)
            }

            (mContext as Activity?)?.runOnUiThread {
                initLogic()
            }
        }.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initLogic() {
        updateModelsPanel(R.color.accent_900, R.color.accent_100, R.color.accent_100, R.color.window_background, R.color.accent_900, R.color.accent_900)

        initializeCat()

        promptsList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val topRowVerticalPosition: Int = if (prompts.isEmpty() || promptsList == null || promptsList?.childCount == 0) 0 else promptsList?.getChildAt(0)!!.top

                if (dy < 0 && topRowVerticalPosition >= 0) {
                    btnPost?.extend()
                } else {
                    btnPost?.shrink()
                }

                refreshLayout?.isEnabled = dy < 0 && topRowVerticalPosition >= 0
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        reloadAmoled(mContext?: return)

        noInternetLayout?.visibility = View.GONE
        promptsContainer?.visibility = View.GONE
        progressbar?.visibility = View.VISIBLE

        refreshButton?.setOnClickListener {
            loadData()
        }

        refreshLayout?.setColorSchemeResources(R.color.accent_900)
        refreshLayout?.setProgressBackgroundColorSchemeColor(
            SurfaceColors.SURFACE_2.getColor(mContext ?: return)
        )

        refreshLayout?.setSize(SwipeRefreshLayout.LARGE)
        refreshLayout?.setOnRefreshListener(this)

        promptsAdapter = PromptAdapterNew(prompts, this)

        promptsList?.adapter = promptsAdapter
        promptsAdapter?.notifyDataSetChanged()

        requestNetwork = RequestNetwork((mContext as Activity?)?: return)

        btnSearch?.setImageResource(R.drawable.ic_search)

        fieldSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* unused */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s.toString()

                if (query.lowercase().contains("type:gpt")) {
                    updateModelsPanel(R.color.accent_100, R.color.accent_900, R.color.accent_100, R.color.accent_900, R.color.window_background, R.color.accent_900)
                } else if (query.lowercase().contains("type:dall-e")) {
                    updateModelsPanel(R.color.accent_100, R.color.accent_100, R.color.accent_900, R.color.accent_900, R.color.accent_900, R.color.window_background)
                } else {
                    updateModelsPanel(R.color.accent_900, R.color.accent_100, R.color.accent_100, R.color.window_background, R.color.accent_900, R.color.accent_900)
                }

                loadData()
            }

            override fun afterTextChanged(s: Editable?) {
                /* unused */
            }
        })

        btnPost?.setOnClickListener {
            val promptDialog: PostPromptDialogFragment = PostPromptDialogFragment.newInstance("", "", "", "", "", "")
            promptDialog.setStateChangedListener(postPromptListener)
            promptDialog.show(parentFragmentManager.beginTransaction(), "PromptDialog")
        }

        fieldSearch?.background = getDisabledDrawable(fieldSearch?.background!!)

        btnAllModels?.setOnClickListener {
            fieldSearch?.setText("")
            model = "all"
            updateModelsPanel(R.color.accent_900, R.color.accent_100, R.color.accent_100, R.color.window_background, R.color.accent_900, R.color.accent_900)
        }

        btnTextModel?.setOnClickListener {
            fieldSearch?.setText("type:gpt")
            model = "type:gpt"
            updateModelsPanel(R.color.accent_100, R.color.accent_900, R.color.accent_100, R.color.accent_900, R.color.window_background, R.color.accent_900)
        }

        btnImageModel?.setOnClickListener {
            fieldSearch?.setText("type:dall-e")
            model = "type:dall-e"
            updateModelsPanel(R.color.accent_100, R.color.accent_100, R.color.accent_900, R.color.accent_900, R.color.accent_900, R.color.window_background)
        }

        btnDetails?.setOnClickListener {
            MaterialAlertDialogBuilder(mContext?: return@setOnClickListener, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_error_details)
                .setMessage(networkError)
                .setPositiveButton(R.string.btn_close) { _, _ -> }
                .show()
        }

        if (!isInitialized) {
            loadData()
            isInitialized = true
        } else {
            promptsContainer?.visibility = View.VISIBLE
            progressbar?.visibility = View.GONE
        }
    }

    private fun updateModelsPanel(btnAllBg: Int, btnTextBg: Int, btnImageBg: Int, btnAllText: Int, btnTextText: Int, btnImageText: Int) {
        btnTextModel?.backgroundTintList = ResourcesCompat.getColorStateList(mContext?.resources?: return, btnTextBg, mContext?.theme)
        btnImageModel?.backgroundTintList = ResourcesCompat.getColorStateList(mContext?.resources?: return, btnImageBg, mContext?.theme)
        btnAllModels?.backgroundTintList = ResourcesCompat.getColorStateList(mContext?.resources?: return, btnAllBg, mContext?.theme)
        btnTextModel?.setTextColor(ResourcesCompat.getColor(mContext?.resources?: return, btnTextText, mContext?.theme))
        btnImageModel?.setTextColor(ResourcesCompat.getColor(mContext?.resources?: return, btnImageText, mContext?.theme))
        btnAllModels?.setTextColor(ResourcesCompat.getColor(mContext?.resources?: return, btnAllText, mContext?.theme))
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (mContext?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    fun reloadAmoled(context: Context) {
        if (isDarkThemeEnabled() && Preferences.getPreferences(context, "").getAmoledPitchBlack()) {
            searchBar?.background = ResourcesCompat.getDrawable(mContext?.resources?: return, R.drawable.btn_accent_tonal_amoled, context.theme)!!
        } else {
            searchBar?.background = getDisabledDrawable(ResourcesCompat.getDrawable(mContext?.resources?: return, R.drawable.btn_accent_tonal, context.theme)!!)
        }
    }

    private fun initializeCat() {
        catAll?.setOnClickListener {
            clearSelection()
            catAll?.setBackgroundResource(R.drawable.cat_all_active)
            selectedCategory = "all"
            filter(prompts)
        }
        catDevelopment?.setOnClickListener {
            clearSelection()
            catDevelopment?.setBackgroundResource(R.drawable.cat_development_active)
            selectedCategory = "development"
            filter(prompts)
        }
        catMusic?.setOnClickListener {
            clearSelection()
            catMusic?.setBackgroundResource(R.drawable.cat_music_active)
            selectedCategory = "music"
            filter(prompts)
        }
        catArt?.setOnClickListener {
            clearSelection()
            catArt?.setBackgroundResource(R.drawable.cat_art_active)
            selectedCategory = "art"
            filter(prompts)
        }
        catCulture?.setOnClickListener {
            clearSelection()
            catCulture?.setBackgroundResource(R.drawable.cat_culture_active)
            selectedCategory = "culture"
            filter(prompts)
        }
        catBusiness?.setOnClickListener {
            clearSelection()
            catBusiness?.setBackgroundResource(R.drawable.cat_business_active)
            selectedCategory = "business"
            filter(prompts)
        }
        catGaming?.setOnClickListener {
            clearSelection()
            catGaming?.setBackgroundResource(R.drawable.cat_gaming_active)
            selectedCategory = "gaming"
            filter(prompts)
        }
        catEducation?.setOnClickListener {
            clearSelection()
            catEducation?.setBackgroundResource(R.drawable.cat_education_active)
            selectedCategory = "education"
            filter(prompts)
        }
        catHistory?.setOnClickListener {
            clearSelection()
            catHistory?.setBackgroundResource(R.drawable.cat_history_active)
            selectedCategory = "history"
            filter(prompts)
        }
        catFood?.setOnClickListener {
            clearSelection()
            catFood?.setBackgroundResource(R.drawable.cat_food_active)
            selectedCategory = "food"
            filter(prompts)
        }
        catTourism?.setOnClickListener {
            clearSelection()
            catTourism?.setBackgroundResource(R.drawable.cat_tourism_active)
            selectedCategory = "tourism"
            filter(prompts)
        }
        catProductivity?.setOnClickListener {
            clearSelection()
            catProductivity?.setBackgroundResource(R.drawable.cat_productivity_active)
            selectedCategory = "productivity"
            filter(prompts)
        }
        catTools?.setOnClickListener {
            clearSelection()
            catTools?.setBackgroundResource(R.drawable.cat_tools_active)
            selectedCategory = "tools"
            filter(prompts)
        }
        catEntertainment?.setOnClickListener {
            clearSelection()
            catEntertainment?.setBackgroundResource(R.drawable.cat_entertainment_active)
            selectedCategory = "entertainment"
            filter(prompts)
        }
        catSport?.setOnClickListener {
            clearSelection()
            catSport?.setBackgroundResource(R.drawable.cat_sport_active)
            selectedCategory = "sport"
            filter(prompts)
        }
        catHealth?.setOnClickListener {
            clearSelection()
            catHealth?.setBackgroundResource(R.drawable.cat_health_active)
            selectedCategory = "health"
            filter(prompts)
        }
    }

    private fun clearSelection () {
        catAll?.setBackgroundResource(R.drawable.cat_all)
        catDevelopment?.setBackgroundResource(R.drawable.cat_development)
        catMusic?.setBackgroundResource(R.drawable.cat_music)
        catArt?.setBackgroundResource(R.drawable.cat_art)
        catCulture?.setBackgroundResource(R.drawable.cat_culture)
        catBusiness?.setBackgroundResource(R.drawable.cat_business)
        catGaming?.setBackgroundResource(R.drawable.cat_gaming)
        catEducation?.setBackgroundResource(R.drawable.cat_education)
        catHistory?.setBackgroundResource(R.drawable.cat_history)
        catFood?.setBackgroundResource(R.drawable.cat_food)
        catTourism?.setBackgroundResource(R.drawable.cat_tourism)
        catProductivity?.setBackgroundResource(R.drawable.cat_productivity)
        catTools?.setBackgroundResource(R.drawable.cat_tools)
        catEntertainment?.setBackgroundResource(R.drawable.cat_entertainment)
        catSport?.setBackgroundResource(R.drawable.cat_sport)
        catHealth?.setBackgroundResource(R.drawable.cat_health)
    }

    private fun loadData() {
        requestNetwork?.startRequestNetwork("GET", "${API_ENDPOINT}/search.php?api_key=${Api.TESLASOFT_API_KEY}&query=$query", "A", searchDataListener)
        noInternetLayout?.visibility = View.GONE
        promptsContainer?.visibility = View.GONE
        progressbar?.visibility = View.VISIBLE
    }

    override fun onRefresh() {
        refreshLayout?.isRefreshing = false
        loadData()
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return SurfaceColors.SURFACE_5.getColor(mContext ?: return 0)
    }

    override fun onAttach(context: Context) {
        mContext = context
        onAttach = true

        if (rootView != null) WindowInsetsUtil.adjustPaddings((mContext as Activity?) ?: return, rootView, R.id.prompts_header, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS))
        if (rootView != null) WindowInsetsUtil.adjustPaddings((mContext as Activity?) ?: return, rootView, R.id.refresh_search, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS))

        super.onAttach(context)
    }

    override fun onDetach() {
        mContext = null
        onAttach = false
        super.onDetach()
    }
}
