package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * DTOs del cliente — espejo de los DTOs del backend.
 * Se definen aquí para no acoplar commonMain al módulo backend.
 */

@Serializable
data class ActividadDto(
    val id                   : Int,
    val parcelaId            : Int,
    val equipoId             : Int?    = null,
    val aplicadorId          : Int?    = null,
    val fechaInicio          : String,
    val fechaFin             : String? = null,
    val superficieTratada    : Double? = null,
    val problemaFitosanitario: String? = null,
    val eficacia             : String? = null,
    val observaciones        : String? = null
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
    val observaciones        : String? = null
)

@Serializable
data class ParcelaDto(
    val id                   : Int,
    val explotacionId        : Int,
    val orden                : Int?     = null,
    val sistemaAsesoramiento : String?  = null,
    val zonaNitratos         : Boolean? = null
)

@Serializable
data class ProductoDto(
    val id              : Int,
    val nombreComercial : String? = null,
    val materiaActiva   : String? = null,
    val numeroRegistro  : String? = null
)

/**
 * Fuente de datos remota — encapsula todas las llamadas HTTP.
 * El repositorio usa esta clase, nunca HttpClient directamente.
 */
class ActividadApi(private val client: HttpClient) {

    // ── Actividades ───────────────────────────────────────────────────────────

    suspend fun getActividades(): List<ActividadDto> =
        client.get("$BASE_URL/api/actividades").body()

    suspend fun getActividad(id: Int): ActividadDto =
        client.get("$BASE_URL/api/actividades/$id").body()

    suspend fun crearActividad(actividad: ActividadCreateDto): ActividadDto =
        client.post("$BASE_URL/api/actividades") {
            contentType(ContentType.Application.Json)
            setBody(actividad)
        }.body()

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

    // ── Parcelas (solo lectura) ────────────────────────────────────────────────

    suspend fun getParcelas(): List<ParcelaDto> =
        client.get("$BASE_URL/api/parcelas").body()

    suspend fun getParcela(id: Int): ParcelaDto =
        client.get("$BASE_URL/api/parcelas/$id").body()
}