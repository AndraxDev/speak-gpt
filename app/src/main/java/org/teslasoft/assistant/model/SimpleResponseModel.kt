package org.teslasoft.assistant.model

class SimpleResponseModel(private var code: Int? = null, private var message: String? = null) {
    fun getCode(): Int? {
        return code
    }

    fun setCode(code: Int?) {
        this.code = code
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }
}