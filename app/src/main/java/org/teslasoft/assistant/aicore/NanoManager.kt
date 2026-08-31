package org.teslasoft.assistant.aicore

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.generationConfig
import com.google.mlkit.genai.prompt.modelConfig

class NanoManager {
    private var model: GenerativeModel? = null

    suspend fun initialize(
        releaseStage: Int = ModelReleaseStage.PREVIEW,
        preference: Int = ModelPreference.FAST
    ): GenerativeModel {

        val config = generationConfig {
            modelConfig = modelConfig {
                this.releaseStage = releaseStage
                this.preference = preference
            }
        }

        val client = Generation.getClient(config)

        when (client.checkStatus()) {
            FeatureStatus.AVAILABLE -> {
                model = client
                client.warmup()
                return client
            }

            FeatureStatus.DOWNLOADABLE -> {
                // Model exists for this configuration,
                // but has not been downloaded yet.
                throw IllegalStateException("Gemini Nano model is downloadable but not installed")
            }

            FeatureStatus.DOWNLOADING -> {
                throw IllegalStateException("Gemini Nano is currently downloading")
            }

            FeatureStatus.UNAVAILABLE -> {
                throw IllegalStateException("Requested Gemini Nano configuration unavailable")
            }

            else -> {
                throw IllegalStateException("Unknown model state")
            }
        }
    }

    fun getModel(): GenerativeModel {
        return model ?: throw IllegalStateException("NanoManager has not been initialized")
    }

    fun close() {
        model?.close()
        model = null
    }
}