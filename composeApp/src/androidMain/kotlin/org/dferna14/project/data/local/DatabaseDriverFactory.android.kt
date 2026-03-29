package org.dferna14.project.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.dferna14.project.db.CuadernoCampoDatabase

/**
 * Implementación Android del driver SQLDelight.
 * Usa SQLite nativo de Android.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema  = CuadernoCampoDatabase.Schema,
            context = context,
            name    = "cuaderno_campo.db"
        )
    }
}