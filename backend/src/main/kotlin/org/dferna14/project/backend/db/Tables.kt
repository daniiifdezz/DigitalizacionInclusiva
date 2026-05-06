package org.dferna14.project.backend.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

/**
 * Definición de tablas usando IntIdTable (Exposed).
 * IntIdTable genera automáticamente la columna 'id' como Int primary key,
 * lo que habilita insertAndGetId, findById, etc.
 *
 * Los nombres de tabla coinciden exactamente con tu esquema PostgreSQL.
 */

// ── Catálogos / entidades raíz (sin FKs hacia otras tablas) ───────────────────

object Titulares : IntIdTable("titular") {
    val nombre        = varchar("nombre", 100)
    val apellidos     = varchar("apellidos", 150).nullable()
    val nif           = varchar("nif", 20).uniqueIndex()
    val direccion     = varchar("direccion", 200).nullable()
    val telefono      = varchar("telefono", 20).nullable()
    val email         = varchar("email", 150).nullable()
}

object Productos : IntIdTable("producto") {
    val nombreComercial = varchar("nombre_comercial", 100).nullable()
    val materiaActiva   = varchar("materia_activa", 100).nullable()
    val numeroRegistro  = varchar("numero_registro", 50).nullable()
}

object Cultivos : IntIdTable("cultivo") {
    val especie  = varchar("especie", 100).nullable()
    val variedad = varchar("variedad", 100).nullable()
}

// ── Entidades con FK a las raíces ─────────────────────────────────────────────

object Explotaciones : IntIdTable("explotacion") {
    val nombre        = varchar("nombre", 150)
    val titularId     = integer("titular_id").references(Titulares.id).nullable()
    val direccion     = varchar("direccion", 200).nullable()
    val municipio     = varchar("municipio", 100).nullable()
    val provincia     = varchar("provincia", 100).nullable()
    val codigoPostal  = varchar("codigo_postal", 10).nullable()
    val nifEmpresa    = varchar("nif_empresa", 20).nullable()
}

object Parcelas : IntIdTable("parcela") {
    val explotacionId        = integer("explotacion_id").nullable()
    val orden                = integer("orden").nullable()
    val alias                = varchar("alias", 100).nullable()
    val sistemaAsesoramiento = varchar("sistema_asesoramiento", 50).nullable()
    val zonaNitratos         = bool("zona_nitratos").nullable()
}

object EquiposAplicacion : IntIdTable("equipoaplicacion") {
    val explotacionId          = integer("explotacion_id").references(Explotaciones.id).nullable()
    val tipo                   = varchar("tipo", 50)
    val marca                  = varchar("marca", 100).nullable()
    val modelo                 = varchar("modelo", 100).nullable()
    val numeroRoma             = varchar("numero_roma", 50).nullable()
    val anyoFabricacion        = integer("anyo_fabricacion").nullable()
    val fechaUltimaInspeccion  = date("fecha_ultima_inspeccion").nullable()
}

object Usuarios : IntIdTable("usuario") {
    val nombre         = varchar("nombre", 100)
    val apellidos      = varchar("apellidos", 150).nullable()
    val email          = varchar("email", 150).uniqueIndex()
    val passwordHash   = varchar("password_hash", 255).nullable()
    val rol            = varchar("rol", 20).default("AGRICULTOR")
    val explotacionId  = integer("explotacion_id").references(Explotaciones.id).nullable()
    val fechaAlta      = date("fecha_alta").nullable()
}

// ── Operativa de actividades ──────────────────────────────────────────────────

object Actividades : IntIdTable("actividad") {
    val parcelaId             = integer("parcela_id").references(Parcelas.id)
    val equipoId              = integer("equipo_id").references(EquiposAplicacion.id).nullable()
    val aplicadorId           = integer("aplicador_id").references(Usuarios.id).nullable()
    val fechaInicio           = date("fecha_inicio").nullable()
    val fechaFin              = date("fecha_fin").nullable()
    val superficieTratada     = double("superficie_tratada").nullable()
    val problemaFitosanitario = text("problema_fitosanitario").nullable()
    val eficacia              = varchar("eficacia", 50).nullable()
    val observaciones         = text("observaciones").nullable()
    val estado                = varchar("estado", 30).default("BORRADOR")
}

