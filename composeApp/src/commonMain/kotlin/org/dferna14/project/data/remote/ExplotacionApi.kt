package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExplotacionDto(
    @SerialName("id")                 val id                 : Int,
    @SerialName("nombre")             val nombre             : String,
    @SerialName("titularId")          val titularId          : Int?    = null,
    @SerialName("nifEmpresa")         val nifEmpresa         : String? = null,
    @SerialName("registroNacional")   val registroNacional   : String? = null,
    @SerialName("registroAutonomico") val registroAutonomico : String? = null,
    @SerialName("direccion")          val direccion          : String? = null,
    @SerialName("municipio")          val municipio          : String? = null,
    @SerialName("provincia")          val provincia          : String? = null,
    @SerialName("codigoPostal")       val codigoPostal       : String? = null,
    @SerialName("telefonoFijo")       val telefonoFijo       : String? = null,
    @SerialName("telefonoMovil")      val telefonoMovil      : String? = null,
    @SerialName("email")              val email              : String? = null
)

@Serializable
data class ExplotacionCreateDto(
    @SerialName("nombre")             val nombre             : String,
    @SerialName("titularId")          val titularId          : Int?    = null,
    @SerialName("nifEmpresa")         val nifEmpresa         : String? = null,
    @SerialName("registroNacional")   val registroNacional   : String? = null,
    @SerialName("registroAutonomico") val registroAutonomico : String? = null,
    @SerialName("direccion")          val direccion          : String? = null,
    @SerialName("municipio")          val municipio          : String? = null,
    @SerialName("provincia")          val provincia          : String? = null,
    @SerialName("codigoPostal")       val codigoPostal       : String? = null,
    @SerialName("telefonoFijo")       val telefonoFijo       : String? = null,
    @SerialName("telefonoMovil")      val telefonoMovil      : String? = null,
    @SerialName("email")              val email              : String? = null
)

class ExplotacionApi(private val client: HttpClient) {

    /**
     * Devuelve la primera explotación del sistema (la app monoexplotación de TFG asume una sola)
     * o null si aún no se ha creado.
     */
    suspend fun getExplotacion(): ExplotacionDto? {
        return try {
            client.get("$BASE_URL/api/explotaciones").body<List<ExplotacionDto>>().firstOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lista completa de explotaciones — usada para los selectores de creación
     * y edición de parcela. En cuanto haya login, esta lista se filtrará por
     * la explotación del usuario autenticado.
     */
    suspend fun getExplotaciones(): List<ExplotacionDto> =
        client.get("$BASE_URL/api/explotaciones").body()

    suspend fun crearExplotacion(request: ExplotacionCreateDto): ExplotacionDto {
        return client.post("$BASE_URL/api/explotaciones") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ExplotacionDto>()
    }

    suspend fun actualizarExplotacion(id: Int, request: ExplotacionCreateDto): Boolean {
        val response = client.put("$BASE_URL/api/explotaciones/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.status == HttpStatusCode.OK
    }
}
