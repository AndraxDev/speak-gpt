/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
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

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import org.teslasoft.assistant.Api
import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.adapters.PromptAdapter
import org.teslasoft.assistant.ui.fragments.dialogs.PostPromptDialogFragment
import org.teslasoft.core.api.network.RequestNetwork

import java.net.URLEncoder

class PromptsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_prompts, container, false)
    }

    private var fieldSearch: EditText? = null

    private var btnPost: ExtendedFloatingActionButton? = null

    private var promptsList: ListView? = null

    private var promptsAdapter: PromptAdapter? = null

    private var prompts: ArrayList<HashMap<String, String>> = arrayListOf()

    private var refreshLayout: SwipeRefreshLayout? = null

    private var requestNetwork: RequestNetwork? = null

    private var query: String = ""

    private var refreshButton: MaterialButton? = null

    private var noInternetLayout: LinearLayout? = null

    private var progressbar: ProgressBar? = null

    private var selectedCategory: String = "all"

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

    private val postPromptListener: PostPromptDialogFragment.StateChangesListener = object : PostPromptDialogFragment.StateChangesListener {
        override fun onFormFilled(
            name: String,
            title: String,
            desc: String,
            prompt: String,
            type: String,
            category: String
        ) {
            val mName: String = URLEncoder.encode(name, Charsets.UTF_8.name())
            val mTitle: String = URLEncoder.encode(title, Charsets.UTF_8.name())
            val mDesc: String = URLEncoder.encode(desc, Charsets.UTF_8.name())
            val mPrompt: String = URLEncoder.encode(prompt, Charsets.UTF_8.name())

            requestNetwork?.startRequestNetwork(
                "GET",
                "https://gpt.teslasoft.org/api/v1/post.php?api_key=${Api.API_KEY}&name=$mName&title=$mTitle&desc=$mDesc&prompt=$mPrompt&type=$type&category=$category",
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
            Toast.makeText(requireActivity(), "Please fill all blanks", Toast.LENGTH_SHORT).show()

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
            } catch (e: Exception) {
                MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
                    .setTitle("Error")
                    .setMessage(e.stackTraceToString())
                    .setPositiveButton("Close") { _, _ -> }
                    .show()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            noInternetLayout?.visibility = View.VISIBLE
            promptsList?.visibility = View.GONE
            progressbar?.visibility = View.GONE

//            MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
//                .setTitle("Error")
//                .setMessage(message)
//                .setPositiveButton("Close") { _, _ -> }
//                .show()
        }
    }

    private val promptPostListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            loadData()
        }

        override fun onErrorResponse(tag: String, message: String) {
            MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
                .setTitle("Error")
                .setMessage("Failed to post prompt. Please check your Internet connection.")
                .setPositiveButton("Close") { _, _ -> }
                .show()
        }
    }

    private fun filter(plist: ArrayList<HashMap<String, String>>) {

        if (selectedCategory == "all") {
            promptsAdapter = PromptAdapter(plist, this)

            promptsList?.adapter = promptsAdapter

            promptsAdapter?.notifyDataSetChanged()
        } else {
            val filtered = arrayListOf<HashMap<String, String>>()

            for (map: HashMap<String, String> in plist) {
                if (selectedCategory == map["category"]) {
                    filtered.add(map)
                }
            }

            promptsAdapter = PromptAdapter(filtered, this)
            promptsList?.adapter = promptsAdapter
            promptsAdapter?.notifyDataSetChanged()
        }

        noInternetLayout?.visibility = View.GONE
        promptsList?.visibility = View.VISIBLE
        progressbar?.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSearch: ImageButton = view.findViewById(R.id.btn_search)
        fieldSearch = view.findViewById(R.id.field_search)
        btnPost = view.findViewById(R.id.btn_add_prompt)
        promptsList = view.findViewById(R.id.prompts)
        refreshLayout = view.findViewById(R.id.refresh_search)
        refreshButton = view.findViewById(R.id.btn_reconnect)
        noInternetLayout = view.findViewById(R.id.no_internet)
        progressbar = view.findViewById(R.id.progress_bar)
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

        initializeCat()

        promptsList?.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) { /* unused */ }

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

                    val topRowVerticalPosition: Int = if (prompts.isEmpty() || promptsList == null || promptsList?.childCount == 0) 0 else promptsList?.getChildAt(0)!!.top

                    if (firstVisibleItem == 0 && topRowVerticalPosition >= 0) {
                        btnPost?.extend()
                    } else {
                        btnPost?.shrink()
                    }

                    refreshLayout?.isEnabled = firstVisibleItem == 0 && topRowVerticalPosition >= 0
                }
            })


        noInternetLayout?.visibility = View.GONE
        promptsList?.visibility = View.GONE
        progressbar?.visibility = View.VISIBLE

        refreshButton?.setOnClickListener {
            loadData()
        }

        refreshLayout?.setColorSchemeResources(R.color.accent_900)
        refreshLayout?.setProgressBackgroundColorSchemeColor(
            SurfaceColors.SURFACE_2.getColor(requireActivity())
        )

        refreshLayout?.setSize(SwipeRefreshLayout.LARGE)

        refreshLayout?.setOnRefreshListener(this)

        promptsAdapter = PromptAdapter(prompts, this)

        promptsList?.dividerHeight = 0

        promptsList?.adapter = promptsAdapter

        promptsAdapter?.notifyDataSetChanged()

        requestNetwork = RequestNetwork(requireActivity())

        btnSearch.setImageResource(R.drawable.ic_search)

        fieldSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* unused */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s.toString()

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

        loadData()
    }

    private fun initializeCat() {
        catAll?.setOnClickListener { selectedCategory = "all";filter(prompts) }
        catDevelopment?.setOnClickListener { selectedCategory = "development";filter(prompts) }
        catMusic?.setOnClickListener { selectedCategory = "music";filter(prompts) }
        catArt?.setOnClickListener { selectedCategory = "art";filter(prompts) }
        catCulture?.setOnClickListener { selectedCategory = "culture";filter(prompts) }
        catBusiness?.setOnClickListener { selectedCategory = "business";filter(prompts) }
        catGaming?.setOnClickListener { selectedCategory = "gaming";filter(prompts) }
        catEducation?.setOnClickListener { selectedCategory = "education";filter(prompts) }
        catHistory?.setOnClickListener { selectedCategory = "history";filter(prompts) }
        catFood?.setOnClickListener { selectedCategory = "food";filter(prompts) }
        catTourism?.setOnClickListener { selectedCategory = "tourism";filter(prompts) }
        catProductivity?.setOnClickListener { selectedCategory = "productivity";filter(prompts) }
        catTools?.setOnClickListener { selectedCategory = "tools";filter(prompts) }
        catEntertainment?.setOnClickListener { selectedCategory = "entertainment";filter(prompts) }
        catSport?.setOnClickListener { selectedCategory = "sport";filter(prompts) }
        catHealth?.setOnClickListener { selectedCategory = "health";filter(prompts) }
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return SurfaceColors.SURFACE_2.getColor(context)
    }

    private fun loadData() {
        requestNetwork?.startRequestNetwork("GET", "https://gpt.teslasoft.org/api/v1/search.php?api_key=${Api.API_KEY}&query=$query", "A", searchDataListener)

//        refreshLayout?.visibility = View.GONE
        noInternetLayout?.visibility = View.GONE
        promptsList?.visibility = View.GONE
        progressbar?.visibility = View.VISIBLE
    }

    override fun onRefresh() {
        refreshLayout?.isRefreshing = false
        loadData()
    }
}
