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
    val parcelaAlias         : String?  = null,
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
    val actividadId      : Int,
    val parcelaId        : Int,
    val aplica           : Boolean = false,
    val fechaSiembra     : String? = null,
    val superficieHa     : Double? = null,
    val cantidadSemillaKg: Double? = null,
    val productoId       : Int?    = null,
    val variedadSemilla  : String? = null,
    val cultivoId        : Int?    = null
)

@Serializable
data class SemillaTratadaResponse(
    val id               : Int,
    val actividadId      : Int,
    val parcelaId        : Int,
    val aplica           : Boolean,
    val fechaSiembra     : String? = null,
    val superficieHa     : Double? = null,
    val cantidadSemillaKg: Double? = null,
    val productoId       : Int?    = null,
    val variedadSemilla  : String? = null,
    val cultivoId        : Int?    = null
)

// Fertilización

@Serializable
data class FertilizacionRequest(
    val actividadId      : Int?    = null,
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
    val actividadId      : Int?    = null,
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

// Parcela Completa - agrega parcela + referenciasigpac + datosagronomicos en sub-objetos.
// Si referenciaSigpac o datosAgronomicos es null el cliente sabe que no existe aún
// y debe hacer POST. Si no es null, hace PUT.

@Serializable
data class ParcelaCompletaResponse(
    val parcela          : ParcelaResponse,
    val referenciaSigpac : ReferenciaSigpacResponse? = null,
    val datosAgronomicos : DatosAgronomicosResponse? = null
)

// SIGPAC y Datos Agronómicos (request bodies para POST/PUT bajo /api/parcelas/{id}/...)

@Serializable
data class ReferenciaSigpacRequest(
    val provincia        : String? = null,
    val terminoMunicipal : String? = null,
    val codigoAgregado   : String? = null,
    val zona             : String? = null,
    val numeroPoligono   : String? = null,
    val numeroParcela    : String? = null,
    val numeroRecinto    : String? = null,
    val usoSigpac        : String? = null,
    val superficieHa     : Double? = null
)

@Serializable
data class ReferenciaSigpacResponse(
    val id                : Int,
    val parcelaId         : Int,
    val provincia         : String? = null,
    val terminoMunicipal  : String? = null,
    val codigoAgregado    : String? = null,
    val zona              : String? = null,
    val numeroPoligono    : String? = null,
    val numeroParcela     : String? = null,
    val numeroRecinto     : String? = null,
    val usoSigpac         : String? = null,
    val superficieHa      : Double? = null
)

@Serializable
data class DatosAgronomicosRequest(
    val especieVariedad    : String? = null,
    val ecoregimenPractica : String? = null,
    val secanoRegadio      : String? = null,
    val cultivoId          : Int?    = null,
    val fechaInicio        : String? = null,
    val fechaFin           : String? = null,
    val aireLibreProtegido : String? = null
)

@Serializable
data class DatosAgronomicosResponse(
    val id                 : Int,
    val parcelaId          : Int,
    val especieVariedad    : String? = null,
    val ecoregimenPractica : String? = null,
    val secanoRegadio      : String? = null,
    val cultivoId          : Int?    = null,
    val fechaInicio        : String? = null,
    val fechaFin           : String? = null,
    val aireLibreProtegido : String? = null
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
    val numeroRegistro  : String? = null,
    val tipo            : String  = "FITOSANITARIO",
    val riquezaNpk      : String? = null,
    val tipoFertilizante: String? = null
)

@Serializable
data class ProductoResponse(
    val id              : Int,
    val nombreComercial : String? = null,
    val materiaActiva   : String? = null,
    val numeroRegistro  : String? = null,
    val tipo            : String  = "FITOSANITARIO",
    val riquezaNpk      : String? = null,
    val tipoFertilizante: String? = null
)

// Titular

@Serializable
data class TitularRequest(
    val nombre       : String,
    val apellidos    : String? = null,
    val nif          : String,
    val direccion    : String? = null,
    val localidad    : String? = null,
    val codigoPostal : String? = null,
    val provincia    : String? = null,
    val telefono     : String? = null,
    val email        : String? = null
)

@Serializable
data class TitularResponse(
    val id           : Int,
    val nombre       : String,
    val apellidos    : String? = null,
    val nif          : String,
    val direccion    : String? = null,
    val localidad    : String? = null,
    val codigoPostal : String? = null,
    val provincia    : String? = null,
    val telefono     : String? = null,
    val email        : String? = null
)

// Explotacion

@Serializable
data class ExplotacionRequest(
    val nombre             : String,
    val titularId          : Int?    = null,
    val nifEmpresa         : String? = null,
    val registroNacional   : String? = null,
    val registroAutonomico : String? = null,
    val direccion          : String? = null,
    val municipio          : String? = null,
    val provincia          : String? = null,
    val codigoPostal       : String? = null,
    val telefonoFijo       : String? = null,
    val telefonoMovil      : String? = null,
    val email              : String? = null
)

@Serializable
data class ExplotacionResponse(
    val id                 : Int,
    val nombre             : String,
    val titularId          : Int?    = null,
    val nifEmpresa         : String? = null,
    val registroNacional   : String? = null,
    val registroAutonomico : String? = null,
    val direccion          : String? = null,
    val municipio          : String? = null,
    val provincia          : String? = null,
    val codigoPostal       : String? = null,
    val telefonoFijo       : String? = null,
    val telefonoMovil      : String? = null,
    val email              : String? = null
)

// EquipoAplicacion

@Serializable
data class EquipoRequest(
    val explotacionId         : Int?    = null,
    val tipo                  : String,
    val marca                 : String? = null,
    val modelo                : String? = null,
    val numeroRoma            : String? = null,
    val anyoFabricacion       : Int?    = null,
    val fechaUltimaInspeccion : String? = null
)

@Serializable
data class EquipoResponse(
    val id                    : Int,
    val explotacionId         : Int?    = null,
    val tipo                  : String,
    val marca                 : String? = null,
    val modelo                : String? = null,
    val numeroRoma            : String? = null,
    val anyoFabricacion       : Int?    = null,
    val fechaUltimaInspeccion : String? = null
)

// Usuario (response-only por ahora; el sprint de auth añadirá login y POST)

@Serializable
data class UsuarioResponse(
    val id            : Int,
    val nombre        : String,
    val apellidos     : String? = null,
    val email         : String,
    val rol           : String,
    val explotacionId : Int?    = null,
    val fechaAlta     : String? = null
)

// Auth — registro y login

@Serializable
data class RegisterRequest(
    val email    : String,
    val password : String,
    val nombre   : String,
    val rol      : String? = null
)

@Serializable
data class LoginRequest(
    val email    : String,
    val password : String
)