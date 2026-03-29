package org.dferna14.project.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * expect/actual para el driver de SQLDelight.
 * Cada plataforma proporciona su propia implementación:
 * - Android → AndroidSqliteDriver
 * - Desktop → JdbcSqliteDriver
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}