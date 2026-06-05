package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.dferna14.project.data.local.SessionStorage


@Serializable
enum class EstadoActividadDto {
    @SerialName("BORRADOR") BORRADOR,
    @SerialName("PENDIENTE_VALIDAR") PENDIENTE_VALIDAR,
    @SerialName("VALIDADA") VALIDADA
}

@Serializable
data class ActividadDto(
    val id                   : Int,
    val parcelaId            : Int,
    val parcelaAlias         : String? = null,
    val equipoId             : Int?    = null,
    val aplicadorId          : Int?    = null,
    val fechaInicio          : String,
    val fechaFin             : String? = null,
    val superficieTratada    : Double? = null,
    val problemaFitosanitario: String? = null,
    val eficacia             : String? = null,
    val observaciones        : String? = null,
    val estado               : EstadoActividadDto = EstadoActividadDto.BORRADOR
)

@Serializable
data class ActividadCreateDto(
    val parcelaId            : Int,
    val equipoId             : Int?    = null,
    val aplicadorId          : Int?    = null,
    val fechaInicio          : String,
    val fechaFin             : String? = null,
    val superficieTratada    : Double? = null,
    val problemaFitosanitario: String? = null,
    val eficacia             : String? = null,
    val observaciones        : String? = null,
    val estado               : EstadoActividadDto = EstadoActividadDto.BORRADOR
)

@Serializable
data class ParcelaDto(
    @SerialName("id")                    val id                   : Int,
    @SerialName("explotacionId")         val explotacionId        : Int?    = null,
    @SerialName("orden")                 val orden                : Int?    = null,
    @SerialName("alias")                 val alias                : String? = null,
    @SerialName("sistemaAsesoramiento")  val sistemaAsesoramiento : String? = null,
    @SerialName("zonaNitratos")          val zonaNitratos         : Boolean? = null
)

@Serializable
data class ParcelaCreateDto(
    @SerialName("explotacionId")         val explotacionId        : Int?    = null,
    @SerialName("orden")                 val orden                : Int?    = null,
    @SerialName("alias")                 val alias                : String? = null,
    @SerialName("sistemaAsesoramiento")  val sistemaAsesoramiento : String? = null,
    @SerialName("zonaNitratos")          val zonaNitratos         : Boolean? = null
)

@Serializable
data class ProductoDto(
    @SerialName("id")               val id              : Int,
    @SerialName("nombreComercial")  val nombreComercial : String? = null,
    @SerialName("materiaActiva")    val materiaActiva   : String? = null,
    @SerialName("numeroRegistro")   val numeroRegistro  : String? = null,
    @SerialName("tipo")             val tipo            : String  = "FITOSANITARIO",
    @SerialName("riquezaNpk")       val riquezaNpk      : String? = null,
    @SerialName("tipoFertilizante") val tipoFertilizante: String? = null
)

@Serializable
data class ProductoCreateDto(
    @SerialName("nombreComercial")  val nombreComercial : String? = null,
    @SerialName("materiaActiva")    val materiaActiva   : String? = null,
    @SerialName("numeroRegistro")   val numeroRegistro  : String? = null,
    @SerialName("tipo")             val tipo            : String  = "FITOSANITARIO",
    @SerialName("riquezaNpk")       val riquezaNpk      : String? = null,
    @SerialName("tipoFertilizante") val tipoFertilizante: String? = null
)

// Conteo de dependencias para el borrado en cascada (Desktop).
@Serializable
data class DependenciasParcelaDto(
    @SerialName("actividades")      val actividades      : Int = 0,
    @SerialName("semillas")         val semillas         : Int = 0,
    @SerialName("fertilizaciones")  val fertilizaciones  : Int = 0,
    @SerialName("referenciaSigpac") val referenciaSigpac : Int = 0,
    @SerialName("datosAgronomicos") val datosAgronomicos : Int = 0
)

