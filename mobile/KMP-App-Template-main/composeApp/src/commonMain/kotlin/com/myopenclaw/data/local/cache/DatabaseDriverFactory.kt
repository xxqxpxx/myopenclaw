package com.myopenclaw.data.local.cache

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQLDelight database drivers.
 *
 * This uses the expect/actual pattern for KMP:
 * - Android: AndroidSqliteDriver with Context
 * - iOS: NativeSqliteDriver
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
