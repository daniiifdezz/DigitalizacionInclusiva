package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dferna14.project.data.local.SessionStorage

// DTOs específicos de la pantalla de edición de parcela. Reusan la forma de los
// DTOs del backend (DTOs.kt) pero declarados aquí para no acoplar el módulo
// commonMain al módulo backend.

@Serializable
data class ReferenciaSigpacDto(
    @SerialName("id")               val id               : Int,
    @SerialName("parcelaId")        val parcelaId        : Int,
    @SerialName("provincia")        val provincia        : String? = null,
    @SerialName("terminoMunicipal") val terminoMunicipal : String? = null,
    @SerialName("codigoAgregado")   val codigoAgregado   : String? = null,
    @SerialName("zona")             val zona             : String? = null,
    @SerialName("numeroPoligono")   val numeroPoligono   : String? = null,
    @SerialName("numeroParcela")    val numeroParcela    : String? = null,
    @SerialName("numeroRecinto")    val numeroRecinto    : String? = null,
    @SerialName("usoSigpac")        val usoSigpac        : String? = null,
    @SerialName("superficieHa")     val superficieHa     : Double? = null
)

@Serializable
data class ReferenciaSigpacCreateDto(
    @SerialName("provincia")        val provincia        : String? = null,
    @SerialName("terminoMunicipal") val terminoMunicipal : String? = null,
    @SerialName("codigoAgregado")   val codigoAgregado   : String? = null,
    @SerialName("zona")             val zona             : String? = null,
    @SerialName("numeroPoligono")   val numeroPoligono   : String? = null,
    @SerialName("numeroParcela")    val numeroParcela    : String? = null,
    @SerialName("numeroRecinto")    val numeroRecinto    : String? = null,
    @SerialName("usoSigpac")        val usoSigpac        : String? = null,
    @SerialName("superficieHa")     val superficieHa     : Double? = null
)

@Serializable
data class DatosAgronomicosDto(
    @SerialName("id")                 val id                 : Int,
    @SerialName("parcelaId")          val parcelaId          : Int,
    @SerialName("especieVariedad")    val especieVariedad    : String? = null,
    @SerialName("ecoregimenPractica") val ecoregimenPractica : String? = null,
    @SerialName("secanoRegadio")      val secanoRegadio      : String? = null,
    @SerialName("cultivoId")          val cultivoId          : Int?    = null,
    @SerialName("fechaInicio")        val fechaInicio        : String? = null,
    @SerialName("fechaFin")           val fechaFin           : String? = null,
    @SerialName("aireLibreProtegido") val aireLibreProtegido : String? = null
)

@Serializable
data class DatosAgronomicosCreateDto(
    @SerialName("especieVariedad")    val especieVariedad    : String? = null,
    @SerialName("ecoregimenPractica") val ecoregimenPractica : String? = null,
    @SerialName("secanoRegadio")      val secanoRegadio      : String? = null,
    @SerialName("cultivoId")          val cultivoId          : Int?    = null,
    @SerialName("fechaInicio")        val fechaInicio        : String? = null,
    @SerialName("fechaFin")           val fechaFin           : String? = null,
    @SerialName("aireLibreProtegido") val aireLibreProtegido : String? = null
)

@Serializable
data class ParcelaCompletaDto(
    @SerialName("parcela")          val parcela          : ParcelaDto,
    @SerialName("referenciaSigpac") val referenciaSigpac : ReferenciaSigpacDto? = null,
    @SerialName("datosAgronomicos") val datosAgronomicos : DatosAgronomicosDto? = null
)

@Serializable
data class CultivoDto(
    @SerialName("id")       val id       : Int,
    @SerialName("especie")  val especie  : String? = null,
    @SerialName("variedad") val variedad : String? = null
)

class ParcelaApi(private val client: HttpClient, private val sessionStorage: SessionStorage) {

    suspend fun getParcelaCompleta(id: Int): ParcelaCompletaDto? {
        return try {
            client.get("${baseUrl(sessionStorage)}/api/parcelas/$id/completa").body<ParcelaCompletaDto?>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Upsert de la referencia SIGPAC para una parcela.
     * Caller indica si el registro ya existe (PUT) o no (POST).
     */
    suspend fun crearOActualizarSigpac(
        parcelaId: Int,
        request: ReferenciaSigpacCreateDto,
        esActualizacion: Boolean
    ): Boolean {
        val response = if (esActualizacion) {
            client.put("${baseUrl(sessionStorage)}/api/parcelas/$parcelaId/sigpac") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } else {
            client.post("${baseUrl(sessionStorage)}/api/parcelas/$parcelaId/sigpac") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
        return response.status.isSuccess()
    }

    /**
     * Upsert de los datos agronómicos para una parcela.
     */
    suspend fun crearOActualizarAgronomico(
        parcelaId: Int,
        request: DatosAgronomicosCreateDto,
        esActualizacion: Boolean
    ): Boolean {
        val response = if (esActualizacion) {
            client.put("${baseUrl(sessionStorage)}/api/parcelas/$parcelaId/agronomico") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } else {
            client.post("${baseUrl(sessionStorage)}/api/parcelas/$parcelaId/agronomico") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
        return response.status.isSuccess()
    }

    suspend fun getCultivos(): List<CultivoDto> =
        client.get("${baseUrl(sessionStorage)}/api/cultivos").body()
}
