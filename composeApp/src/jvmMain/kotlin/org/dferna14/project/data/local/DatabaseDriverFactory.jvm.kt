package org.dferna14.project.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.dferna14.project.db.CuadernoCampoDatabase

/**
 * Implementación Desktop del driver SQLDelight.
 * Usa SQLite via JDBC — el fichero se guarda en el directorio de trabajo.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:cuaderno_campo.db")
        CuadernoCampoDatabase.Schema.create(driver)
        return driver
    }
}