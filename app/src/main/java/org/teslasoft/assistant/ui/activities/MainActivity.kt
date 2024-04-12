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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import org.teslasoft.assistant.Config.Companion.API_ENDPOINT
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.DeviceInfoProvider
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.fragments.tabs.ChatsListFragment
import org.teslasoft.assistant.ui.fragments.tabs.PromptsFragment
import org.teslasoft.core.api.network.RequestNetwork
import java.io.IOException

class MainActivity : FragmentActivity(), Preferences.PreferencesChangedListener {
    private var navigationBar: BottomNavigationView? = null
    private var fragmentChats: ConstraintLayout? = null
    private var fragmentPrompts: ConstraintLayout? = null
    private var fragmentTips: ConstraintLayout? = null
    private var btnDebugger: ImageButton? = null
    private var debuggerWindow: ConstraintLayout? = null
    private var btnCloseDebugger: ImageButton? = null
    private var btnInitiateCrash: MaterialButton? = null
    private var btnSwitchAds: MaterialButton? = null
    private var threadLoader: LinearLayout? = null
    private var devIds: TextView? = null
    private var frameChats: Fragment? = null
    private var framePrompts: Fragment? = null
    private var frameTips: Fragment? = null
    private var root: ConstraintLayout? = null
    private var requestNetwork: RequestNetwork? = null
    private var preferences: Preferences? = null

    private var selectedTab: Int = 1
    private var isAnimating = false
    private var isInitialized: Boolean = false

    private val requestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            if (message == "131") {
                preferences!!.setAdsEnabled(false)
                startActivity(Intent(this@MainActivity, ThanksActivity::class.java))
                finish()
            } else {
                if (tag == "AID" && !preferences!!.getDebugMode()) {
                    preferences!!.setAdsEnabled(true)
                } else {
                    val androidId = DeviceInfoProvider.getAndroidId(this@MainActivity)

                    requestNetwork?.startRequestNetwork(
                        "GET",
                        "${API_ENDPOINT}/checkForDonation?did=${androidId}",
                        "AID",
                        this
                    )
                }
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            /* Failed to verify donation */
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val consent: SharedPreferences = getSharedPreferences("consent", MODE_PRIVATE)

        if (!consent.getBoolean("consent", false)) {
            startActivity(Intent(this, DataSafety::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        preferences = Preferences.getPreferences(this, "").addOnPreferencesChangedListener(this)

        navigationBar = findViewById(R.id.navigation_bar)

        fragmentChats = findViewById(R.id.fragment_chats)
        fragmentPrompts = findViewById(R.id.fragment_prompts)
        fragmentTips = findViewById(R.id.fragment_tips)
        root = findViewById(R.id.root)
        btnDebugger = findViewById(R.id.btn_open_debugger)
        debuggerWindow = findViewById(R.id.debugger_window)
        btnCloseDebugger = findViewById(R.id.btn_close_debugger)
        btnInitiateCrash = findViewById(R.id.btn_initiate_crash)
        btnSwitchAds = findViewById(R.id.btn_switch_ads)
        devIds = findViewById(R.id.dev_ids)
        threadLoader = findViewById(R.id.thread_loader)

        threadLoader?.visibility = View.VISIBLE

        btnDebugger?.visibility = View.GONE
        debuggerWindow?.visibility = View.GONE

        frameChats = supportFragmentManager.findFragmentById(R.id.fragment_chats_)
        framePrompts = supportFragmentManager.findFragmentById(R.id.fragment_prompts_)
        frameTips = supportFragmentManager.findFragmentById(R.id.fragment_tips_)

        preloadAmoled()

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Confirm exit")
                    .setMessage("Do you want to exit?")
                    .setPositiveButton("Yes") { _, _ ->
                        finish()
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .show()
            }
        } else {
            onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Confirm exit")
                        .setMessage("Do you want to exit?")
                        .setPositiveButton("Yes") { _, _ ->
                            finish()
                        }
                        .setNegativeButton("No") { _, _ -> }
                        .show()
                }
            })
        }

