package org.teslasoft.core.api.network

import android.app.Activity

@Suppress("unused")
class RequestNetwork(private val activity: Activity) {
    private var params: HashMap<String, Any> = HashMap()
    private var headers: HashMap<String, Any> = HashMap()

    private var requestType: Int = 0

    fun setHeaders(headers: HashMap<String, Any>) {
        this.headers = headers
    }

    fun setParams(params: HashMap<String, Any>, requestType: Int) {
        this.params = params
        this.requestType = requestType
    }

    fun getParams(): HashMap<String, Any> {
        return params
    }

    fun getHeaders(): HashMap<String, Any> {
        return headers
    }

    fun getActivity(): Activity {
        return activity
    }

    fun getRequestType(): Int {
        return requestType
    }

    fun startRequestNetwork(
        method: String, url: String, tag: String, requestListener: RequestListener
    ) {
        RequestNetworkController.getInstance()?.execute(this, method, url, tag, requestListener)
    }

    interface RequestListener {
        fun onResponse(tag: String, message: String)
        fun onErrorResponse(tag: String, message: String)
    }
}