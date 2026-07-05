package org.dferna14.project.backend.mapper

import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.DatosAgronomicos
import org.dferna14.project.backend.db.EquiposAplicacion
import org.dferna14.project.backend.db.Explotaciones
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.db.Productos
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.ReferenciaSigpac
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.db.Titulares
import org.dferna14.project.backend.db.Usuarios
import org.dferna14.project.backend.model.ActividadProductoResponse
import org.dferna14.project.backend.model.ActividadResponse
import org.dferna14.project.backend.model.DatosAgronomicosResponse
import org.dferna14.project.backend.model.EquipoResponse
import org.dferna14.project.backend.model.EstadoActividad
import org.dferna14.project.backend.model.ExplotacionResponse
import org.dferna14.project.backend.model.FertilizacionResponse
import org.dferna14.project.backend.model.ParcelaResponse
import org.dferna14.project.backend.model.ReferenciaSigpacResponse
import org.dferna14.project.backend.model.SemillaTratadaResponse
import org.dferna14.project.backend.model.TitularResponse
import org.dferna14.project.backend.model.UsuarioResponse
import org.jetbrains.exposed.sql.ResultRow

/**
 * Mappers compartidos `ResultRow → *Response`.
 *
 * Vivían antes como funciones privadas en cada *Routes.kt; se centralizan aquí
 * para que CuadernoService (y futuros servicios) puedan reutilizarlos sin
 * duplicar código. Mantienen exactamente la misma semántica que los originales.
 */

fun ResultRow.toTitularResponse(): TitularResponse = TitularResponse(
    id           = this[Titulares.id].value,
    nombre       = this[Titulares.nombre],
    apellidos    = this[Titulares.apellidos],
    nif          = this[Titulares.nif],
    direccion    = this[Titulares.direccion],
    localidad    = this[Titulares.localidad],
    codigoPostal = this[Titulares.codigoPostal],
    provincia    = this[Titulares.provincia],
    telefono     = this[Titulares.telefono],
    email        = this[Titulares.email]
)

fun ResultRow.toExplotacionResponse(): ExplotacionResponse = ExplotacionResponse(
    id                 = this[Explotaciones.id].value,
    nombre             = this[Explotaciones.nombre],
    titularId          = this[Explotaciones.titularId],
    nifEmpresa         = this[Explotaciones.nifEmpresa],
    registroNacional   = this[Explotaciones.registroNacional],
    registroAutonomico = this[Explotaciones.registroAutonomico],
    direccion          = this[Explotaciones.direccion],
    municipio          = this[Explotaciones.municipio],
    provincia          = this[Explotaciones.provincia],
    codigoPostal       = this[Explotaciones.codigoPostal],
    telefonoFijo       = this[Explotaciones.telefonoFijo],
    telefonoMovil      = this[Explotaciones.telefonoMovil],
    email              = this[Explotaciones.email]
)

fun ResultRow.toParcelaResponse(): ParcelaResponse = ParcelaResponse(
    id                   = this[Parcelas.id].value,
    explotacionId        = this[Parcelas.explotacionId],
    orden                = this[Parcelas.orden],
    alias                = this[Parcelas.alias],
    sistemaAsesoramiento = this[Parcelas.sistemaAsesoramiento],
    zonaNitratos         = this[Parcelas.zonaNitratos]
)

fun ResultRow.toReferenciaSigpacResponse(): ReferenciaSigpacResponse = ReferenciaSigpacResponse(
    id               = this[ReferenciaSigpac.id].value,
    parcelaId        = this[ReferenciaSigpac.parcelaId],
    provincia        = this[ReferenciaSigpac.provincia],
    terminoMunicipal = this[ReferenciaSigpac.terminoMunicipal],
    codigoAgregado   = this[ReferenciaSigpac.codigoAgregado],
    zona             = this[ReferenciaSigpac.zona],
    numeroPoligono   = this[ReferenciaSigpac.numeroPoligono],
    numeroParcela    = this[ReferenciaSigpac.numeroParcela],
    numeroRecinto    = this[ReferenciaSigpac.numeroRecinto],
    usoSigpac        = this[ReferenciaSigpac.usoSigpac],
    superficieHa     = this[ReferenciaSigpac.superficieHa]
)

fun ResultRow.toDatosAgronomicosResponse(): DatosAgronomicosResponse = DatosAgronomicosResponse(
    id                 = this[DatosAgronomicos.id].value,
    parcelaId          = this[DatosAgronomicos.parcelaId],
    especieVariedad    = this[DatosAgronomicos.especieVariedad],
    ecoregimenPractica = this[DatosAgronomicos.ecoregimenPractica],
    secanoRegadio      = this[DatosAgronomicos.secanoRegadio],
    cultivoId          = this[DatosAgronomicos.cultivoId],
    fechaInicio        = this[DatosAgronomicos.fechaInicio]?.toString(),
    fechaFin           = this[DatosAgronomicos.fechaFin]?.toString(),
    aireLibreProtegido = this[DatosAgronomicos.aireLibreProtegido]
)

