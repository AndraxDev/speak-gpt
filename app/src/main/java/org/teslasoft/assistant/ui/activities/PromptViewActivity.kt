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

package org.teslasoft.assistant.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.Api
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Logger
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.assistant.AssistantActivity
import org.teslasoft.assistant.util.TestDevicesAds.Companion.TEST_DEVICES
import org.teslasoft.core.api.network.RequestNetwork
import java.net.MalformedURLException
import java.net.URL


class PromptViewActivity : FragmentActivity(), SwipeRefreshLayout.OnRefreshListener {

    private var activityTitle: TextView? = null

    private var content: ConstraintLayout? = null

    private var progressBar: ProgressBar? = null

    private var noInternetLayout: ConstraintLayout? = null

    private var btnReconnect: MaterialButton? = null

    private var btnShowDetails: MaterialButton? = null

    private var promptBy: TextView? = null

    private var promptText: EditText? = null

    private var textCat: TextView? = null

    private var refreshPage: SwipeRefreshLayout? = null

    private var requestNetwork: RequestNetwork? = null

    private var btnCopy: MaterialButton? = null

    private var btnLike: MaterialButton? = null

    private var btnTry: MaterialButton? = null

    private var btnFlag: ImageButton? = null

    private var promptBg: ConstraintLayout? = null

    private var promptActions: ConstraintLayout? = null

    private var id = ""

    private var title = ""

    private var networkError = ""

    private var likeState = false

    private var settings: SharedPreferences? = null

    private var promptFor: String? = null

    private var btnBack: ImageButton? = null

    private var root: ConstraintLayout? = null

    private var ad: LinearLayout? = null

    private val dataListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            noInternetLayout?.visibility = View.GONE
            progressBar?.visibility = View.GONE
            content?.visibility = View.VISIBLE

