package org.dferna14.project.data.local

import java.io.File
import java.util.Properties

/**
 * Implementación Desktop con un fichero .properties bajo el directorio del usuario.
 */
class SessionStorageJvm : SessionStorage {
    private val ficheroSesion = File(
        System.getProperty("user.home"),
        ".digitalizacion-inclusiva/session.properties"
    )

    private fun cargar(): Properties {
        val props = Properties()
        if (ficheroSesion.exists()) {
            ficheroSesion.inputStream().use { props.load(it) }
        }
        return props
    }

    private fun guardar(props: Properties) {
        ficheroSesion.parentFile.mkdirs()
        ficheroSesion.outputStream().use { props.store(it, "Sesion guardada") }
    }

    override fun guardarSesion(token: String, userId: Int, email: String, rol: String) {
        val props = cargar()
        props.setProperty("token", token)
        props.setProperty("userId", userId.toString())
        props.setProperty("email", email)
        props.setProperty("rol", rol)
        guardar(props)
    }

    override fun obtenerToken(): String? = cargar().getProperty("token")
    override fun obtenerEmail(): String? = cargar().getProperty("email")
    override fun obtenerRol(): String? = cargar().getProperty("rol")
    override fun obtenerUserId(): Int? = cargar().getProperty("userId")?.toIntOrNull()


    override fun guardarUrlBackend(url: String) {
        val props = cargar()
        props.setProperty("url_backend", url)
        guardar(props)
    }

    override fun obtenerUrlBackend(): String? {
        return cargar().getProperty("url_backend")
    }

    override fun limpiarSesion() {
        val props = cargar()
        props.remove("token")
        props.remove("userId")
        props.remove("email")
        props.remove("rol")
        if (props.isEmpty) {
            if (ficheroSesion.exists()) ficheroSesion.delete()
        } else {
            guardar(props)
        }
    }

    override fun haySesion(): Boolean = obtenerToken() != null
}

actual fun crearSessionStorage(): SessionStorage = SessionStorageJvm()
