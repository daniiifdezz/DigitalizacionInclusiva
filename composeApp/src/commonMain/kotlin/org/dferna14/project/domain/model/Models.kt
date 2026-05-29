package org.dferna14.project.domain.model

enum class EstadoActividad {
    BORRADOR,
    PENDIENTE_VALIDAR,
    VALIDADA;

    fun esEditable(): Boolean = this != VALIDADA
    fun puedeEnviar(): Boolean = this == BORRADOR
    fun puedeValidar(): Boolean = this == PENDIENTE_VALIDAR
    fun puedeDevolver(): Boolean = this == PENDIENTE_VALIDAR
}

data class Parcela(
    val id                   : Int,
    val explotacionId        : Int?    = null,
    val orden                : Int?    = null,
    val alias                : String? = null,
    val sistemaAsesoramiento : String? = null,
    val zonaNitratos         : Boolean? = null
)

data class Producto(
    val id: Int,
    val nombreComercial: String,
    val materiaActiva: String? = null,
    val numeroRegistro: String? = null
)

data class Actividad(
    val id                   : Int           = 0,
    val parcelaId            : Int,
    val parcelaAlias         : String?  = null,
    val equipoId             : Int?     = null,
    val aplicadorId          : Int?     = null,
    val fechaInicio          : String,
    val fechaFin             : String?  = null,
    val superficieTratada    : Double?  = null,
    val problemaFitosanitario : String?  = null,
    val eficacia             : String?  = null,
    val observaciones        : String?  = null,
    val estado               : EstadoActividad = EstadoActividad.BORRADOR,
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
    val productoId        : Int?    = null,
    val variedadSemilla   : String? = null,
    val cultivoId         : Int?    = null
)

data class Fertilizacion(
    val id                 : Int     = 0,
    val actividadId        : Int?    = null,
    val cultivoId          : Int?    = null,
    val aplica             : Boolean = false,
    val fechaInicio        : String? = null,
    val fechaFin           : String? = null,
    val tipoProducto       : String? = null,
    val numeroAlbaran      : String? = null,
    val riquezaNPK         : String? = null,
    val dosis              : Double? = null,
    val tipoFertilizacion  : String? = null,
    val observaciones      : String? = null
)

data class Titular(
    val id           : Int     = 0,
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

data class Explotacion(
    val id                 : Int     = 0,
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

data class EquipoAplicacion(
    val id                    : Int     = 0,
    val explotacionId         : Int?    = null,
    val tipo                  : String,
    val marca                 : String? = null,
    val modelo                : String? = null,
    val numeroRoma            : String? = null,
    val anyoFabricacion       : Int?    = null,
    val fechaUltimaInspeccion : String? = null
)

data class Usuario(
    val id            : Int     = 0,
    val nombre        : String,
    val apellidos     : String? = null,
    val email         : String,
    val rol           : String  = "AGRICULTOR",
    val explotacionId : Int?    = null,
    val fechaAlta     : String? = null
)

data class Cultivo(
    val id       : Int     = 0,
    val especie  : String? = null,
    val variedad : String? = null
)

data class ReferenciaSigpac(
    val id               : Int     = 0,
    val parcelaId        : Int,
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

data class DatosAgronomicos(
    val id                 : Int     = 0,
    val parcelaId          : Int,
    val especieVariedad    : String? = null,
    val ecoregimenPractica : String? = null,
    val secanoRegadio      : String? = null,
    val cultivoId          : Int?    = null,
    val fechaInicio        : String? = null,
    val fechaFin           : String? = null,
    val aireLibreProtegido : String? = null
)

data class ParcelaCompleta(
    val parcela          : Parcela,
    val referenciaSigpac : ReferenciaSigpac? = null,
    val datosAgronomicos : DatosAgronomicos? = null
)

/**
 * Wrapper para manejo de estados de carga, exito y error, sin el uso de excepciones
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}