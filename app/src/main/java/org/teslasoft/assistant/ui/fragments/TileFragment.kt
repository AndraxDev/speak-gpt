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

package org.teslasoft.assistant.ui.fragments

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class TileFragment : Fragment() {
    companion object {
        fun newInstance(checked: Boolean, checkable: Boolean, enabledText: String, disabledText: String?, enabledDesc: String, disabledDesc: String?, icon: Int, disabled: Boolean, chatId: String, functionDesc: String, transitionName: String? = null): TileFragment {

            val tileFragment = TileFragment()

            val args = Bundle()
            args.putBoolean("checked", checked)
            args.putBoolean("checkable", checkable)
            args.putString("enabledText", enabledText)
            args.putString("disabledText", disabledText)
            args.putString("enabledDesc", enabledDesc)
            args.putString("disabledDesc", disabledDesc)
            args.putInt("icon", icon)
            args.putBoolean("disabled", disabled)
            args.putString("chatId", chatId)
            args.putString("functionDesc", functionDesc)
            args.putString("transitionName", transitionName)

            tileFragment.arguments = args

            return tileFragment
        }
    }

    private var isChecked: Boolean = false
    private var isEnabled: Boolean = true

    private var onTileClickListener: OnTileClickListener? = null
    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    private var tileBg: ConstraintLayout? = null
    private var tileTitle: TextView? = null
    private var tileSubtitle: TextView? = null
    private var tileIcon: ImageView? = null

    private var onAttachedToActivity: Boolean = false

    private var preferences: Preferences? = null

    private var desc: String = ""

    enum class TileVisibility {
        VISIBLE,
        GONE
    }

    fun setOnTileClickListener(onTileClickListener: OnTileClickListener) {
        this.onTileClickListener = onTileClickListener
    }

    fun setOnCheckedChangeListener(onCheckedChangeListener: OnCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener
    }

    fun interface OnTileClickListener {
        fun onTileClick(view: View)
    }

    fun interface OnCheckedChangeListener {
        fun onCheckedChange(isChecked: Boolean)
    }

    fun setVisibility(visibility: TileVisibility) {
        if (visibility == TileVisibility.GONE) {
            tileBg?.visibility = View.GONE
        } else {
            tileBg?.visibility = View.VISIBLE
        }
    }

    fun setEnabled(isEnabled: Boolean) {
        if (onAttachedToActivity) {
            this.isEnabled = isEnabled

            if (isEnabled) {
                tileBg?.isClickable = true
                tileBg?.isEnabled = true
                if (isChecked) {
                    tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.window_background)
                    tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title_inv))
                    tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle_inv))
                    tileBg?.background = getEnabledDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_active)!!)
                } else {
                    if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true){
                        tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.accent_600)
                    } else {
                        tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.accent_900)
                    }
                    tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title))
                    tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle))
                    tileBg?.background = getDisabledDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_inactive)!!)
                }
            } else {
                tileBg?.isClickable = false
                tileBg?.isEnabled = false
                tileBg?.background = getDisDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_disabled)!!)
                tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.disabled_icon)
            }
        }
    }

    fun updateSubtitle(subtitle: String) {
        if (onAttachedToActivity) tileSubtitle?.text = subtitle
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

    fun setChecked(isChecked: Boolean) {
        if (onAttachedToActivity) {
            this.isChecked = isChecked

            if (isChecked) {
                tileIcon?.imageTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.window_background)
                tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title_inv))
                tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle_inv))
                tileBg?.background = getEnabledDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.tile_active
                    )!!
                )
                tileTitle?.text = requireArguments().getString("enabledText").toString()
                tileSubtitle?.text = requireArguments().getString("enabledDesc").toString()
            } else {
                if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true) {
                    tileIcon?.imageTintList =
                        ContextCompat.getColorStateList(requireActivity(), R.color.accent_600)
                } else {
                    tileIcon?.imageTintList =
                        ContextCompat.getColorStateList(requireActivity(), R.color.accent_900)
                }
                tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title))
                tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle))
                tileBg?.background = getDisabledDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.tile_inactive
                    )!!
                )
                tileTitle?.text = requireArguments().getString("disabledText")
                    ?: requireArguments().getString("enabledText").toString()
                tileSubtitle?.text = requireArguments().getString("disabledDesc")
                    ?: requireArguments().getString("enabledDesc").toString()
            }
        }
    }

    override fun onAttach(context: Context) {
        onAttachedToActivity = true

        super.onAttach(context)
    }

    override fun onDetach() {
        onAttachedToActivity = false

        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()

        val checked = args.getBoolean("checked")
        val checkable = args.getBoolean("checkable")
        val enabledText = args.getString("enabledText")
        val disabledText = args.getString("disabledText")
        val enabledDesc = args.getString("enabledDesc")
        val disabledDesc = args.getString("disabledDesc")
        val icon = args.getInt("icon")
        val disabled = args.getBoolean("disabled")
        val chatId: String = args.getString("chatId").toString()
        val functionDesc: String = args.getString("functionDesc").toString()

        desc = functionDesc
        preferences = Preferences.getPreferences(requireActivity(), chatId)

        isChecked = checked
        isEnabled = !disabled

        tileBg = view.findViewById(R.id.tile_bg)
        tileTitle = view.findViewById(R.id.tile_title)
        tileSubtitle = view.findViewById(R.id.tile_subtitle)
        tileIcon = view.findViewById(R.id.tile_icon)

        tileIcon?.setImageResource(icon)

        val tileText: String = if (!checkable) {
            enabledText.toString()
        } else if (checked) {
            enabledText.toString()
        } else {
            disabledText ?: enabledText.toString()
        }

        val tileDesc: String = if (checked) {
            enabledDesc.toString()
        } else {
            disabledDesc ?: enabledDesc.toString()
        }

        tileTitle?.text = tileText
        tileSubtitle?.text = tileDesc

        tileIcon?.contentDescription = getString(R.string.icon) + "" + tileText
        tileBg?.contentDescription = getString(R.string.function) + "" + tileText
        tileBg?.tooltipText = tileText

        if (checked) {
            tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.window_background)
            tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title_inv))
            tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle_inv))
            tileBg?.background = getEnabledDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_active)!!)
        } else {
            if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true){
                tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.accent_600)
            } else {
                tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.accent_900)
            }
            tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title))
            tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle))
            tileBg?.background = getDisabledDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_inactive)!!)
        }

        if (!isEnabled) {
            tileBg?.isClickable = false
            tileBg?.isEnabled = false
            tileBg?.background = getDisDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_disabled)!!)
            tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.disabled_icon)
        }

        tileBg?.setOnLongClickListener {
            MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
                .setTitle(tileText)
                .setMessage(desc)
                .setPositiveButton("Close") { _, _ -> }
                .show()
            true
        }

        tileBg?.setOnClickListener {
            if (isEnabled) {
                if (checkable) {
                    if (isChecked) {
                        val fadeOut: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_out_btn)
                        val fadeIn: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_in_btn)
                        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true){
                            tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.accent_600)
                        } else {
                            tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.accent_900)
                        }
                        tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title))
                        tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle))
                        tileBg?.animation = fadeOut
                        tileTitle?.text = disabledText ?: enabledText
                        tileSubtitle?.text = disabledDesc ?: enabledDesc
                        fadeOut.start()
                        fadeOut.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) { /* UNUSED */ }
                            override fun onAnimationEnd(animation: Animation?) {
                                tileBg?.background = getDisabledDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_inactive)!!)
                                tileBg?.animation = fadeIn
                                fadeIn.start()

                                isChecked = false
                                onCheckedChangeListener?.onCheckedChange(false)
                            }
                            override fun onAnimationRepeat(animation: Animation?) { /* UNUSED */ }
                        })
                    } else {
                        val fadeOut: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_out_btn)
                        val fadeIn: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_in_btn)
                        tileIcon?.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.window_background)
                        tileTitle?.setTextColor(requireActivity().getColor(R.color.text_title_inv))
                        tileSubtitle?.setTextColor(requireActivity().getColor(R.color.text_subtitle_inv))
                        tileBg?.animation = fadeOut
                        tileTitle?.text = enabledText
                        tileSubtitle?.text = enabledDesc
                        fadeOut.start()
                        fadeOut.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                tileBg?.background = getEnabledDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.tile_active)!!)
                                tileBg?.animation = fadeIn
                                fadeIn.start()

                                isChecked = true
                                onCheckedChangeListener?.onCheckedChange(true)
                            }
                            override fun onAnimationRepeat(animation: Animation?) {}
                        })
                    }
                }

                onTileClickListener?.onTileClick(tileBg!!)
            }
        }

        tileTitle?.isSelected = true
        tileSubtitle?.isSelected = true

        Handler(Looper.getMainLooper()).postDelayed({
            tileTitle?.isSelected = true
            tileSubtitle?.isSelected = true
        }, 400)
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true){
            requireActivity().getColor(R.color.amoled_accent_50)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SurfaceColors.SURFACE_5.getColor(requireActivity())
            } else {
                requireActivity().getColor(R.color.accent_100)
            }
        }
    }

    private fun getEnabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getEnabledColor())
        return drawable
    }

    private fun getEnabledColor() : Int {
        return if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true){
            requireActivity().getColor(R.color.accent_600)
        } else {
            requireActivity().getColor(R.color.accent_900)
        }
    }

    private fun getDisDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisColor())
        return drawable
    }

    private fun getDisColor() : Int {
        return requireActivity().getColor(R.color.disabled_background)
    }
}
