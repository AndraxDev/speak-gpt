package org.teslasoft.assistant.ui.debug.aicore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.teslasoft.assistant.aicore.NanoManager

class NanoViewModel : ViewModel() {

    private val nanoManager = NanoManager()

    private var generationJob: Job? = null

    private val _response = MutableLiveData("")
    val response: LiveData<String> = _response

    private val _generating = MutableLiveData(false)
    val generating: LiveData<Boolean> = _generating

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun initialize(
        releaseStage: Int = ModelReleaseStage.PREVIEW,
        preference: Int = ModelPreference.FAST
    ) {
        viewModelScope.launch {
            try {
                nanoManager.initialize(releaseStage, preference)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun generate(prompt: String) {

        // Kill a previous inference if one exists.
        generationJob?.cancel()

        _response.value = ""
        _generating.value = true
        _error.value = null

        generationJob = viewModelScope.launch {

            val model: GenerativeModel = nanoManager.getModel()

            val output = StringBuilder()

            try {
                model.generateContentStream(prompt)
                    .collect { chunk ->

                        val text =
                            chunk.candidates
                                .firstOrNull()
                                ?.text
                                .orEmpty()

                        output.append(text)

                        _response.value = output.toString()
                    }

            } catch (e: CancellationException) {
                // User pressed Stop.
                throw e

            } catch (e: Exception) {
                _error.value = e.message

            } finally {
                _generating.value = false
                generationJob = null
            }
        }
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        generationJob = null
    }

    override fun onCleared() {
        generationJob?.cancel()
        nanoManager.close()
    }
}