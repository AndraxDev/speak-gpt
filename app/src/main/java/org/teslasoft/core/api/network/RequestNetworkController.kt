/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.core.api.network

import com.google.gson.Gson

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.lang.String.valueOf
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

open class RequestNetworkController {
    companion object {
        const val GET = "GET"

        const val REQUEST_PARAM = 0

        private const val SOCKET_TIMEOUT: Long = 15000
        private const val READ_TIMEOUT: Long = 25000

        protected var client: OkHttpClient? = null
        private var mInstance: RequestNetworkController? = null

        @Synchronized
        fun getInstance(): RequestNetworkController? {
            if (mInstance == null) {
                mInstance = RequestNetworkController()
            }
            return mInstance
        }
    }

    private fun getClient(): OkHttpClient {
        if (client == null) {
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()

            try {
                val trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                    "Unexpected default trust managers:" + Arrays.toString(
                        trustManagers
                    )
                }
                val trustManager = trustManagers[0] as X509TrustManager

                val sslContext: SSLContext = SSLContext.getInstance("TLSv1.3")
                sslContext.init(null, null, SecureRandom())
                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

                builder.sslSocketFactory(sslSocketFactory, trustManager)
                builder.connectTimeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS)
                builder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                builder.writeTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                builder.hostnameVerifier { _, _ -> true }
            } catch (ignored: java.lang.Exception) { /* unused */ }

            client = builder.build()
        }

        return client as OkHttpClient
    }

    fun execute(
        requestNetwork: RequestNetwork,
        method: String,
        url: String,
        tag: String,
        requestListener: RequestNetwork.RequestListener
    ) {
        val reqBuilder: Request.Builder = Request.Builder()
        val headerBuilder: Headers.Builder = Headers.Builder()

        if (requestNetwork.getHeaders().size > 0) {
            val headers: HashMap<String, Any> = requestNetwork.getHeaders()

            for (header: MutableMap.MutableEntry<String, Any> in headers.entries) {
                headerBuilder.add(header.key, valueOf(header.value))
            }
        }

        try {
            if (requestNetwork.getRequestType() == REQUEST_PARAM) {
                if (method == GET) {
                    get(reqBuilder, url, requestNetwork, headerBuilder)
                } else {
                    nonGet(reqBuilder, url, requestNetwork, headerBuilder, method)
                }
            } else {
                val reqBody: RequestBody = Gson().toJson(requestNetwork.getParams())
                    .toRequestBody("application/json".toMediaTypeOrNull())

                if (method == GET) {
                    reqBuilder.url(url).headers(headerBuilder.build()).get()
                } else {
                    reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody)
                }
            }

            val req: Request = reqBuilder.build()

            clientCall(req, requestNetwork, tag, requestListener)
        } catch (e: Exception) {
            requestListener.onErrorResponse(tag, e.message!!)
        }
    }

    private fun clientCall(
        req: Request,
        requestNetwork: RequestNetwork,
        tag: String,
        requestListener: RequestNetwork.RequestListener
    ) {
        getClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requestNetwork.getActivity().runOnUiThread {
                    requestListener.onErrorResponse(
                        tag, e.message!!
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody: String = response.body.string().trim()

                requestNetwork.getActivity().runOnUiThread {
                    requestListener.onResponse(
                        tag, responseBody
                    )
                }
            }
        })
    }

    private fun get(
        reqBuilder: Request.Builder,
        url: String,
        requestNetwork: RequestNetwork,
        headerBuilder: Headers.Builder
    ) {
        val httpBuilder: HttpUrl.Builder =
            url.toHttpUrlOrNull()?.newBuilder() ?: throw NullPointerException(
                "unexpected url: $url"
            )

        if (requestNetwork.getParams().size > 0) {
            val params: HashMap<String, Any> = requestNetwork.getParams()

            for (param: MutableMap.MutableEntry<String, Any> in params.entries) {
                httpBuilder.addQueryParameter(param.key, valueOf(param.value))
            }
        }

        reqBuilder.url(httpBuilder.build()).headers(headerBuilder.build()).get()
    }

    private fun nonGet(
        reqBuilder: Request.Builder,
        url: String,
        requestNetwork: RequestNetwork,
        headerBuilder: Headers.Builder,
        method: String
    ) {
        val formBuilder: FormBody.Builder = FormBody.Builder()

        if (requestNetwork.getParams().size > 0) {
            val params: HashMap<String, Any> = requestNetwork.getParams()

            for (param: MutableMap.MutableEntry<String, Any> in params.entries) {
                formBuilder.add(param.key, valueOf(param.value))
            }
        }

        val reqBody: RequestBody = formBuilder.build()
        reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody)
    }
}
