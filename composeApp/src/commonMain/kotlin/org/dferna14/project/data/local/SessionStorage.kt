package org.dferna14.project.data.local

/**
 * Almacenamiento de sesión multiplataforma. Persiste el JWT y los datos básicos
 * del usuario para no tener que volver a hacer login al reabrir la app.
 *
 * Implementaciones: SharedPreferences en Android, Properties (fichero en
 * user.home) en Desktop.
 */
interface SessionStorage {
    fun guardarSesion(token: String, userId: Int, email: String, rol: String)
    fun obtenerToken(): String?
    fun obtenerEmail(): String?
    fun obtenerRol(): String?
    fun obtenerUserId(): Int?
    fun limpiarSesion()
    fun haySesion(): Boolean

    //URL del back
    fun guardarUrlBackend(url: String)
    fun obtenerUrlBackend(): String?
}

expect fun crearSessionStorage(): SessionStorage