@Serializable
data class DependenciasProductoDto(
    @SerialName("actividadProductos") val actividadProductos : Int = 0,
    @SerialName("semillas")           val semillas           : Int = 0,
    @SerialName("fertilizaciones")    val fertilizaciones    : Int = 0
)

@Serializable
data class SemillaTratadaDto(
    @SerialName("id")                       val id                      : Int,
    @SerialName("actividadId")              val actividadId             : Int,
    @SerialName("parcelaId")                val parcelaId               : Int,
    @SerialName("aplica")                   val aplica                  : Boolean = false,
    @SerialName("fechaSiembra")             val fechaSiembra            : String? = null,
    @SerialName("superficieHa")             val superficieHa            : Double? = null,
    @SerialName("cantidadSemillaKg")        val cantidadSemillaKg       : Double? = null,
    @SerialName("productoId")               val productoId              : Int?    = null,
    @SerialName("variedadSemilla")          val variedadSemilla         : String? = null,
    @SerialName("cultivoId")                val cultivoId               : Int?    = null,
    @SerialName("productoNombreComercial")  val productoNombreComercial : String? = null,
    @SerialName("productoNumeroRegistro")   val productoNumeroRegistro  : String? = null,
    @SerialName("productoMateriaActiva")    val productoMateriaActiva   : String? = null
)

@Serializable
data class SemillaTratadaCreateDto(
    @SerialName("actividadId")       val actividadId       : Int,
    @SerialName("parcelaId")         val parcelaId         : Int,
    @SerialName("aplica")            val aplica            : Boolean = false,
    @SerialName("fechaSiembra")      val fechaSiembra      : String? = null,
    @SerialName("superficieHa")      val superficieHa      : Double? = null,
    @SerialName("cantidadSemillaKg") val cantidadSemillaKg : Double? = null,
    @SerialName("productoId")        val productoId        : Int?    = null,
    @SerialName("variedadSemilla")   val variedadSemilla   : String? = null,
    @SerialName("cultivoId")         val cultivoId         : Int?    = null
)

@Serializable
data class FertilizacionDto(
    @SerialName("id")                val id                : Int,
    @SerialName("actividadId")       val actividadId       : Int?    = null,
    @SerialName("productoId")        val productoId        : Int?    = null,
    @SerialName("cultivoId")         val cultivoId         : Int?    = null,
    @SerialName("aplica")            val aplica            : Boolean = false,
    @SerialName("fechaInicio")       val fechaInicio       : String? = null,
    @SerialName("fechaFin")          val fechaFin          : String? = null,
    @SerialName("tipoProducto")      val tipoProducto      : String? = null,
    @SerialName("numeroAlbaran")     val numeroAlbaran     : String? = null,
    @SerialName("riquezaNpk")        val riquezaNpk        : String? = null,
    @SerialName("dosis")             val dosis             : Double? = null,
    @SerialName("tipoFertilizacion") val tipoFertilizacion : String? = null,
    @SerialName("observaciones")     val observaciones     : String? = null
)

@Serializable
data class FertilizacionCreateDto(
    @SerialName("actividadId")       val actividadId       : Int?    = null,
    @SerialName("productoId")        val productoId        : Int?    = null,
    @SerialName("cultivoId")         val cultivoId         : Int?    = null,
    @SerialName("aplica")            val aplica            : Boolean = false,
    @SerialName("fechaInicio")       val fechaInicio       : String? = null,
    @SerialName("fechaFin")          val fechaFin          : String? = null,
    @SerialName("tipoProducto")      val tipoProducto      : String? = null,
    @SerialName("numeroAlbaran")     val numeroAlbaran     : String? = null,
    @SerialName("riquezaNpk")        val riquezaNpk        : String? = null,
    @SerialName("dosis")             val dosis             : Double? = null,
    @SerialName("tipoFertilizacion") val tipoFertilizacion : String? = null,
    @SerialName("observaciones")     val observaciones     : String? = null
)