        Thread {
            DeviceInfoProvider.assignInstallationId(this)

            runOnUiThread {
                navigationBar!!.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item: MenuItem ->
                    if (!isAnimating) {
                        isAnimating = true
                        when (item.itemId) {
                            R.id.menu_chat -> {
                                menuChats()
                                return@OnItemSelectedListener true
                            }
                            R.id.menu_prompts -> {
                                menuPrompts()
                                return@OnItemSelectedListener true
                            }
                            R.id.menu_tips -> {
                                menuTips()
                                return@OnItemSelectedListener true
                            }
                        }
                    }

                    return@OnItemSelectedListener false
                })

                if (savedInstanceState != null) {
                    onRestoredState(savedInstanceState)
                }

                if (preferences!!.getDebugTestAds() && !preferences!!.getDebugMode()) {
                    preferences!!.setDebugMode(true)
                    restartActivity()
                }

                val installationId = DeviceInfoProvider.getInstallationId(this)
                val androidId = DeviceInfoProvider.getAndroidId(this)

                if (preferences!!.getAdsEnabled()) {
                    requestNetwork = RequestNetwork(this)
                    requestNetwork?.startRequestNetwork("GET", "${API_ENDPOINT}/checkForDonation?did=${installationId}", "IID", requestListener)
                }

                if (preferences!!.getDebugMode()) {
                    btnDebugger?.visibility = View.VISIBLE
                    btnDebugger?.setOnClickListener {
                        debuggerWindow?.visibility = View.VISIBLE
                    }

                    btnCloseDebugger?.setOnClickListener {
                        debuggerWindow?.visibility = View.GONE
                    }

                    btnInitiateCrash?.setOnClickListener {
                        throw RuntimeException("Test crash")
                    }

                    if (preferences!!.getAdsEnabled()) {
                        btnSwitchAds?.text = "Disable ads"
                    } else {
                        btnSwitchAds?.text = "Enable ads"
                    }

                    btnSwitchAds?.setOnClickListener {
                        if (preferences!!.getAdsEnabled()) {
                            preferences!!.setAdsEnabled(false)
                        } else {
                            preferences!!.setAdsEnabled(true)
                        }
                        restartActivity()
                    }

                    devIds?.text = "${devIds?.text}\n\nInstallation ID: $installationId\nAndroid ID: $androidId"

                    val crearEventoHilo: Thread = object : Thread() {
                        @SuppressLint("HardwareIds")
                        override fun run() {
                            val info: AdvertisingIdClient.Info?

                            val adId = try {
                                info = AdvertisingIdClient.getAdvertisingIdInfo(this@MainActivity)
                                info.id.toString()
                            } catch (e: IOException) {
                                e.printStackTrace()
                                "<Google Play Services error>"
                            } catch (e : GooglePlayServicesNotAvailableException) {
                                e.printStackTrace()
                                "<Google Play Services not found>"
                            } catch (e : IllegalStateException) {
                                e.printStackTrace()
                                "<IllegalStateException: ${e.message}>"
                            } catch (e : GooglePlayServicesRepairableException) {
                                e.printStackTrace()
                                "<Google Play Services error>"
                            }

                            devIds?.text = "${devIds?.text}\nAds ID: $adId"
                        }
                    }
                    crearEventoHilo.start()
                }

                reloadAmoled()

