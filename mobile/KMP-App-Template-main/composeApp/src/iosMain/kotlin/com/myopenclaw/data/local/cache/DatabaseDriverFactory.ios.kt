package com.myopenclaw.data.local.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseDriverFactory.
 * Uses NativeSqliteDriver which wraps iOS's native SQLite implementation.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = MyOpenClawDatabase.Schema,
            name = "myopenclaw_cache.db"
        )
    }
}
