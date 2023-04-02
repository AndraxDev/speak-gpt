package org.teslasoft.assistant.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
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

    private val postPromptListener: PostPromptDialog.StateChangesListener = object : PostPromptDialog.StateChangesListener {
        override fun onFormFilled(name: String, title: String, desc: String, prompt: String, type: String, category: String) {
            val mName: String = URLEncoder.encode(name, Charsets.UTF_8.name())
            val mTitle: String = URLEncoder.encode(title, Charsets.UTF_8.name())
            val mDesc: String = URLEncoder.encode(desc, Charsets.UTF_8.name())
            val mPrompt: String = URLEncoder.encode(prompt, Charsets.UTF_8.name())

            requestNetwork?.startRequestNetwork("GET", "https://gpt.teslasoft.org/api/v1/post.php?api_key=${Api.API_KEY}&name=$mName&title=$mTitle&desc=$mDesc&prompt=$mPrompt&type=$type&category=$category", "A", promptPostListener)
        }

        override fun onFormError(name: String, title: String, desc: String, prompt: String, type: String, category: String) {
            Toast.makeText(requireActivity(), "Please fill all blanks", Toast.LENGTH_SHORT).show()

            val promptDialog: PostPromptDialog = PostPromptDialog.newInstance(name, title, desc, prompt, type, category)
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

                promptsAdapter = PromptAdapter(prompts, this@PromptsFragment)

                promptsList?.adapter = promptsAdapter

                promptsAdapter?.notifyDataSetChanged()

                refreshLayout?.visibility = View.VISIBLE
                noInternetLayout?.visibility = View.GONE
                progressbar?.visibility = View.GONE

            } catch (e: Exception) {
                MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
                    .setTitle("Error")
                    .setMessage(e.stackTraceToString())
                    .setPositiveButton("Close") { _, _ -> }
                    .show()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            refreshLayout?.visibility = View.GONE
            noInternetLayout?.visibility = View.VISIBLE
            progressbar?.visibility = View.GONE
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

        refreshLayout?.visibility = View.GONE
        noInternetLayout?.visibility = View.GONE
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
            val promptDialog: PostPromptDialog = PostPromptDialog.newInstance("", "", "", "", "", "")
            promptDialog.setStateChangedListener(postPromptListener)
            promptDialog.show(parentFragmentManager.beginTransaction(), "PromptDialog")
        }

        loadData()
    }

    private fun loadData() {
        requestNetwork?.startRequestNetwork("GET", "https://gpt.teslasoft.org/api/v1/search.php?api_key=${Api.API_KEY}&query=$query", "A", searchDataListener)

        refreshLayout?.visibility = View.GONE
        noInternetLayout?.visibility = View.GONE
        progressbar?.visibility = View.VISIBLE
    }

    override fun onRefresh() {
        refreshLayout?.isRefreshing = false
        loadData()
    }
}