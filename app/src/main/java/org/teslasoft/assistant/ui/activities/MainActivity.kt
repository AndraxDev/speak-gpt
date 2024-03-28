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
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView

import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.DeviceInfoProvider
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.fragments.tabs.ChatsListFragment
import org.teslasoft.assistant.ui.fragments.tabs.PromptsFragment

class MainActivity : FragmentActivity() {
    private var navigationBar: BottomNavigationView? = null
    private var fragmentChats: ConstraintLayout? = null
    private var fragmentPrompts: ConstraintLayout? = null
    private var fragmentTips: ConstraintLayout? = null

    private var selectedTab: Int = 1
    private var isAnimating = false

    private var frameChats: Fragment? = null
    private var framePrompts: Fragment? = null
    private var frameTips: Fragment? = null

    private var root: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val consent: SharedPreferences = getSharedPreferences("consent", MODE_PRIVATE)

        if (!consent.getBoolean("consent", false)) {
            startActivity(Intent(this, DataSafety::class.java))
            finish()
        }

        setContentView(R.layout.activity_main)

        navigationBar = findViewById(R.id.navigation_bar)

        fragmentChats = findViewById(R.id.fragment_chats)
        fragmentPrompts = findViewById(R.id.fragment_prompts)
        fragmentTips = findViewById(R.id.fragment_tips)
        root = findViewById(R.id.root)

        frameChats = supportFragmentManager.findFragmentById(R.id.fragment_chats_)
        framePrompts = supportFragmentManager.findFragmentById(R.id.fragment_prompts_)
        frameTips = supportFragmentManager.findFragmentById(R.id.fragment_tips_)



        Thread {
            DeviceInfoProvider.assignInstallationId(this)

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

            runOnUiThread {
                reloadAmoled()

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
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        reloadAmoled()
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() &&  Preferences.getPreferences(this, "").getAmoledPitchBlack()) {
            window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
            root?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            navigationBar!!.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
        } else {
            window.navigationBarColor = SurfaceColors.SURFACE_3.getColor(this)
            window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            window.setBackgroundDrawableResource(R.color.window_background)
            root?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
            navigationBar!!.setBackgroundColor(SurfaceColors.SURFACE_3.getColor(this))
        }

        (frameChats as ChatsListFragment).reloadAmoled()
        (framePrompts as PromptsFragment).reloadAmoled()
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
}
