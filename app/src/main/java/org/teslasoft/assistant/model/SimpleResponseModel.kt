package org.teslasoft.assistant.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SimpleResponseModel(
    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null
)
//class SimpleResponseModel(private var code: Int? = null, private var message: String? = null) {
//    fun getCode(): Int? {
//        return code
//    }
//
//    fun setCode(code: Int?) {
//        this.code = code
//    }
//
//    fun getMessage(): String? {
//        return message
//    }
//
//    fun setMessage(message: String?) {
//        this.message = message
//    }
//}