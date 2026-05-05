package org.dferna14.project.backend.model

import kotlinx.serialization.Serializable

/**
 * DTOs (Data Transfer Objects) — estructuras JSON que viajan entre la app y el backend.
 * Son distintos de las entidades de BD (Exposed) para mantener separación de capas.
 */

// Actividad

@Serializable
enum class EstadoActividad {
    BORRADOR,           // Creada en móvil, incompleta
    PENDIENTE_VALIDAR,   // Enviada, espera técnico
    VALIDADA             // Cerrada por técnico desktop
}

@Serializable
data class ActividadRequest(
    val parcelaId            : Int,
    val equipoId             : Int?     = null,
    val aplicadorId          : Int?     = null,
    val fechaInicio          : String,
    val fechaFin             : String?  = null,
    val superficieTratada    : Double?  = null,
    val problemaFitosanitario: String?  = null,
    val eficacia             : String?  = null,
    val observaciones        : String?  = null,
    val estado               : EstadoActividad = EstadoActividad.BORRADOR
)

@Serializable
data class ActividadResponse(
    val id                   : Int,
    val parcelaId            : Int,
    val equipoId             : Int?     = null,
    val aplicadorId          : Int?     = null,
    val fechaInicio          : String,
    val fechaFin             : String?  = null,
    val superficieTratada    : Double?  = null,
    val problemaFitosanitario: String?  = null,
    val eficacia             : String?  = null,
    val observaciones        : String?  = null,
    val estado               : EstadoActividad
)

// ProductoAPlicado

@Serializable
data class ActividadProductoRequest(
    val productoId: Int,
    val dosis     : Double
)

@Serializable
data class ActividadProductoResponse(
    val id        : Int,
    val actividadId: Int,
    val productoId: Int,
    val dosis     : Double
)

// Semilla tratada

@Serializable
data class SemillaTratadaRequest(
    val actividadId     : Int,
    val parcelaId       : Int,
    val aplica          : Boolean = false,
    val fechaSiembra    : String? = null,
    val superficieHa    : Double? = null,
    val cantidadSemillaKg: Double? = null,
    val productoId      : Int?    = null
)

@Serializable
data class SemillaTratadaResponse(
    val id              : Int,
    val actividadId     : Int,
    val parcelaId       : Int,
    val aplica          : Boolean,
    val fechaSiembra    : String? = null,
    val superficieHa    : Double? = null,
    val cantidadSemillaKg: Double? = null,
    val productoId      : Int?    = null
)

// Fertilización

@Serializable
data class FertilizacionRequest(
    val cultivoId        : Int?    = null,
    val aplica           : Boolean = false,
    val fechaInicio      : String? = null,
    val fechaFin         : String? = null,
    val tipoProducto     : String? = null,
    val numeroAlbaran    : String? = null,
    val riquezaNPK       : String? = null,
    val dosis            : Double? = null,
    val tipoFertilizacion: String? = null,
    val observaciones    : String? = null
)

@Serializable
data class FertilizacionResponse(
    val id               : Int,
    val cultivoId        : Int?    = null,
    val aplica           : Boolean,
    val fechaInicio      : String? = null,
    val fechaFin         : String? = null,
    val tipoProducto     : String? = null,
    val numeroAlbaran    : String? = null,
    val riquezaNPK       : String? = null,
    val dosis            : Double? = null,
    val tipoFertilizacion: String? = null,
    val observaciones    : String? = null
)

// Parcela

@Serializable
data class ParcelaRequest(
    val explotacionId       : Int?    = null,
    val orden               : Int?    = null,
    val alias               : String? = null,
    val sistemaAsesoramiento: String? = null,
    val zonaNitratos        : Boolean? = null
)

@Serializable
data class ParcelaResponse(
    val id                  : Int,
    val explotacionId       : Int?    = null,
    val orden               : Int?    = null,
    val alias               : String? = null,
    val sistemaAsesoramiento: String? = null,
    val zonaNitratos        : Boolean? = null
)

// Parcela Completa - combina parcela + referenciasigpac + datosagronomicos

@Serializable
data class ParcelaCompletaResponse(
    // Datos de parcela
    val id                   : Int,
    val explotacionId        : Int?    = null,
    val orden                : Int?    = null,
    val alias                : String? = null,
    val zonaNitratos         : Boolean? = null,
    val sistemaAsesoramiento : String? = null,

    // SIGPAC (referenciasigpac)
    val provincia           : String? = null,
    val terminoMunicipal    : String? = null,
    val codigoAgregado     : String? = null,
    val zona              : String? = null,
    val numeroPoligono     : String? = null,
    val numeroParcela     : String? = null,
    val numeroRecinto     : String? = null,
    val usoSigpac          : String? = null,
    val superficieHa        : Double? = null,

    // Agronómicos (datosagronomicos)
    val especieVariedad    : String? = null,
    val ecoregimenPractica: String? = null,
    val secanoRegadio    : String? = null,
    val cultivo          : String? = null,
    val fechaInicio      : String? = null,
    val fechaFin         : String? = null,
    val aireLibreProtegido: String? = null
)

// Cultivo

@Serializable
data class CultivoRequest(
    val especie  : String? = null,
    val variedad : String? = null
)

@Serializable
data class CultivoResponse(
    val id       : Int,
    val especie  : String? = null,
    val variedad : String? = null
)

// Producto catálogo

@Serializable
data class ProductoRequest(
    val nombreComercial : String? = null,
    val materiaActiva   : String? = null,
    val numeroRegistro  : String? = null
)

@Serializable
data class ProductoResponse(
    val id              : Int,
    val nombreComercial : String? = null,
    val materiaActiva   : String? = null,
    val numeroRegistro  : String? = null
)