@Serializable
data class ActividadProductoDto(
    @SerialName("id")                       val id                      : Int,
    @SerialName("actividadId")              val actividadId             : Int,
    @SerialName("productoId")               val productoId              : Int,
    @SerialName("dosis")                    val dosis                   : Double,
    @SerialName("productoNombreComercial")  val productoNombreComercial : String? = null,
    @SerialName("productoNumeroRegistro")   val productoNumeroRegistro  : String? = null,
    @SerialName("productoMateriaActiva")    val productoMateriaActiva   : String? = null
)

@Serializable
data class ActividadProductoCreateDto(
    @SerialName("productoId") val productoId : Int,
    @SerialName("dosis")      val dosis      : Double
)

@Serializable
data class EquipoDto(
    @SerialName("id")                    val id                    : Int,
    @SerialName("explotacionId")         val explotacionId         : Int?    = null,
    @SerialName("tipo")                  val tipo                  : String,
    @SerialName("marca")                 val marca                 : String? = null,
    @SerialName("modelo")                val modelo                : String? = null,
    @SerialName("numeroRoma")            val numeroRoma            : String? = null,
    @SerialName("anyoFabricacion")       val anyoFabricacion       : Int?    = null,
    @SerialName("fechaUltimaInspeccion") val fechaUltimaInspeccion : String? = null
)

@Serializable
data class EquipoCreateDto(
    @SerialName("explotacionId")         val explotacionId         : Int?    = null,
    @SerialName("tipo")                  val tipo                  : String,
    @SerialName("marca")                 val marca                 : String? = null,
    @SerialName("modelo")                val modelo                : String? = null,
    @SerialName("numeroRoma")            val numeroRoma            : String? = null,
    @SerialName("anyoFabricacion")       val anyoFabricacion       : Int?    = null,
    @SerialName("fechaUltimaInspeccion") val fechaUltimaInspeccion : String? = null
)

@Serializable
data class UsuarioDto(
    @SerialName("id")             val id             : Int,
    @SerialName("nombre")         val nombre         : String,
    @SerialName("apellidos")      val apellidos      : String? = null,
    @SerialName("email")          val email          : String,
    @SerialName("rol")            val rol            : String,
    @SerialName("explotacionId")  val explotacionId  : Int?    = null,
    @SerialName("fechaAlta")      val fechaAlta      : String? = null,
    @SerialName("tipoCarnetRopo") val tipoCarnetRopo : String? = null
)

@Serializable
data class UsuarioCreateDto(
    @SerialName("nombre")         val nombre         : String,
    @SerialName("apellidos")      val apellidos      : String? = null,
    @SerialName("email")          val email          : String,
    @SerialName("rol")            val rol            : String? = null,
    @SerialName("explotacionId")  val explotacionId  : Int?    = null,
    @SerialName("tipoCarnetRopo") val tipoCarnetRopo : String? = null
)

@Serializable
data class LoginRequest(
    @SerialName("email")    val email    : String,
    @SerialName("password") val password : String
)

@Serializable
data class RegisterRequest(
    @SerialName("email")    val email    : String,
    @SerialName("password") val password : String,
    @SerialName("nombre")   val nombre   : String,
    @SerialName("rol")      val rol      : String? = null
)

/**
 * Respuesta de login/registro: JWT firmado + datos del usuario.
 */
@Serializable
data class LoginResponseDto(
    @SerialName("token")   val token   : String,
    @SerialName("usuario") val usuario : UsuarioDto
)

/**
 * Excepción específica para conflictos 409 del backend.
 */
class ConflictException(message: String) : RuntimeException(message)

@Serializable
data class CambioRolRequest(val nuevoRol: String)

@Serializable
private data class ErrorMessage(val message: String? = null)

/**
 * Fuente de datos remota — encapsula todas las llamadas HTTP.
 * El repositorio usa esta clase, nunca HttpClient directamente.
 */
class ActividadApi(private val client: HttpClient, private val sessionStorage: SessionStorage) {

    // Actividades

