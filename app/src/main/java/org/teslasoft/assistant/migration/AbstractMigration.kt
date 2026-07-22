package org.teslasoft.assistant.migration

import android.content.Context

abstract class AbstractMigration {
    abstract fun migrate(context: Context, chatId: String)
}