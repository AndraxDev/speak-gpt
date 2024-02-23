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
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import org.teslasoft.assistant.Api
import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.assistant.AssistantActivity
import org.teslasoft.core.api.network.RequestNetwork

class PromptViewActivity : FragmentActivity(), SwipeRefreshLayout.OnRefreshListener {

    private var activityTitle: TextView? = null

    private var content: ConstraintLayout? = null

    private var progressBar: ProgressBar? = null

    private var noInternetLayout: ConstraintLayout? = null

    private var btnReconnect: MaterialButton? = null

    private var promptBy: TextView? = null

    private var promptText: EditText? = null

    private var textCat: TextView? = null

    private var refreshPage: SwipeRefreshLayout? = null

    private var requestNetwork: RequestNetwork? = null

    private var btnCopy: MaterialButton? = null

    private var btnLike: MaterialButton? = null

    private var btnTry: MaterialButton? = null

    private var btnFlag: ImageButton? = null

    private var id = ""

    private var title = ""

    private var likeState = false

    private var settings: SharedPreferences? = null

    private var promptFor: String? = null

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
            } catch (e: Exception) {
                MaterialAlertDialogBuilder(this@PromptViewActivity, R.style.App_MaterialAlertDialog)
                    .setTitle("Error")
                    .setMessage(e.stackTraceToString())
                    .setPositiveButton("Close") { _, _ -> }
                    .show()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.accent_100)

        val extras: Bundle? = intent.extras

        if (extras == null) {
            finish()
        } else {
            id = extras.getString("id", "")
            title = extras.getString("title", "")

            if (id == "" || title == "") {
                finish()
            } else {
                setContentView(R.layout.activity_view_prompt)
                activityTitle = findViewById(R.id.activity_view_title)

                content = findViewById(R.id.view_content)
                progressBar = findViewById(R.id.progress_bar_view)
                noInternetLayout = findViewById(R.id.no_internet)
                btnReconnect = findViewById(R.id.btn_reconnect)
                promptBy = findViewById(R.id.prompt_by)
                promptText = findViewById(R.id.prompt_text)
                refreshPage = findViewById(R.id.refresh_page)
                btnFlag = findViewById(R.id.btn_flag)
                btnCopy = findViewById(R.id.btn_copy)
                btnLike = findViewById(R.id.btn_like)
                btnTry = findViewById(R.id.btn_try)
                textCat = findViewById(R.id.text_cat)

                btnFlag?.setImageResource(R.drawable.ic_flag)

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
                        requestNetwork?.startRequestNetwork("GET", "https://gpt.teslasoft.org/api/v1/dislike.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", dislikeListener)
                    } else {
                        requestNetwork?.startRequestNetwork("GET", "https://gpt.teslasoft.org/api/v1/like.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", likeListener)
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

                loadData()
            }
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

        requestNetwork?.startRequestNetwork("GET", "https://gpt.teslasoft.org/api/v1/prompt.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", dataListener)
    }
}
