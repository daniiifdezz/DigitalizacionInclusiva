package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    @SerialName("numeroRegistro")   val numeroRegistro  : String? = null
)

@Serializable
data class ProductoCreateDto(
    @SerialName("nombreComercial")  val nombreComercial : String? = null,
    @SerialName("materiaActiva")    val materiaActiva   : String? = null,
    @SerialName("numeroRegistro")   val numeroRegistro  : String? = null
)

@Serializable
data class SemillaTratadaDto(
    @SerialName("id")                val id                : Int,
    @SerialName("actividadId")       val actividadId       : Int,
    @SerialName("parcelaId")         val parcelaId         : Int,
    @SerialName("aplica")            val aplica            : Boolean = false,
    @SerialName("fechaSiembra")      val fechaSiembra      : String? = null,
    @SerialName("superficieHa")      val superficieHa      : Double? = null,
    @SerialName("cantidadSemillaKg") val cantidadSemillaKg : Double? = null,
    @SerialName("productoId")        val productoId        : Int?    = null
)

@Serializable
data class SemillaTratadaCreateDto(
    @SerialName("actividadId")       val actividadId       : Int,
    @SerialName("parcelaId")         val parcelaId         : Int,
    @SerialName("aplica")            val aplica            : Boolean = false,
    @SerialName("fechaSiembra")      val fechaSiembra      : String? = null,
    @SerialName("superficieHa")      val superficieHa      : Double? = null,
    @SerialName("cantidadSemillaKg") val cantidadSemillaKg : Double? = null,
    @SerialName("productoId")        val productoId        : Int?    = null
)

@Serializable
data class FertilizacionDto(
    @SerialName("id")                val id                : Int,
    @SerialName("cultivoId")         val cultivoId         : Int?    = null,
    @SerialName("aplica")            val aplica            : Boolean = false,
    @SerialName("fechaInicio")       val fechaInicio       : String? = null,
    @SerialName("fechaFin")          val fechaFin          : String? = null,
    @SerialName("tipoProducto")      val tipoProducto      : String? = null,
    @SerialName("numeroAlbaran")     val numeroAlbaran     : String? = null,
    @SerialName("riquezaNPK")        val riquezaNPK        : String? = null,
    @SerialName("dosis")             val dosis             : Double? = null,
    @SerialName("tipoFertilizacion") val tipoFertilizacion : String? = null,
    @SerialName("observaciones")     val observaciones     : String? = null
)

@Serializable
data class FertilizacionCreateDto(
    @SerialName("cultivoId")         val cultivoId         : Int?    = null,
    @SerialName("aplica")            val aplica            : Boolean = false,
    @SerialName("fechaInicio")       val fechaInicio       : String? = null,
    @SerialName("fechaFin")          val fechaFin          : String? = null,
    @SerialName("tipoProducto")      val tipoProducto      : String? = null,
    @SerialName("numeroAlbaran")     val numeroAlbaran     : String? = null,
    @SerialName("riquezaNPK")        val riquezaNPK        : String? = null,
    @SerialName("dosis")             val dosis             : Double? = null,
    @SerialName("tipoFertilizacion") val tipoFertilizacion : String? = null,
    @SerialName("observaciones")     val observaciones     : String? = null
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

    suspend fun getActividadesPendientes(): List<ActividadDto> =
        client.get("$BASE_URL/api/actividades/pendientes").body()

    suspend fun enviarActividad(id: Int): ActividadDto =
        client.post("$BASE_URL/api/actividades/$id/enviar").body()

    suspend fun validarActividad(id: Int): ActividadDto =
        client.post("$BASE_URL/api/actividades/$id/validar").body()

    suspend fun devolverActividad(id: Int): Boolean {
        val response = client.post("$BASE_URL/api/actividades/$id/devolver")
        return response.status == HttpStatusCode.OK
    }

    // Semillas tratadas

    suspend fun getSemillaTratada(actividadId: Int): SemillaTratadaDto? {
        return try {
            client.get("$BASE_URL/api/actividades/$actividadId/semilla").body<SemillaTratadaDto?>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearSemillaTratada(semilla: SemillaTratadaCreateDto): SemillaTratadaDto {
        return client.post("$BASE_URL/api/actividades/${semilla.actividadId}/semilla") {
            contentType(ContentType.Application.Json)
            setBody(semilla)
        }.body()
    }


    // Parcelas

    suspend fun getParcelas(): List<ParcelaDto> =
        client.get("$BASE_URL/api/parcelas").body()

    suspend fun getParcela(id: Int): ParcelaDto =
        client.get("$BASE_URL/api/parcelas/$id").body()

    suspend fun crearParcela(parcela: ParcelaCreateDto): ParcelaDto {
        return client.post("$BASE_URL/api/parcelas") {
            contentType(ContentType.Application.Json)
            setBody(parcela)
        }.body()
    }

    suspend fun actualizarParcela(id: Int, parcela: ParcelaCreateDto): Boolean {
        val response = client.put("$BASE_URL/api/parcelas/$id") {
            contentType(ContentType.Application.Json)
            setBody(parcela)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun eliminarParcela(id: Int): Boolean {
        val response = client.delete("$BASE_URL/api/parcelas/$id")
        return response.status == HttpStatusCode.NoContent
    }

    // Productos

    suspend fun getProductos(): List<ProductoDto> =
        client.get("$BASE_URL/api/productos").body()

    suspend fun crearProducto(producto: ProductoCreateDto): ProductoDto {
        return client.post("$BASE_URL/api/productos") {
            contentType(ContentType.Application.Json)
            setBody(producto)
        }.body()
    }

    suspend fun actualizarProducto(id: Int, producto: ProductoCreateDto): Boolean {
        val response = client.put("$BASE_URL/api/productos/$id") {
            contentType(ContentType.Application.Json)
            setBody(producto)
        }
        return response.status == HttpStatusCode.OK
    }

    suspend fun eliminarProducto(id: Int): Boolean {
        val response = client.delete("$BASE_URL/api/productos/$id")
        return response.status == HttpStatusCode.NoContent
    }

    // Fertilizacion functions
    suspend fun getFertilizaciones(): List<FertilizacionDto> =
        client.get("$BASE_URL/api/fertilizaciones").body()

    suspend fun getFertilizacionByCultivo(cultivoId: Int): FertilizacionDto? {
        return try {
            client.get("$BASE_URL/api/fertilizaciones").body<List<FertilizacionDto>>()
                .find { it.cultivoId == cultivoId }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearFertilizacion(fertilizacion: FertilizacionCreateDto): FertilizacionDto {
        return client.post("$BASE_URL/api/fertilizaciones") {
            contentType(ContentType.Application.Json)
            setBody(fertilizacion)
        }.body()
    }
}