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

    override fun guardarSesion(token: String, userId: Int, email: String, rol: String, explotacionId: Int?) {
        val props = cargar()
        props.setProperty("token", token)
        props.setProperty("userId", userId.toString())
        props.setProperty("email", email)
        props.setProperty("rol", rol)
        if (explotacionId != null) props.setProperty("explotacion_id", explotacionId.toString())
        else props.remove("explotacion_id")
        guardar(props)
    }

    override fun obtenerToken(): String? = cargar().getProperty("token")
    override fun obtenerEmail(): String? = cargar().getProperty("email")
    override fun obtenerRol(): String? = cargar().getProperty("rol")
    override fun obtenerUserId(): Int? = cargar().getProperty("userId")?.toIntOrNull()
    override fun obtenerExplotacionId(): Int? = cargar().getProperty("explotacion_id")?.toIntOrNull()

    override fun guardarNombre(nombre: String) {
        val props = cargar()
        props.setProperty("nombre", nombre)
        guardar(props)
    }
    override fun obtenerNombre(): String? = cargar().getProperty("nombre")

    override fun guardarNombreExplotacion(nombre: String) {
        val props = cargar()
        props.setProperty("explotacion_nombre", nombre)
        guardar(props)
    }
    override fun obtenerNombreExplotacion(): String? = cargar().getProperty("explotacion_nombre")

    override fun guardarUrlBackend(url: String) {
        val props = cargar()
        props.setProperty("url_backend", url)
        guardar(props)
    }
    override fun obtenerUrlBackend(): String? = cargar().getProperty("url_backend")

    override fun limpiarSesion() {
        val props = cargar()
        props.remove("token")
        props.remove("userId")
        props.remove("email")
        props.remove("rol")
        props.remove("explotacion_id")
        props.remove("nombre")
        props.remove("explotacion_nombre")
        if (props.isEmpty) {
            if (ficheroSesion.exists()) ficheroSesion.delete()
        } else {
            guardar(props)
        }
    }

    override fun haySesion(): Boolean = obtenerToken() != null
}

actual fun crearSessionStorage(): SessionStorage = SessionStorageJvm()
