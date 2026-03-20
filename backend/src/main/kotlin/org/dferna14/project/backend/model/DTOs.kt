package org.dferna14.project.backend.model

import kotlinx.serialization.Serializable

/**
 * DTOs (Data Transfer Objects) — estructuras JSON que viajan entre la app y el backend.
 * Son distintos de las entidades de BD (Exposed) para mantener separación de capas.
 */

// ── Actividad ─────────────────────────────────────────────────────────────────

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
    val observaciones        : String?  = null
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
    val observaciones        : String?  = null
)

// ── Producto aplicado ─────────────────────────────────────────────────────────

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

// ── Semilla tratada ───────────────────────────────────────────────────────────

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

// ── Fertilización ─────────────────────────────────────────────────────────────

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

// ── Parcela (solo lectura desde la app) ───────────────────────────────────────

@Serializable
data class ParcelaResponse(
    val id                  : Int,
    val explotacionId       : Int,
    val orden               : Int?     = null,
    val sistemaAsesoramiento: String?  = null,
    val zonaNitratos        : Boolean? = null
)

// ── Producto (catálogo, solo lectura desde la app) ────────────────────────────

@Serializable
data class ProductoResponse(
    val id              : Int,
    val nombreComercial : String? = null,
    val materiaActiva   : String? = null,
    val numeroRegistro  : String? = null
)