                Handler(Looper.getMainLooper()).postDelayed({
                    val fadeOut: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    threadLoader?.startAnimation(fadeOut)

                    fadeOut.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) { /* UNUSED */ }
                        override fun onAnimationEnd(animation: Animation) {
                            runOnUiThread {
                                threadLoader?.visibility = View.GONE
                                threadLoader?.elevation = 0.0f

                                isInitialized = true
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
                    })
                }, 50)
            }
        }.start()
    }

    private fun restartActivity() {
        runOnUiThread {
            threadLoader?.visibility = View.VISIBLE
            threadLoader?.elevation = 100.0f
            val fadeIn: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            threadLoader?.startAnimation(fadeIn)

            fadeIn.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) { /* UNUSED */ }
                override fun onAnimationEnd(animation: Animation) {
                    runOnUiThread {
                        recreate()
                    }
                }

                override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        if (isInitialized) {
            // Reset preferences singleton to global settings
            preferences = Preferences.getPreferences(this, "")

            reloadAmoled()
        }
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack()!!) {
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
            root?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            navigationBar!!.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.setColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            drawable.alpha = 235

            debuggerWindow?.background = drawable

            btnDebugger?.background = ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal_amoled, theme)
            btnCloseDebugger?.background = ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal_amoled, theme)
            btnInitiateCrash?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.amoled_accent_100, theme)
            btnInitiateCrash?.setTextColor(ResourcesCompat.getColor(resources, R.color.accent_600, theme))
            devIds?.background = ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_16_amoled, theme)
            devIds?.setTextColor(ResourcesCompat.getColor(resources, R.color.accent_600, theme))

            if (preferences?.getAdsEnabled()!!) {
                btnSwitchAds?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.accent_600, theme)
                btnSwitchAds?.setTextColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            } else {
                btnSwitchAds?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.amoled_accent_100, theme)
                btnSwitchAds?.setTextColor(ResourcesCompat.getColor(resources, R.color.accent_600, theme))
            }
        } else {
            window.navigationBarColor = SurfaceColors.SURFACE_3.getColor(this)
            window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            window.setBackgroundDrawableResource(R.color.window_background)
            root?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
            navigationBar!!.setBackgroundColor(SurfaceColors.SURFACE_3.getColor(this))

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.setColor(SurfaceColors.SURFACE_0.getColor(this))
            drawable.alpha = 235

            debuggerWindow?.background = drawable

            btnDebugger?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal, theme)!!)
            btnCloseDebugger?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal, theme)!!)
            btnInitiateCrash?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.accent_250, theme)
            btnInitiateCrash?.setTextColor(ResourcesCompat.getColor(resources, R.color.accent_900, theme))
            devIds?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_tonal_16, theme)!!)
            devIds?.setTextColor(ResourcesCompat.getColor(resources, R.color.accent_900, theme))

            if (preferences?.getAdsEnabled()!!) {
                btnSwitchAds?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.accent_900, theme)
                btnSwitchAds?.setTextColor(ResourcesCompat.getColor(resources, R.color.window_background, theme))
            } else {
                btnSwitchAds?.backgroundTintList = ResourcesCompat.getColorStateList(resources, R.color.accent_250, theme)
                btnSwitchAds?.setTextColor(ResourcesCompat.getColor(resources, R.color.accent_900, theme))
            }
        }

        (frameChats as ChatsListFragment).reloadAmoled(this)
        (framePrompts as PromptsFragment).reloadAmoled(this)
    }

    private fun preloadAmoled() {
        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack()!!) {
            window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            threadLoader?.background = ResourcesCompat.getDrawable(resources, R.color.amoled_window_background, null)
        } else {
            window.navigationBarColor = SurfaceColors.SURFACE_3.getColor(this)
            window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            threadLoader?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
        }
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack()!!) {
            ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
        } else {
            SurfaceColors.SURFACE_5.getColor(this)
        }
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("tab", selectedTab)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selectedTab = savedInstanceState.getInt("tab")
    }

    private fun menuChats() {
        Thread {
            runOnUiThread {
                openChats()
            }
        }.start()
    }

    private fun menuPrompts() {
        Thread {
            runOnUiThread {
                openPrompts()
            }
        }.start()
    }

    private fun menuTips() {
        Thread {
            runOnUiThread {
                openTips()
            }
        }.start()
    }

    private fun openChats() {
        transition(
            fragmentPrompts as ConstraintLayout,
            fragmentTips as ConstraintLayout,
            switchChatsAnimation,
            2,
            3,
            1
        )
    }

    private fun openPrompts() {
        transition(
            fragmentChats as ConstraintLayout,
            fragmentTips as ConstraintLayout,
            switchPromptsAnimation,
            1,
            3,
            2
        )
    }

    private fun openTips() {
        transition(
            fragmentChats as ConstraintLayout,
            fragmentPrompts as ConstraintLayout,
            switchTipsAnimation,
            1,
            2,
            3
        )
    }

    private fun onRestoredState(savedInstanceState: Bundle?) {
        selectedTab = savedInstanceState!!.getInt("tab")

        when (selectedTab) {
            1 -> {
                navigationBar?.selectedItemId = R.id.menu_chat
                switchLayout(fragmentPrompts!!, fragmentTips!!, fragmentChats!!)
            }
            2 -> {
                navigationBar?.selectedItemId = R.id.menu_prompts
                switchLayout(fragmentChats!!, fragmentTips!!, fragmentPrompts!!)
            }
            3 -> {
                navigationBar?.selectedItemId = R.id.menu_tips
                switchLayout(fragmentChats!!, fragmentPrompts!!, fragmentTips!!)
            }
        }
    }

    private fun animate(target: ConstraintLayout, listener: AnimatorListenerAdapter) {
        isAnimating = true
        target.visibility = View.VISIBLE
        target.alpha = 1f
        target.animate().setDuration(100).alpha(0f).setListener(listener).start()
    }

    private fun hideFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (!isFinishing && !fragmentManager.isDestroyed) {
            Thread {
                runOnUiThread {
                    fragmentManager.beginTransaction().hide(fragment).commit()
                }
            }.start()
        }
    }

    private fun showFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (!isFinishing && !fragmentManager.isDestroyed) {
            Thread {
                runOnUiThread {
                    fragmentManager.beginTransaction().show(fragment).commit()
                }
            }.start()
        }
    }

    private fun animationListenerCallback(
        fragmentManager: FragmentManager,
        layout1: ConstraintLayout,
        layout2: ConstraintLayout,
        layoutToShow: ConstraintLayout,
        fragment1: Fragment?,
        fragment2: Fragment?,
        fragmentToShow: Fragment?
    ) {
        layoutToShow.visibility = View.VISIBLE
        layoutToShow.alpha = 0f

        if (fragment1 != null) {
            hideFragment(fragmentManager, fragment1)
        }

        if (fragment2 != null) {
            hideFragment(fragmentManager, fragment2)
        }

        if (fragmentToShow != null) {
            showFragment(fragmentManager, fragmentToShow)
        }

        layoutToShow.animate()?.setDuration(150)?.alpha(1f)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    Thread {
                        this@MainActivity.runOnUiThread {
                            switchLayout(layout1, layout2, layoutToShow)
                            isAnimating = false
                        }
                    }.start()
                }
            })?.start()
    }

    private fun switchLayout(
        layout1: ConstraintLayout, layout2: ConstraintLayout, layoutToShow: ConstraintLayout
    ) {
        layout1.visibility = View.GONE
        layout2.visibility = View.GONE
        layoutToShow.visibility = View.VISIBLE
    }

    private fun transition(
        l1: ConstraintLayout,
        l2: ConstraintLayout,
        animation: AnimatorListenerAdapter,
        tab1: Int,
        tab2: Int,
        targetTab: Int
    ) {
        isAnimating = true
        when (selectedTab) {
            tab1 -> animate(l1, animation)
            tab2 -> animate(l2, animation)
            else -> {
                isAnimating = false
            }
        }

        selectedTab = targetTab
    }

    private val switchChatsAnimation: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Thread {
                    runOnUiThread {
                        animationListenerCallback(
                            supportFragmentManager,
                            fragmentPrompts!!,
                            fragmentTips!!,
                            fragmentChats!!,
                            frameTips,
                            framePrompts,
                            frameChats
                        )
                    }
                }.start()
            }
    }

    private val switchPromptsAnimation: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Thread {
                    runOnUiThread {
                        animationListenerCallback(
                            supportFragmentManager,
                            fragmentTips!!,
                            fragmentChats!!,
                            fragmentPrompts!!,
                            frameChats,
                            frameTips,
                            framePrompts
                        )
                    }
                }.start()
            }
        }

    private val switchTipsAnimation: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Thread {
                    runOnUiThread {
                        animationListenerCallback(
                            supportFragmentManager,
                            fragmentPrompts!!,
                            fragmentChats!!,
                            fragmentTips!!,
                            framePrompts,
                            frameChats,
                            frameTips
                        )
                    }
                }.start()
            }
        }

    override fun onPreferencesChanged(key: String, value: String) {
        if (key == "debug_mode" || key == "debug_test_ads") {
            restartActivity()
        }
    }
}