fun ResultRow.toActividadResponse(tipoActividad: String = "FITOSANITARIA"): ActividadResponse {
    // Si la fila viene de un JOIN con Parcelas leemos el alias; si no, queda null.
    val alias = runCatching { this[Parcelas.alias] }.getOrNull()
    return ActividadResponse(
        id                    = this[Actividades.id].value,
        parcelaId             = this[Actividades.parcelaId],
        parcelaAlias          = alias,
        equipoId              = this[Actividades.equipoId],
        aplicadorId           = this[Actividades.aplicadorId],
        fechaInicio           = this[Actividades.fechaInicio]?.toString() ?: "",
        fechaFin              = this[Actividades.fechaFin]?.toString(),
        superficieTratada     = this[Actividades.superficieTratada],
        problemaFitosanitario = this[Actividades.problemaFitosanitario],
        eficacia              = this[Actividades.eficacia],
        observaciones         = this[Actividades.observaciones],
        estado                = runCatching {
            EstadoActividad.valueOf(this[Actividades.estado] ?: "BORRADOR")
        }.getOrDefault(EstadoActividad.BORRADOR),
        tipoActividad         = tipoActividad
    )
}

fun ResultRow.toActividadProductoResponse(): ActividadProductoResponse = ActividadProductoResponse(
    id          = this[ActividadProductos.id].value,
    actividadId = this[ActividadProductos.actividadId],
    productoId  = this[ActividadProductos.productoId],
    dosis       = this[ActividadProductos.dosis]
)

fun ResultRow.toSemillaTratadaResponse(): SemillaTratadaResponse = SemillaTratadaResponse(
    id                      = this[SemillasTratadas.id].value,
    actividadId             = this[SemillasTratadas.actividadId],
    parcelaId               = this[SemillasTratadas.parcelaId],
    aplica                  = this[SemillasTratadas.aplica],
    fechaSiembra            = this[SemillasTratadas.fechaSiembra]?.toString(),
    superficieHa            = this[SemillasTratadas.superficieHa],
    cantidadSemillaKg       = this[SemillasTratadas.cantidadSemillaKg],
    productoId              = this[SemillasTratadas.productoId],
    variedadSemilla         = this[SemillasTratadas.variedadSemilla],
    cultivoId               = this[SemillasTratadas.cultivoId],
    productoNombreComercial = runCatching { this[Productos.nombreComercial] }.getOrNull(),
    productoNumeroRegistro  = runCatching { this[Productos.numeroRegistro] }.getOrNull(),
    productoMateriaActiva   = runCatching { this[Productos.materiaActiva] }.getOrNull()
)

fun ResultRow.toFertilizacionResponse(): FertilizacionResponse = FertilizacionResponse(
    id                      = this[Fertilizaciones.id].value,
    actividadId             = this[Fertilizaciones.actividadId],
    productoId              = this[Fertilizaciones.productoId],
    cultivoId               = this[Fertilizaciones.cultivoId],
    aplica                  = this[Fertilizaciones.aplica],
    fechaInicio             = this[Fertilizaciones.fechaInicio]?.toString(),
    fechaFin                = this[Fertilizaciones.fechaFin]?.toString(),
    tipoProducto            = this[Fertilizaciones.tipoProducto],
    numeroAlbaran           = this[Fertilizaciones.numeroAlbaran],
    riquezaNpk              = this[Fertilizaciones.riquezaNpk],
    dosis                   = this[Fertilizaciones.dosis],
    tipoFertilizacion       = this[Fertilizaciones.tipoFertilizacion],
    observaciones           = this[Fertilizaciones.observaciones],
    productoNombreComercial = runCatching { this[Productos.nombreComercial] }.getOrNull()
)

fun ResultRow.toEquipoResponse(): EquipoResponse = EquipoResponse(
    id                    = this[EquiposAplicacion.id].value,
    explotacionId         = this[EquiposAplicacion.explotacionId],
    tipo                  = this[EquiposAplicacion.tipo],
    marca                 = this[EquiposAplicacion.marca],
    modelo                = this[EquiposAplicacion.modelo],
    numeroRoma            = this[EquiposAplicacion.numeroRoma],
    anyoFabricacion       = this[EquiposAplicacion.anyoFabricacion],
    fechaUltimaInspeccion = this[EquiposAplicacion.fechaUltimaInspeccion]?.toString()
)

fun ResultRow.toUsuarioResponse(): UsuarioResponse = UsuarioResponse(
    id             = this[Usuarios.id].value,
    nombre         = this[Usuarios.nombre],
    apellidos      = this[Usuarios.apellidos],
    email          = this[Usuarios.email],
    rol            = this[Usuarios.rol],
    explotacionId  = this[Usuarios.explotacionId],
    fechaAlta      = this[Usuarios.fechaAlta]?.toString(),
    tipoCarnetRopo = this[Usuarios.tipoCarnetRopo],
    numeroRopo     = this[Usuarios.numeroRopo]
)
