package org.teslasoft.assistant

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        navigationBar = findViewById(R.id.navigation_bar)

        fragmentChats = findViewById(R.id.fragment_chats)
        fragmentPrompts = findViewById(R.id.fragment_prompts)
        fragmentTips = findViewById(R.id.fragment_tips)

        frameChats = supportFragmentManager.findFragmentById(R.id.fragment_chats_)
        framePrompts = supportFragmentManager.findFragmentById(R.id.fragment_prompts_)
        frameTips = supportFragmentManager.findFragmentById(R.id.fragment_tips_)

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
                    else -> {
                        return@OnItemSelectedListener false
                    }
                }
            } else return@OnItemSelectedListener false
        })

        if (savedInstanceState != null) {
            onRestoredState(savedInstanceState)
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
        Handler(Looper.getMainLooper()).postDelayed({
            openChats()
        }, 50)
    }

    private fun menuPrompts() {
        Handler(Looper.getMainLooper()).postDelayed({
            openPrompts()
        }, 50)
    }

    private fun menuTips() {
        Handler(Looper.getMainLooper()).postDelayed({
            openTips()
        }, 50)
    }

    private fun openChats() {
        transition(
            fragmentTips as ConstraintLayout,
            fragmentPrompts as ConstraintLayout,
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
        target.animate().setDuration(150).alpha(0f).setListener(listener).start()
    }

    private fun hideFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (!isFinishing && !fragmentManager.isDestroyed) {
            fragmentManager.beginTransaction().hide(fragment).commit()
        }
    }

    private fun showFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (!isFinishing && !fragmentManager.isDestroyed) {
            fragmentManager.beginTransaction().show(fragment).commit()
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
                    Handler(Looper.getMainLooper()).postDelayed({
                        switchLayout(layout1, layout2, layoutToShow)

                        isAnimating = false
                    }, 10)
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

    private val switchChatsAnimation: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            Handler(Looper.getMainLooper()).postDelayed({
                animationListenerCallback(
                    supportFragmentManager,
                    fragmentPrompts!!,
                    fragmentTips!!,
                    fragmentChats!!,
                    frameTips,
                    framePrompts,
                    frameChats
                )
            }, 10)
        }
    }

    private val switchPromptsAnimation: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Handler(Looper.getMainLooper()).postDelayed({
                    animationListenerCallback(
                        supportFragmentManager,
                        fragmentTips!!,
                        fragmentChats!!,
                        fragmentPrompts!!,
                        frameChats,
                        frameTips,
                        framePrompts
                    )
                }, 10)
            }
        }

    private val switchTipsAnimation: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Handler(Looper.getMainLooper()).postDelayed({
                    animationListenerCallback(
                        supportFragmentManager,
                        fragmentPrompts!!,
                        fragmentChats!!,
                        fragmentTips!!,
                        framePrompts,
                        frameChats,
                        frameTips
                    )
                }, 10)
            }
        }
}