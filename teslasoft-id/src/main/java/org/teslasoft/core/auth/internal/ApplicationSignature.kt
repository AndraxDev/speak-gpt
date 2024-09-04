/*******************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.teslasoft.core.auth.internal

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

class ApplicationSignature(var context: Context) {
    fun getCertificateFingerprint(method: String): String {
        val pm = context.packageManager
        val packageName = context.packageName
        val signatures = pm.getPackageInfo(
            packageName, PackageManager.GET_SIGNING_CERTIFICATES
        ).signingInfo?.apkContentsSigners
        val cert = signatures!![0].toByteArray()
        val input: InputStream = ByteArrayInputStream(cert)
        var cf: CertificateFactory? = null
        var c: X509Certificate? = null
        var hexString: String? = null

        try {
            cf = CertificateFactory.getInstance("X509")
        } catch (e: CertificateException) {
            e.printStackTrace()
        }

        try {
            c = cf!!.generateCertificate(input) as X509Certificate
        } catch (e: CertificateException) {
            e.printStackTrace()
        }

        try {
            val md = MessageDigest.getInstance(method)
            val publicKey = md.digest(c!!.encoded)
            hexString = convert(publicKey)
        } catch (e1: NoSuchAlgorithmException) {
            e1.printStackTrace()
        } catch (e1: CertificateEncodingException) {
            e1.printStackTrace()
        }
        return hexString ?: ""
    }

    private fun convert(arr: ByteArray): String {
        val str = StringBuilder(arr.size * 2)
        for (i in arr.indices) {
            var h = Integer.toHexString(arr[i].toInt())
            val l = h.length
            if (l == 1) h = "0$h"
            if (l > 2) h = h.substring(l - 2, l)
            str.append(h.uppercase(Locale.getDefault()))
            if (i < arr.size - 1) str.append(':')
        }
        return str.toString()
    }
}
