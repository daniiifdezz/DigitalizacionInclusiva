package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs del cliente — espejo de los DTOs del backend.
 * Se definen aquí para no acoplar commonMain al módulo backend.
 */
@Serializable
data class ActividadDto(
    @SerialName("id")                    val id                   : Int,
    @SerialName("parcelaId")             val parcelaId            : Int,
    @SerialName("equipoId")              val equipoId             : Int?    = null,
    @SerialName("aplicadorId")           val aplicadorId          : Int?    = null,
    @SerialName("fechaInicio")           val fechaInicio          : String,
    @SerialName("fechaFin")              val fechaFin             : String? = null,
    @SerialName("superficieTratada")     val superficieTratada    : Double? = null,
    @SerialName("problemaFitosanitario") val problemaFitosanitario: String? = null,
    @SerialName("eficacia")              val eficacia             : String? = null,
    @SerialName("observaciones")         val observaciones        : String? = null
)

@Serializable
data class ActividadCreateDto(
    @SerialName("parcelaId")             val parcelaId            : Int,
    @SerialName("equipoId")              val equipoId             : Int?    = null,
    @SerialName("aplicadorId")           val aplicadorId          : Int?    = null,
    @SerialName("fechaInicio")           val fechaInicio          : String,
    @SerialName("fechaFin")              val fechaFin             : String? = null,
    @SerialName("superficieTratada")     val superficieTratada    : Double? = null,
    @SerialName("problemaFitosanitario") val problemaFitosanitario: String? = null,
    @SerialName("eficacia")              val eficacia             : String? = null,
    @SerialName("observaciones")         val observaciones        : String? = null
)

@Serializable
data class ParcelaDto(
    @SerialName("id")                    val id                   : Int,
    @SerialName("explotacionId")         val explotacionId        : Int,
    @SerialName("orden")                 val orden                : Int?     = null,
    @SerialName("sistemaAsesoramiento")  val sistemaAsesoramiento : String?  = null,
    @SerialName("zonaNitratos")          val zonaNitratos         : Boolean? = null
)

@Serializable
data class ProductoDto(
    @SerialName("id")               val id              : Int,
    @SerialName("nombreComercial")  val nombreComercial : String? = null,
    @SerialName("materiaActiva")    val materiaActiva   : String? = null,
    @SerialName("numeroRegistro")   val numeroRegistro  : String? = null
)
/**
 * Fuente de datos remota — encapsula todas las llamadas HTTP.
 * El repositorio usa esta clase, nunca HttpClient directamente.
 */
class ActividadApi(private val client: HttpClient) {

    // Actividades

    suspend fun getActividades(): List<ActividadDto> =
        client.get("$BASE_URL/api/actividades").body()

    suspend fun getActividad(id: Int): ActividadDto =
        client.get("$BASE_URL/api/actividades/$id").body()

    suspend fun crearActividad(actividad: ActividadCreateDto): ActividadDto {
        return client.post("$BASE_URL/api/actividades") {
            contentType(ContentType.Application.Json)
            setBody(actividad)
        }.body()
    }

    suspend fun actualizarActividad(id: Int, actividad: ActividadCreateDto): Boolean {
        val response = client.put("$BASE_URL/api/actividades/$id") {
            contentType(ContentType.Application.Json)
            setBody(actividad)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun eliminarActividad(id: Int): Boolean {
        val response = client.delete("$BASE_URL/api/actividades/$id")
        return response.status == HttpStatusCode.NoContent
    }

    // parcelas --> solo lectura

    suspend fun getParcelas(): List<ParcelaDto> =
        client.get("$BASE_URL/api/parcelas").body()

    suspend fun getParcela(id: Int): ParcelaDto =
        client.get("$BASE_URL/api/parcelas/$id").body()
}