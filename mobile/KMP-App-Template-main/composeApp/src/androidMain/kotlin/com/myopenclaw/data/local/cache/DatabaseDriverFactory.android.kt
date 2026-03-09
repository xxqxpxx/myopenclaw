package com.myopenclaw.data.local.cache

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory.
 * Uses AndroidSqliteDriver which wraps Android's SQLite implementation.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MyOpenClawDatabase.Schema,
            context = context,
            name = "myopenclaw_cache.db"
        )
    }
}
