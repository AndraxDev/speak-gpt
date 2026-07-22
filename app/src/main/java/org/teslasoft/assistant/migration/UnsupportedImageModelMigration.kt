package org.teslasoft.assistant.migration

import android.content.Context
import org.teslasoft.assistant.preferences.Preferences

class UnsupportedImageModelMigration : AbstractMigration() {
    override fun migrate(context: Context, chatId: String) {
        val preferences = Preferences.getPreferences(context, chatId)

        if (preferences.getImageModel() == "dall-e-2" || preferences.getImageModel() == "dall-e-3") {
            preferences.setImageModel("gpt-image-1")
        }
    }
}