object ActividadProductos : IntIdTable("actividad_producto") {
    val actividadId = integer("actividad_id").references(Actividades.id)
    val productoId  = integer("producto_id").references(Productos.id)
    val dosis       = double("dosis")
}

object SemillasTratadas : IntIdTable("semillatratada") {
    val actividadId       = integer("actividad_id").references(Actividades.id)
    val parcelaId         = integer("parcela_id").references(Parcelas.id)
    val aplica            = bool("aplica").default(false)
    val fechaSiembra      = date("fecha_siembra").nullable()
    val superficieHa      = double("superficie_ha").nullable()
    val cantidadSemillaKg = double("cantidad_semilla_kg").nullable()
    val productoId        = integer("producto_id").references(Productos.id).nullable()
    val variedadSemilla   = varchar("variedad_semilla", 100).nullable()
    val cultivoId         = integer("cultivo_id").references(Cultivos.id).nullable()
}

object Fertilizaciones : IntIdTable("fertilizacion") {
    val cultivoId         = integer("cultivo_id").references(Cultivos.id).nullable()
    val aplica            = bool("aplica").default(false)
    val fechaInicio       = date("fecha_inicio").nullable()
    val fechaFin          = date("fecha_fin").nullable()
    val tipoProducto      = varchar("tipo_producto", 10).nullable()
    val numeroAlbaran     = varchar("numero_albaran", 50).nullable()
    val riquezaNPK        = varchar("riqueza_npk", 50).nullable()
    val dosis             = double("dosis").nullable()
    val tipoFertilizacion = varchar("tipo_fertilizacion", 10).nullable()
    val observaciones     = text("observaciones").nullable()
}

/**
 * Tabla pivote N:M entre fertilización y parcela.
 * Una fertilización puede aplicarse a varias parcelas, y una parcela puede recibir
 * varias fertilizaciones a lo largo del tiempo.
 */
object FertilizacionParcelas : Table("fertilizacion_parcela") {
    val fertilizacionId = integer("fertilizacion_id").references(Fertilizaciones.id)
    val parcelaId       = integer("parcela_id").references(Parcelas.id)
    override val primaryKey = PrimaryKey(fertilizacionId, parcelaId, name = "PK_FertilizacionParcela")
}

// ── Datos asociados a parcela (SIGPAC, agronómicos, medioambientales) ─────────

object ReferenciaSigpac : IntIdTable("referenciasigpac") {
    val parcelaId         = integer("parcela_id").references(Parcelas.id)
    val provincia         = varchar("provincia", 100).nullable()
    val terminoMunicipal  = varchar("termino_municipal", 100).nullable()
    val codigoAgregado    = varchar("codigo_agregado", 50).nullable()
    val zona              = varchar("zona", 50).nullable()
    val numeroPoligono    = varchar("numero_poligono", 20).nullable()
    val numeroParcela     = varchar("numero_parcela", 20).nullable()
    val numeroRecinto     = varchar("numero_recinto", 20).nullable()
    val usoSigpac         = varchar("uso_sigpac", 20).nullable()
    val superficieHa      = double("superficie_ha").nullable()
}

object DatosAgronomicos : IntIdTable("datosagronomicos") {
    val parcelaId          = integer("parcela_id").references(Parcelas.id)
    val especieVariedad    = varchar("especie_variedad", 200).nullable()
    val ecoregimenPractica  = varchar("ecoregimen_practica", 50).nullable()
    val secanoRegadio      = varchar("secano_regadio", 20).nullable()
    val cultivoId          = integer("cultivo_id").references(Cultivos.id).nullable()
    val fechaInicio        = date("fecha_inicio").nullable()
    val fechaFin           = date("fecha_fin").nullable()
    val aireLibreProtegido = varchar("aire_libre_protegido", 50).nullable()
}

object DatosMedioambientales : IntIdTable("datosmedioambientales") {
    val parcelaId = integer("parcela_id").references(Parcelas.id)
}
