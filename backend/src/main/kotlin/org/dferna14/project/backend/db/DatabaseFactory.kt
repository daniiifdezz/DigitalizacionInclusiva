package org.dferna14.project.backend.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

/**
 * Gestiona la conexión a PostgreSQL mediante HikariCP (pool de conexiones)
 * y la inicializa con Exposed ORM.
 *
 * Las credenciales se leen de variables de entorno para no exponerlas
 * en el código fuente. Valores por defecto para desarrollo local.
 */
object DatabaseFactory {

    fun init() {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl         = buildJdbcUrl()
            username        = System.getenv("DB_USER")     ?: "postgres"
            password        = System.getenv("DB_PASSWORD") ?: ""
            maximumPoolSize = 10
            isAutoCommit    = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        println("Conectado a PostgreSQL: ${config.jdbcUrl}")
    }

    private fun buildJdbcUrl(): String {
        val host = System.getenv("DB_HOST") ?: "localhost"
        val port = System.getenv("DB_PORT") ?: "5435"
        val name = System.getenv("DB_NAME") ?: "DigitalizacionInclusiva"
        return "jdbc:postgresql://$host:$port/$name"
    }
}