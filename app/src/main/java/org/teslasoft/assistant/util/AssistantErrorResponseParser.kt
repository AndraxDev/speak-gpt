package org.teslasoft.assistant.util

import android.content.Context
import org.teslasoft.assistant.R

class AssistantErrorResponseParser {
    companion object {
        fun parseFromException(context: Context, e: Exception, model: String) : String {
            return when {
                e.stackTraceToString().contains("invalid model") -> {
                    context.getString(R.string.prompt_no_model_provided)
                }
                e.stackTraceToString().contains("does not exist") -> {
                    String.format(context.getString(R.string.prompt_model_not_available), model)
                }
                e.stackTraceToString().contains("Connect timeout has expired") || e.stackTraceToString().contains("SocketTimeoutException") -> {
                    context.getString(R.string.prompt_timed_out)
                }
                e.stackTraceToString().contains("This model's maximum") -> {
                    context.getString(R.string.prompt_max_tokens_error)
                }
                e.stackTraceToString().contains("No address associated with hostname") -> {
                    context.getString(R.string.prompt_offline)
                }
                e.stackTraceToString().contains("Incorrect API key") -> {
                    context.getString(R.string.prompt_key_invalid)
                }
                e.stackTraceToString().contains("you must provide a model") -> {
                    context.getString(R.string.prompt_no_model)
                }
                e.stackTraceToString().contains("Software caused connection abort") -> {
                    context.getString(R.string.prompt_error_unknown)
                }
                e.stackTraceToString().contains("You exceeded your current quota") -> {
                    context.getString(R.string.prompt_quota_reached)
                }
                else -> {
                    e.stackTraceToString()
                }
            }
        }
    }
}