package org.dferna14.project.domain.model

/**
 * Modelos de dominio de la aplicación.
 *
 * Son distintos de los DTOs del backend y de las entidades SQLDelight.
 * Representan los datos tal como los necesita la UI y la lógica de negocio.
 * La conversión entre capas se hace en el repositorio.
 */

data class Parcela(
    val id                   : Int,
    val explotacionId        : Int,
    val orden                : Int?     = null,
    val sistemaAsesoramiento : String?  = null,
    val zonaNitratos         : Boolean? = null
)

data class Producto(
    val id             : Int,
    val nombreComercial: String? = null,
    val materiaActiva  : String? = null,
    val numeroRegistro : String? = null
)

data class  Actividad(
    val id                   : Int           = 0,
    val parcelaId            : Int,
    val equipoId             : Int?     = null,
    val aplicadorId          : Int?     = null,
    val fechaInicio          : String,
    val fechaFin             : String?  = null,
    val superficieTratada    : Double?  = null,
    val problemaFitosanitario: String?  = null,
    val eficacia             : String?  = null,
    val observaciones        : String?  = null,
    // Estado de sincronización offline-first
    val sincronizado         : Boolean  = false
)

data class ActividadProducto(
    val id          : Int = 0,
    val actividadId : Int,
    val productoId  : Int,
    val dosis       : Double
)

data class SemillaTratada(
    val id                : Int     = 0,
    val actividadId       : Int,
    val parcelaId         : Int,
    val aplica            : Boolean = false,
    val fechaSiembra      : String? = null,
    val superficieHa      : Double? = null,
    val cantidadSemillaKg : Double? = null,
    val productoId        : Int?    = null
)

/**
 * Wrapper para manejo de estados de carga, exito y error, sin el uso de excepciones
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}