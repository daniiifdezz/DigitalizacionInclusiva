package org.dferna14.project.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.startup.Initializer

/**
 * Implementación Android con SharedPreferences. El contexto de aplicación se
 * captura mediante androidx.startup (ContextInitializer), de modo que la factory
 * `crearSessionStorage()` (sin parámetros) pueda construirlo sin acoplarse a Koin.
 */
class SessionStorageAndroid(context: Context) : SessionStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "session_prefs", Context.MODE_PRIVATE
    )

    override fun guardarSesion(token: String, userId: Int, email: String, rol: String) {
        prefs.edit().apply {
            putString("token", token)
            putInt("userId", userId)
            putString("email", email)
            putString("rol", rol)
            apply()
        }
    }

    override fun obtenerToken(): String? = prefs.getString("token", null)
    override fun obtenerEmail(): String? = prefs.getString("email", null)
    override fun obtenerRol(): String? = prefs.getString("rol", null)
    override fun obtenerUserId(): Int? =
        if (prefs.contains("userId")) prefs.getInt("userId", -1) else null



    override fun guardarUrlBackend(url: String) {
        prefs.edit().putString("url_backend", url).apply()
    }

    override fun obtenerUrlBackend(): String? {
        return prefs.getString("url_backend", null)
    }

    override fun limpiarSesion() {
        prefs.edit().apply {
            remove("token")
            remove("userId")
            remove("email")
            remove("rol")
            apply()
        }
    }
    override fun haySesion(): Boolean = obtenerToken() != null


}

private lateinit var contextoApp: Context

/**
 * Inicializador de androidx.startup que captura el Context de aplicación antes de
 * que cualquier Activity arranque. Declarado como provider en el AndroidManifest.
 */
class ContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context {
        contextoApp = context.applicationContext
        return contextoApp
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

actual fun crearSessionStorage(): SessionStorage {
    return SessionStorageAndroid(contextoApp)
}
