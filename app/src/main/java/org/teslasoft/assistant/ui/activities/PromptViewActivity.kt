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

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Fade
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.View
import android.view.WindowInsets
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.Api
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.assistant.AssistantActivity
import org.teslasoft.core.api.network.RequestNetwork
import java.net.MalformedURLException
import java.net.URL

class PromptViewActivity : FragmentActivity(), SwipeRefreshLayout.OnRefreshListener {

    private var activityTitle: TextView? = null
    private var progressBar: CircularProgressIndicator? = null
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
    private var cat = ""
    private var networkError = ""
    private var likeState = false
    private var settings: SharedPreferences? = null
    private var promptFor: String? = null
    private var btnBack: ImageButton? = null
    private var root: ConstraintLayout? = null
    private var uiIsUpdated = false

    private val dataListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        @SuppressLint("SetTextI18n")
        override fun onResponse(tag: String, message: String) {
            noInternetLayout?.visibility = View.GONE
            refreshPage?.isRefreshing = false

            try {
                val map: HashMap<String, String> = Gson().fromJson(
                    message, TypeToken.getParameterized(HashMap::class.java, String::class.java, String::class.java).type
                )

                promptText?.setText(map["prompt"])
                promptBy?.text = "By " + map["author"]
                btnLike?.text = map["likes"]
                promptFor = map["type"]

                title = map["name"].toString()
                activityTitle?.text = title

                updateUiFromCat(map["category"].toString())

                Handler(Looper.getMainLooper()).postDelayed({
                    promptBg?.alpha = 1f
                    val fadeIn: Animation = AnimationUtils.loadAnimation(this@PromptViewActivity, R.anim.fade_in_slow)
                    val fadeOut: Animation = AnimationUtils.loadAnimation(this@PromptViewActivity, R.anim.fade_out_slow)
                    promptBg?.startAnimation(fadeIn)
                    progressBar?.startAnimation(fadeOut)

                    fadeOut.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) { /* UNUSED */ }
                        override fun onAnimationEnd(animation: Animation) {
                            progressBar?.visibility = View.GONE
                        }

                        override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
                    })
                }, 100)

                networkError = ""
            } catch (e: Exception) {
                networkError = e.printStackTrace().toString()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            networkError = message
            refreshPage?.isRefreshing = false
            noInternetLayout?.visibility = View.VISIBLE
        }
    }

    private fun updateUiFromCat(cat: String?) {
        if (uiIsUpdated) return
        textCat?.text = when (cat) {
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

        val bgColor = when (cat) {
            "development" -> ResourcesCompat.getColor(resources, R.color.bg_cat_development, theme)
            "music" -> ResourcesCompat.getColor(resources, R.color.bg_cat_music, theme)
            "art" -> ResourcesCompat.getColor(resources, R.color.bg_cat_art, theme)
            "culture" -> ResourcesCompat.getColor(resources, R.color.bg_cat_culture, theme)
            "business" -> ResourcesCompat.getColor(resources, R.color.bg_cat_business, theme)
            "gaming" -> ResourcesCompat.getColor(resources, R.color.bg_cat_gaming, theme)
            "education" -> ResourcesCompat.getColor(resources, R.color.bg_cat_education, theme)
            "history" -> ResourcesCompat.getColor(resources, R.color.bg_cat_history, theme)
            "health" -> ResourcesCompat.getColor(resources, R.color.bg_cat_health, theme)
            "food" ->ResourcesCompat.getColor(resources, R.color.bg_cat_food, theme)
            "tourism" -> ResourcesCompat.getColor(resources, R.color.bg_cat_tourism, theme)
            "productivity" -> ResourcesCompat.getColor(resources, R.color.bg_cat_productivity, theme)
            "tools" -> ResourcesCompat.getColor(resources, R.color.bg_cat_tools, theme)
            "entertainment" -> ResourcesCompat.getColor(resources, R.color.bg_cat_entertainment, theme)
            "sport" -> ResourcesCompat.getColor(resources, R.color.bg_cat_sport, theme)
            else -> ResourcesCompat.getColor(resources, R.color.bg_grey, theme)
        }

        val colorDrawable = ColorDrawable(bgColor)
        window.setBackgroundDrawable(colorDrawable)

        val tintColor = when (cat) {
            "development" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_development, theme)
            "music" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_music, theme)
            "art" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_art, theme)
            "culture" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_culture, theme)
            "business" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_business, theme)
            "gaming" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_gaming, theme)
            "education" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_education, theme)
            "history" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_history, theme)
            "health" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_health, theme)
            "food" ->ResourcesCompat.getColor(resources, R.color.tint2_cat_food, theme)
            "tourism" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_tourism, theme)
            "productivity" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_productivity, theme)
            "tools" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_tools, theme)
            "entertainment" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_entertainment, theme)
            "sport" -> ResourcesCompat.getColor(resources, R.color.tint2_cat_sport, theme)
            else -> ResourcesCompat.getColor(resources, R.color.tint2_grey, theme)
        }

        val catColor = when (cat) {
            "development" -> ResourcesCompat.getColor(resources, R.color.cat_development, theme)
            "music" -> ResourcesCompat.getColor(resources, R.color.cat_music, theme)
            "art" -> ResourcesCompat.getColor(resources, R.color.cat_art, theme)
            "culture" -> ResourcesCompat.getColor(resources, R.color.cat_culture, theme)
            "business" -> ResourcesCompat.getColor(resources, R.color.cat_business, theme)
            "gaming" -> ResourcesCompat.getColor(resources, R.color.cat_gaming, theme)
            "education" -> ResourcesCompat.getColor(resources, R.color.cat_education, theme)
            "history" -> ResourcesCompat.getColor(resources, R.color.cat_history, theme)
            "health" -> ResourcesCompat.getColor(resources, R.color.cat_health, theme)
            "food" ->ResourcesCompat.getColor(resources, R.color.cat_food, theme)
            "tourism" -> ResourcesCompat.getColor(resources, R.color.cat_tourism, theme)
            "productivity" -> ResourcesCompat.getColor(resources, R.color.cat_productivity, theme)
            "tools" -> ResourcesCompat.getColor(resources, R.color.cat_tools, theme)
            "entertainment" -> ResourcesCompat.getColor(resources, R.color.cat_entertainment, theme)
            "sport" -> ResourcesCompat.getColor(resources, R.color.cat_sport, theme)
            else -> ResourcesCompat.getColor(resources, R.color.grey, theme)
        }

        val tintDrawable1 = GradientDrawable()
        tintDrawable1.shape = GradientDrawable.RECTANGLE
        tintDrawable1.setColor(0x000000)
        tintDrawable1.cornerRadius = dpToPx(24).toFloat()

        val tintDrawable2 = GradientDrawable()
        tintDrawable2.shape = GradientDrawable.RECTANGLE
        tintDrawable2.setColor(0x000000)
        tintDrawable2.cornerRadius = dpToPx(24).toFloat()

        val tintDrawable3 = GradientDrawable()
        tintDrawable3.shape = GradientDrawable.RECTANGLE
        tintDrawable3.setColor(0x000000)
        tintDrawable3.cornerRadius = dpToPx(16).toFloat()

        val tintDrawable4 = GradientDrawable()
        tintDrawable4.shape = GradientDrawable.RECTANGLE
        tintDrawable4.setColor(0x000000)
        tintDrawable4.cornerRadius = dpToPx(16).toFloat()

        val tintDrawable5 = GradientDrawable()
        tintDrawable5.shape = GradientDrawable.RECTANGLE
        tintDrawable5.setColor(0x000000)
        tintDrawable5.cornerRadius = dpToPx(16).toFloat()

        promptBg?.backgroundTintList = ColorStateList.valueOf(tintColor)
        promptActions?.backgroundTintList = ColorStateList.valueOf(tintColor)
        activityTitle?.setTextColor(catColor)
        textCat?.backgroundTintList = ColorStateList.valueOf(tintColor)
        promptBy?.backgroundTintList = ColorStateList.valueOf(tintColor)
        btnLike?.backgroundTintList = ColorStateList.valueOf(bgColor)
        btnLike?.iconTint = ColorStateList.valueOf(catColor)
        btnLike?.setTextColor(catColor)
        btnLike?.rippleColor = ColorStateList.valueOf(catColor)
        btnCopy?.backgroundTintList = ColorStateList.valueOf(bgColor)
        btnCopy?.iconTint = ColorStateList.valueOf(catColor)
        btnCopy?.setTextColor(catColor)
        btnCopy?.rippleColor = ColorStateList.valueOf(catColor)
        btnTry?.backgroundTintList = ColorStateList.valueOf(catColor)
        btnTry?.iconTint = ColorStateList.valueOf(bgColor)
        btnTry?.setTextColor(bgColor)
        btnTry?.rippleColor = ColorStateList.valueOf(bgColor)

        progressBar?.setIndicatorColor(catColor)

        btnFlag?.setColorFilter(catColor)
        btnBack?.setColorFilter(catColor)
        uiIsUpdated = true
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
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

            Toast.makeText(this@PromptViewActivity, getString(R.string.label_sorry_action_failed), Toast.LENGTH_SHORT).show()
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

            Toast.makeText(this@PromptViewActivity, getString(R.string.label_sorry_action_failed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        // Reset preferences singleton
        Preferences.getPreferences(this, "")
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
        if (Build.VERSION.SDK_INT >= 30) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                supportFinishAfterTransition()
            }
        }

        val transitionSet = TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition) as TransitionSet

        // Exclude the child views from animations
        transitionSet.excludeTarget(R.id.prompt_bg, true)

        window.sharedElementEnterTransition = transitionSet
        window.sharedElementReturnTransition = transitionSet

        // Create an enter transition for non-shared elements if needed
        val windowTransition = Fade().apply {
            duration = 500
            excludeTarget("prompt_tile", true)
        }

        // Set up enter and return transitions
        window.enterTransition = windowTransition
        window.returnTransition = windowTransition

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_view_prompt)

        val extras: Bundle? = intent.extras

        if (extras == null) {
            checkForURI()
        } else {
            id = extras.getString("id", "")
            title = extras.getString("title", "")
            cat = extras.getString("category", "")

            this@PromptViewActivity.setTitle(extras.getString("title", ""))

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
            supportFinishAfterTransition()
        }
    }

    @Suppress("DEPRECATION")
    private fun allowLaunch() {
        if (Build.VERSION.SDK_INT < 30) {
            window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)
        }

        initUI()

        Thread {
            runOnUiThread {
                initLogic()
            }
        }.start()
    }

    private fun initUI() {
        activityTitle = findViewById(R.id.activity_view_title)
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
        updateUiFromCat(cat)
    }

    private fun initLogic() {
        activityTitle?.isSelected = true

        btnFlag?.setImageResource(R.drawable.ic_flag)
        btnBack?.setOnClickListener { supportFinishAfterTransition() }
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

            Toast.makeText(this, getString(R.string.label_copy), Toast.LENGTH_SHORT).show()
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

        btnShowDetails?.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_error_details)
                .setMessage(networkError)
                .setPositiveButton(R.string.btn_close) { _, _ -> }
                .show()
        }

        loadData()
    }

    override fun onRefresh() {
        loadData()
    }

    private fun loadData() {
        noInternetLayout?.visibility = View.GONE

        requestNetwork?.startRequestNetwork("GET", "${Config.API_ENDPOINT}/prompt.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", dataListener)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adjustPaddings()
    }

    private fun adjustPaddings() {
        if (Build.VERSION.SDK_INT < 30) return
        try {
            val actionBar = findViewById<TextView>(R.id.activity_view_title)
            actionBar?.setPadding(
                actionBar.paddingLeft,
                window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top + actionBar.paddingTop,
                actionBar.paddingRight,
                actionBar.paddingBottom
            )
        } catch (_: Exception) { /* unused */ }
    }
}