            try {
                val map: HashMap<String, String> = Gson().fromJson(
                    message, TypeToken.getParameterized(HashMap::class.java, String::class.java, String::class.java).type
                )

                promptText?.setText(map["prompt"])
                promptBy?.text = "By " + map["author"]
                btnLike?.text = map["likes"]
                promptFor = map["type"]

                textCat?.text = when (map["category"]) {
                    "development" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_development))
                    "music" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_music))
                    "art" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_art))
                    "culture" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_culture))
                    "business" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_business))
                    "gaming" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_gaming))
                    "education" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_education))
                    "history" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_history))
                    "health" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_health))
                    "food" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_food))
                    "tourism" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_tourism))
                    "productivity" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_productivity))
                    "tools" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_tools))
                    "entertainment" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_entertainment))
                    "sport" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_sport))
                    else -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_uncat))
                }

                networkError = ""
            } catch (e: Exception) {
                networkError = e.printStackTrace().toString()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            networkError = message

            noInternetLayout?.visibility = View.VISIBLE
            progressBar?.visibility = View.GONE
            content?.visibility = View.GONE
        }
    }

    private val likeListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            btnLike?.isEnabled = true
            likeState = true

            settings?.edit()?.putBoolean(id, true)?.apply()

            btnLike?.setIconResource(R.drawable.ic_like)

            loadData()
        }

        override fun onErrorResponse(tag: String, message: String) {
            btnLike?.isEnabled = true

            Toast.makeText(this@PromptViewActivity, "Sorry, action failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val dislikeListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            btnLike?.isEnabled = true
            likeState = false

            settings?.edit()?.putBoolean(id, false)?.apply()

            btnLike?.setIconResource(R.drawable.ic_like_outline)

            loadData()
        }

        override fun onErrorResponse(tag: String, message: String) {
            btnLike?.isEnabled = true

            Toast.makeText(this@PromptViewActivity, "Sorry, action failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        reloadAmoled()

        // Reset preferences singleton
        Preferences.getPreferences(this, "")
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() &&  Preferences.getPreferences(this, "").getAmoledPitchBlack()) {
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme)
            window.setBackgroundDrawableResource(R.color.amoled_window_background)

            root?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))

            activityTitle?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme))

            promptBg?.setBackgroundResource(R.drawable.btn_accent_24_amoled)

            promptActions?.setBackgroundResource(R.drawable.btn_accent_24_amoled)

            btnBack?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4_amoled
                )!!, this
            )

            btnFlag?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4_amoled
                )!!, this
            )
        } else {
            window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
            window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)
            window.setBackgroundDrawableResource(R.color.window_background)

            root?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))

            activityTitle?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))

            promptBg?.background = getDarkDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_24
                )!!
            )

            promptActions?.background = getDarkDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_24
                )!!
            )

            btnBack?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4
                )!!, this
            )

            btnFlag?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4
                )!!, this
            )
        }
    }

    private fun getDarkDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), SurfaceColors.SURFACE_2.getColor(this))
        return drawable
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras: Bundle? = intent.extras

        if (extras == null) {
            checkForURI()
        } else {
            id = extras.getString("id", "")
            title = extras.getString("title", "")

            if (id == "" || title == "") {
                checkForURI()
            } else {
                allowLaunch()
            }
        }
    }

    private fun checkForURI() {
        val uri = intent.data
        try {
            val url = URL(uri?.scheme, uri?.host, uri?.path)

            val paths = url.path.split("/")
            id = paths[paths.size - 1]

            allowLaunch()
        } catch (e: MalformedURLException) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun allowLaunch() {
        setContentView(R.layout.activity_view_prompt)

        window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)

        initUI()

        Thread {
            runOnUiThread {
                initLogic()
            }
        }.start()
    }

    private fun initUI() {
        activityTitle = findViewById(R.id.activity_view_title)
        content = findViewById(R.id.view_content)
        progressBar = findViewById(R.id.progress_bar_view)
        noInternetLayout = findViewById(R.id.no_internet)
        btnReconnect = findViewById(R.id.btn_reconnect)
        btnShowDetails = findViewById(R.id.btn_show_details)
        promptBy = findViewById(R.id.prompt_by)
        promptText = findViewById(R.id.prompt_text)
        refreshPage = findViewById(R.id.refresh_page)
        btnFlag = findViewById(R.id.btn_flag)
        btnCopy = findViewById(R.id.btn_copy)
        btnLike = findViewById(R.id.btn_like)
        btnTry = findViewById(R.id.btn_try)
        textCat = findViewById(R.id.text_cat)
        btnBack = findViewById(R.id.btn_back)
        root = findViewById(R.id.root)

        promptBg = findViewById(R.id.prompt_bg)
        promptActions = findViewById(R.id.prompt_actions)

        noInternetLayout?.visibility = View.GONE
        progressBar?.visibility = View.VISIBLE
        content?.visibility = View.GONE

        reloadAmoled()
    }

    private fun initLogic() {
        activityTitle?.isSelected = true

        btnFlag?.background = getDarkAccentDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.btn_accent_tonal_v4
            )!!, this
        )

        btnBack?.background = getDarkAccentDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.btn_accent_tonal_v4
            )!!, this
        )

        btnFlag?.setImageResource(R.drawable.ic_flag)

        btnBack?.setOnClickListener {
            finish()
        }

        settings = getSharedPreferences("likes", MODE_PRIVATE)

        likeState = settings?.getBoolean(id, false) == true

        refreshPage?.setColorSchemeResources(R.color.accent_900)
        refreshPage?.setProgressBackgroundColorSchemeColor(
            SurfaceColors.SURFACE_2.getColor(this)
        )
        refreshPage?.setSize(SwipeRefreshLayout.LARGE)

        refreshPage?.setOnRefreshListener(this)

        if (likeState) {
            btnLike?.setIconResource(R.drawable.ic_like)
        } else {
            btnLike?.setIconResource(R.drawable.ic_like_outline)
        }

        btnCopy?.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("prompt", promptText?.text.toString())
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        btnLike?.setOnClickListener {
            if (likeState) {
                requestNetwork?.startRequestNetwork("GET", "${Config.API_ENDPOINT}/dislike.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", dislikeListener)
            } else {
                requestNetwork?.startRequestNetwork("GET", "${Config.API_ENDPOINT}/like.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", likeListener)
            }

            btnLike?.isEnabled = false
        }

        btnTry?.setOnClickListener {
            if (promptFor == "GPT") {
                val i = Intent(
                    this,
                    AssistantActivity::class.java
                ).setAction(Intent.ACTION_VIEW)
                i.putExtra("prompt", promptText?.text.toString())
                startActivity(i)
            } else {
                val i = Intent(
                    this,
                    AssistantActivity::class.java
                ).setAction(Intent.ACTION_VIEW)
                i.putExtra("prompt", "/imagine " + promptText?.text.toString())
                i.putExtra("FORCE_SLASH_COMMANDS_ENABLED", true)
                startActivity(i)
            }
        }

        btnFlag?.setOnClickListener {
            val i = Intent(this, ReportAbuseActivity::class.java).setAction(Intent.ACTION_VIEW)
            i.putExtra("id", id)
            startActivity(i)
        }

        requestNetwork = RequestNetwork(this)

        activityTitle?.text = title

        btnReconnect?.setOnClickListener { loadData() }

        val preferences: Preferences = Preferences.getPreferences(this, "")

        ad = findViewById(R.id.ad)

        if (preferences.getAdsEnabled()) {
            MobileAds.initialize(this) { /* unused */ }
            Logger.log(this, "ads", "AdMob", "info", "Ads initialized")

            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(TEST_DEVICES)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)

            val adView = AdView(this)
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
                    Logger.log(this@PromptViewActivity, "ads", "AdMob", "error", "Ad failed to load: ${error.message}")
                }

                override fun onAdLoaded() {
                    ad?.visibility = View.VISIBLE
                    Logger.log(this@PromptViewActivity, "ads", "AdMob", "info", "Ad loaded successfully")
                }
            }
        } else {
            ad?.visibility = View.GONE
            Logger.log(this, "ads", "AdMob", "info", "Ads initialization skipped: Ads are disabled")
        }

        btnShowDetails?.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("Error details")
                .setMessage(networkError)
                .setPositiveButton("Close") { _, _ -> }
                .show()
        }

        loadData()
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return if (isDarkThemeEnabled() &&  Preferences.getPreferences(context, "").getAmoledPitchBlack()) {
            ResourcesCompat.getColor(context.resources, R.color.amoled_accent_50, context.theme)
        } else {
            SurfaceColors.SURFACE_4.getColor(context)
        }
    }

    override fun onRefresh() {
        refreshPage?.isRefreshing = false

        loadData()
    }

    private fun loadData() {
        noInternetLayout?.visibility = View.GONE
        progressBar?.visibility = View.VISIBLE
        content?.visibility = View.GONE

        requestNetwork?.startRequestNetwork("GET", "${Config.API_ENDPOINT}/prompt.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", dataListener)
    }
}