    suspend fun getActividades(): List<ActividadDto> {
        println("DEBUG: Intentando conectar a ${baseUrl(sessionStorage)}/api/actividades")
        try {
            val response = client.get("${baseUrl(sessionStorage)}/api/actividades")
            println("DEBUG: Respuesta recibida: ${response.status}")
            return response.body<List<ActividadDto>>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("DEBUG: ERROR CRÍTICO: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getActividad(id: Int): ActividadDto =
        client.get("${baseUrl(sessionStorage)}/api/actividades/$id").body<ActividadDto>()

    suspend fun crearActividad(actividad: ActividadCreateDto): ActividadDto {
        return client.post("${baseUrl(sessionStorage)}/api/actividades") {
            contentType(ContentType.Application.Json)
            setBody(actividad)
        }.body<ActividadDto>()
    }

    suspend fun actualizarActividad(id: Int, actividad: ActividadCreateDto): Boolean {
        val response = client.put("${baseUrl(sessionStorage)}/api/actividades/$id") {
            contentType(ContentType.Application.Json)
            setBody(actividad)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun eliminarActividad(id: Int): Boolean {
        val response = client.delete("${baseUrl(sessionStorage)}/api/actividades/$id")
        return response.status == HttpStatusCode.NoContent
    }

    suspend fun getActividadesPendientes(): List<ActividadDto> =
        client.get("${baseUrl(sessionStorage)}/api/actividades/pendientes").body<List<ActividadDto>>()

    suspend fun enviarActividad(id: Int): ActividadDto =
        client.post("${baseUrl(sessionStorage)}/api/actividades/$id/enviar").body<ActividadDto>()

    suspend fun validarActividad(id: Int): ActividadDto =
        client.post("${baseUrl(sessionStorage)}/api/actividades/$id/validar").body<ActividadDto>()

    suspend fun devolverActividad(id: Int): Boolean {
        val response = client.post("${baseUrl(sessionStorage)}/api/actividades/$id/devolver")
        return response.status == HttpStatusCode.OK
    }

    // Productos aplicados a una actividad (actividad_producto)

    suspend fun getActividadProductos(actividadId: Int): List<ActividadProductoDto> =
        client.get("${baseUrl(sessionStorage)}/api/actividades/$actividadId/productos").body()

    suspend fun crearActividadProducto(
        actividadId: Int,
        request: ActividadProductoCreateDto
    ): ActividadProductoDto {
        return client.post("${baseUrl(sessionStorage)}/api/actividades/$actividadId/productos") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun eliminarActividadProducto(actividadId: Int, actividadProductoId: Int): Boolean {
        val response = client.delete(
            "${baseUrl(sessionStorage)}/api/actividades/$actividadId/productos/$actividadProductoId"
        )
        return response.status == HttpStatusCode.NoContent
    }

    // Semillas tratadas

    suspend fun getSemillaTratada(actividadId: Int): SemillaTratadaDto? {
        return try {
            client.get("${baseUrl(sessionStorage)}/api/actividades/$actividadId/semilla").body<SemillaTratadaDto?>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearSemillaTratada(semilla: SemillaTratadaCreateDto): SemillaTratadaDto {
        return client.post("${baseUrl(sessionStorage)}/api/actividades/${semilla.actividadId}/semilla") {
            contentType(ContentType.Application.Json)
            setBody(semilla)
        }.body<SemillaTratadaDto>()
    }

    // Parcelas

    suspend fun getParcelas(): List<ParcelaDto> =
        client.get("${baseUrl(sessionStorage)}/api/parcelas").body<List<ParcelaDto>>()

    suspend fun getParcela(id: Int): ParcelaDto =
        client.get("${baseUrl(sessionStorage)}/api/parcelas/$id").body<ParcelaDto>()

    suspend fun crearParcela(parcela: ParcelaCreateDto): ParcelaDto {
        return client.post("${baseUrl(sessionStorage)}/api/parcelas") {
            contentType(ContentType.Application.Json)
            setBody(parcela)
        }.body<ParcelaDto>()
    }

    suspend fun actualizarParcela(id: Int, parcela: ParcelaCreateDto): Boolean {
        val response = client.put("${baseUrl(sessionStorage)}/api/parcelas/$id") {
            contentType(ContentType.Application.Json)
            setBody(parcela)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun eliminarParcela(id: Int): Boolean {
        val response = client.delete("${baseUrl(sessionStorage)}/api/parcelas/$id")
        if (response.status == HttpStatusCode.Conflict) {
            val msg = runCatching { response.body<ErrorMessage>().message }.getOrNull()
                ?: "La parcela tiene datos asociados y no se puede eliminar"
            throw ConflictException(msg)
        }
        return response.status == HttpStatusCode.NoContent
    }

    // Borrado en cascada de parcela solo dekstop.
    suspend fun getDependenciasParcela(id: Int): DependenciasParcelaDto =
        client.get("${baseUrl(sessionStorage)}/api/parcelas/$id/dependencias").body()

    suspend fun eliminarParcelaEnCascada(id: Int): Boolean {
        val response = client.delete("${baseUrl(sessionStorage)}/api/parcelas/$id/cascada")
        return response.status == HttpStatusCode.NoContent
    }

    // Productos

    suspend fun getProductos(): List<ProductoDto> =
        client.get("${baseUrl(sessionStorage)}/api/productos").body<List<ProductoDto>>()

    suspend fun getProductosPorTipo(tipo: String): List<ProductoDto> =
        client.get("${baseUrl(sessionStorage)}/api/productos") {
            parameter("tipo", tipo)
        }.body<List<ProductoDto>>()

    suspend fun crearProducto(producto: ProductoCreateDto): ProductoDto {
        return client.post("${baseUrl(sessionStorage)}/api/productos") {
            contentType(ContentType.Application.Json)
            setBody(producto)
        }.body<ProductoDto>()
    }

    suspend fun actualizarProducto(id: Int, producto: ProductoCreateDto): Boolean {
        val response = client.put("${baseUrl(sessionStorage)}/api/productos/$id") {
            contentType(ContentType.Application.Json)
            setBody(producto)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun eliminarProducto(id: Int): Boolean {
        val response = client.delete("${baseUrl(sessionStorage)}/api/productos/$id")
        if (response.status == HttpStatusCode.Conflict) {
            val msg = runCatching { response.body<ErrorMessage>().message }.getOrNull()
                ?: "El producto está siendo usado y no se puede eliminar"
            throw ConflictException(msg)
        }
        return response.status == HttpStatusCode.NoContent
    }

    // Borrado en cascada de producto (solo Desktop)
    suspend fun getDependenciasProducto(id: Int): DependenciasProductoDto =
        client.get("${baseUrl(sessionStorage)}/api/productos/$id/dependencias").body()

    suspend fun eliminarProductoEnCascada(id: Int): Boolean {
        val response = client.delete("${baseUrl(sessionStorage)}/api/productos/$id/cascada")
        return response.status == HttpStatusCode.NoContent
    }

    // Fertilizacion functions
    suspend fun getFertilizaciones(): List<FertilizacionDto> =
        client.get("${baseUrl(sessionStorage)}/api/fertilizaciones").body<List<FertilizacionDto>>()

    suspend fun getFertilizacionByActividad(actividadId: Int): FertilizacionDto? {
        return try {
            val response = client.get("${baseUrl(sessionStorage)}/api/actividades/$actividadId/fertilizacion")
            if (response.status == HttpStatusCode.NotFound) null
            else response.body<FertilizacionDto>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun upsertFertilizacionDeActividad(
        actividadId: Int,
        fertilizacion: FertilizacionCreateDto
    ): FertilizacionDto {
        return client.post("${baseUrl(sessionStorage)}/api/actividades/$actividadId/fertilizacion") {
            contentType(ContentType.Application.Json)
            setBody(fertilizacion)
        }.body<FertilizacionDto>()
    }

    // Equipos de aplicación

    suspend fun getEquipos(): List<EquipoDto> =
        client.get("${baseUrl(sessionStorage)}/api/equipos").body<List<EquipoDto>>()

    suspend fun crearEquipo(equipo: EquipoCreateDto): EquipoDto =
        client.post("${baseUrl(sessionStorage)}/api/equipos") {
            contentType(ContentType.Application.Json)
            setBody(equipo)
        }.body<EquipoDto>()

    suspend fun eliminarEquipo(id: Int): Boolean {
        val response = client.delete("${baseUrl(sessionStorage)}/api/equipos/$id")
        if (response.status == HttpStatusCode.Conflict) {
            val msg = runCatching { response.body<ErrorMessage>().message }.getOrNull()
                ?: "El equipo está asignado a actividades y no se puede eliminar"
            throw ConflictException(msg)
        }
        return response.status == HttpStatusCode.NoContent
    }

    // Usuarios (para dropdown de aplicador)

    suspend fun getUsuarios(rol: String? = null): List<UsuarioDto> {
        val url = if (rol != null) "${baseUrl(sessionStorage)}/api/usuarios?rol=$rol" else "${baseUrl(sessionStorage)}/api/usuarios"
        return client.get(url).body<List<UsuarioDto>>()
    }

    suspend fun crearUsuario(usuario: UsuarioCreateDto): UsuarioDto {
        val response = client.post("${baseUrl(sessionStorage)}/api/usuarios") {
            contentType(ContentType.Application.Json)
            setBody(usuario)
        }
        if (response.status == HttpStatusCode.Conflict) {
            val msg = runCatching { response.body<ErrorMessage>().message }.getOrNull()
                ?: "Ya existe un usuario con ese email"
            throw ConflictException(msg)
        }
        return response.body<UsuarioDto>()
    }

    suspend fun eliminarUsuario(id: Int): Boolean {
        val response = client.delete("${baseUrl(sessionStorage)}/api/usuarios/$id")
        if (response.status == HttpStatusCode.Conflict) {
            val msg = runCatching { response.body<ErrorMessage>().message }.getOrNull()
                ?: "El aplicador está asignado a actividades y no se puede eliminar"
            throw ConflictException(msg)
        }
        return response.status == HttpStatusCode.NoContent
    }

    // Autenticación

    suspend fun login(request: LoginRequest): LoginResponseDto {
        val response = client.post("${baseUrl(sessionStorage)}/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException(
                if (response.status == HttpStatusCode.Unauthorized) "Credenciales inválidas"
                else "Error al iniciar sesión (${response.status.value})"
            )
        }
        return response.body<LoginResponseDto>()
    }

    suspend fun register(request: RegisterRequest): LoginResponseDto {
        val response = client.post("${baseUrl(sessionStorage)}/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status != HttpStatusCode.Created && response.status != HttpStatusCode.OK) {
            throw IllegalStateException(
                when (response.status) {
                    HttpStatusCode.Conflict   -> "Ya existe un usuario con ese email"
                    HttpStatusCode.BadRequest -> "Datos de registro inválidos"
                    else -> "Error al registrar (${response.status.value})"
                }
            )
        }
        return response.body<LoginResponseDto>()
    }

    /**
     * Valida el JWT guardado y devuelve el usuario actual. El token viaja en el
     * header Authorization gracias al defaultRequest del HttpClient.
     */
    suspend fun getMe(): UsuarioDto {
        val response = client.get("${baseUrl(sessionStorage)}/api/auth/me")
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Sesión no válida (${response.status.value})")
        }
        return response.body<UsuarioDto>()
    }

    
    suspend fun cambiarRolUsuario(usuarioId: Int, nuevoRol: String): HttpResponse {
        return client.put("${baseUrl(sessionStorage)}/api/usuarios/$usuarioId/rol") {
            contentType(ContentType.Application.Json)
            setBody(CambioRolRequest(nuevoRol))
        }
    }
}


