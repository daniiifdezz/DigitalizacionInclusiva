package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dferna14.project.data.local.SessionStorage

@Serializable
data class TitularDto(
    @SerialName("id")           val id           : Int,
    @SerialName("nombre")       val nombre       : String,
    @SerialName("apellidos")    val apellidos    : String? = null,
    @SerialName("nif")          val nif          : String,
    @SerialName("direccion")    val direccion    : String? = null,
    @SerialName("localidad")    val localidad    : String? = null,
    @SerialName("codigoPostal") val codigoPostal : String? = null,
    @SerialName("provincia")    val provincia    : String? = null,
    @SerialName("telefono")     val telefono     : String? = null,
    @SerialName("email")        val email        : String? = null
)

@Serializable
data class TitularCreateDto(
    @SerialName("nombre")       val nombre       : String,
    @SerialName("apellidos")    val apellidos    : String? = null,
    @SerialName("nif")          val nif          : String,
    @SerialName("direccion")    val direccion    : String? = null,
    @SerialName("localidad")    val localidad    : String? = null,
    @SerialName("codigoPostal") val codigoPostal : String? = null,
    @SerialName("provincia")    val provincia    : String? = null,
    @SerialName("telefono")     val telefono     : String? = null,
    @SerialName("email")        val email        : String? = null
)

class TitularApi(private val client: HttpClient, private val sessionStorage: SessionStorage) {

    /**
     * Devuelve el primer titular del sistema (la app monoexplotación de TFG asume uno solo)
     * o null si aún no se ha creado. La lista vacía no se considera error.
     */
    suspend fun getTitular(): TitularDto? {
        return try {
            client.get("${baseUrl(sessionStorage)}/api/titulares").body<List<TitularDto>>().firstOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearTitular(request: TitularCreateDto): TitularDto {
        return client.post("${baseUrl(sessionStorage)}/api/titulares") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<TitularDto>()
    }

    suspend fun actualizarTitular(id: Int, request: TitularCreateDto): TitularDto {
        return client.put("${baseUrl(sessionStorage)}/api/titulares/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<TitularDto>()
    }
}
