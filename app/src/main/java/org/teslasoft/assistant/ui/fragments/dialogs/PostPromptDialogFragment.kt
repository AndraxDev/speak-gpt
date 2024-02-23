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

package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.teslasoft.assistant.R

class PostPromptDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(name: String, title: String, desc: String, prompt: String, type: String, category: String) : PostPromptDialogFragment {
            val postPromptDialogFragment = PostPromptDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putString("title", title)
            args.putString("desc", desc)
            args.putString("prompt", prompt)
            args.putString("type", type)
            args.putString("category", category)

            postPromptDialogFragment.arguments = args

            return postPromptDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var listener: StateChangesListener? = null

    private var fieldName: EditText? = null
    private var fieldTitle: EditText? = null
    private var fieldDesc: EditText? = null
    private var fieldPrompt: EditText? = null

    private var gptButton: MaterialButton? = null
    private var dalleButton: MaterialButton? = null

    private var catDevelopment: RadioButton? = null
    private var catMusic: RadioButton? = null
    private var catArt: RadioButton? = null
    private var catCulture: RadioButton? = null
    private var catBusiness: RadioButton? = null
    private var catGaming: RadioButton? = null
    private var catEducation: RadioButton? = null
    private var catHistory: RadioButton? = null
    private var catHealth: RadioButton? = null
    private var catFood: RadioButton? = null
    private var catTourism: RadioButton? = null
    private var catProductivity: RadioButton? = null
    private var catTools: RadioButton? = null
    private var catEntertainment: RadioButton? = null
    private var catSport: RadioButton? = null

    private var category: String = ""
    private var type: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post_prompt, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_post_prompt, null)

        fieldName = view.findViewById(R.id.field_author_name)
        fieldTitle = view.findViewById(R.id.field_prompt_title)
        fieldDesc = view.findViewById(R.id.field_prompt_desc)
        fieldPrompt = view.findViewById(R.id.field_prompt)
        gptButton = view.findViewById(R.id.btn_gpt)
        dalleButton = view.findViewById(R.id.btn_dalle)
        catDevelopment = view.findViewById(R.id.cat_development)
        catMusic = view.findViewById(R.id.cat_music)
        catArt = view.findViewById(R.id.cat_art)
        catCulture = view.findViewById(R.id.cat_culture)
        catBusiness = view.findViewById(R.id.cat_business)
        catGaming = view.findViewById(R.id.cat_gaming)
        catEducation = view.findViewById(R.id.cat_education)
        catHistory = view.findViewById(R.id.cat_history)
        catHealth = view.findViewById(R.id.cat_health)
        catFood = view.findViewById(R.id.cat_food)
        catTourism = view.findViewById(R.id.cat_tourism)
        catProductivity = view.findViewById(R.id.cat_productivity)
        catTools = view.findViewById(R.id.cat_tools)
        catEntertainment = view.findViewById(R.id.cat_entertainment)
        catSport = view.findViewById(R.id.cat_sport)

        gptButton?.setOnClickListener { type = "GPT" }
        dalleButton?.setOnClickListener { type = "DALL-e" }

        catDevelopment?.setOnClickListener { category = "development" }
        catMusic?.setOnClickListener { category = "music" }
        catArt?.setOnClickListener { category = "art" }
        catCulture ?.setOnClickListener { category = "culture" }
        catBusiness?.setOnClickListener { category = "business" }
        catGaming?.setOnClickListener { category = "gaming" }
        catEducation?.setOnClickListener { category = "education" }
        catHistory?.setOnClickListener { category = "history" }
        catHealth ?.setOnClickListener { category = "health" }
        catFood?.setOnClickListener { category = "food" }
        catTourism?.setOnClickListener { category = "tourism" }
        catProductivity?.setOnClickListener { category = "productivity" }
        catTools?.setOnClickListener { category = "tools" }
        catEntertainment?.setOnClickListener { category = "entertainment" }
        catSport?.setOnClickListener { category = "sport" }
        catHealth?.setOnClickListener { category = "health" }

        when (type) {
            "GPT" -> gptButton?.isChecked = true
            "DALL-e" -> dalleButton?.isChecked = true
        }

        when (category) {
            "development" -> catDevelopment?.isChecked = true
            "music" -> catMusic?.isChecked = true
            "art" -> catArt?.isChecked = true
            "culture" -> catCulture?.isChecked = true
            "business" -> catBusiness?.isChecked = true
            "gaming" -> catGaming?.isChecked = true
            "education" -> catEducation?.isChecked = true
            "history" -> catHistory?.isChecked = true
            "health" -> catHealth ?.isChecked = true
            "food" -> catFood?.isChecked = true
            "tourism" -> catTourism?.isChecked = true
            "productivity" -> catProductivity?.isChecked = true
            "tools" -> catTools?.isChecked = true
            "entertainment" -> catEntertainment?.isChecked = true
            "sport" -> catSport?.isChecked = true
        }

        fieldName?.setText(requireArguments().getString("name"))
        fieldTitle?.setText(requireArguments().getString("title"))
        fieldDesc?.setText(requireArguments().getString("desc"))
        fieldPrompt?.setText(requireArguments().getString("prompt"))

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("Post") { _, _ -> validateForm() }
            .setNegativeButton("Cancel") { _, _ -> listener!!.onCanceled() }

        return builder!!.create()
    }

    private fun validateForm() {
        if (fieldName?.text.toString().trim() == "" || fieldTitle?.text.toString().trim() == "" || fieldDesc?.text.toString().trim() == "" || fieldPrompt?.text.toString().trim() == "" || type == "" || category == "") {
            listener!!.onFormError(fieldName?.text.toString(), fieldTitle?.text.toString(), fieldDesc?.text.toString(), fieldPrompt?.text.toString(), type, category)
        } else {
            listener!!.onFormFilled(fieldName?.text.toString(), fieldTitle?.text.toString(), fieldDesc?.text.toString(), fieldPrompt?.text.toString(), type, category)
        }
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onFormFilled(name: String, title: String, desc: String, prompt: String, type: String, category: String)

        fun onFormError(name: String, title: String, desc: String, prompt: String, type: String, category: String)
        fun onCanceled()
    }
}
