package org.dferna14.project.backend.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Definición de tablas usando IntIdTable (Exposed).
 * IntIdTable genera automáticamente la columna 'id' como Int primary key,
 * lo que habilita insertAndGetId, findById, etc.
 *
 * Los nombres de tabla coinciden exactamente con tu esquema PostgreSQL.
 */

object Parcelas : IntIdTable("parcela") {
    val explotacionId        = integer("explotacion_id").nullable()
    val orden                = integer("orden").nullable()
    val alias                = varchar("alias", 100).nullable()
    val sistemaAsesoramiento = varchar("sistema_asesoramiento", 50).nullable()
    val zonaNitratos         = bool("zona_nitratos").nullable()
}

object Productos : IntIdTable("producto") {
    val nombreComercial = varchar("nombre_comercial", 100).nullable()
    val materiaActiva   = varchar("materia_activa", 100).nullable()
    val numeroRegistro  = varchar("numero_registro", 50).nullable()
}

object Actividades : IntIdTable("actividad") {
    val parcelaId             = integer("parcela_id").references(Parcelas.id)
    val equipoId              = integer("equipo_id").nullable()
    val aplicadorId           = integer("aplicador_id").nullable()
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

object Cultivos : IntIdTable("cultivo") {
    val especie  = varchar("especie", 100).nullable()
    val variedad = varchar("variedad", 100).nullable()
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

//referencias sigPac

object ReferenciaSigpac : IntIdTable("referenciasigpac") {
    val parcelaId = integer("parcela_id").references(Parcelas.id)
    val provincia = varchar("provincia", 100).nullable()
    val terminoMunicipal = varchar("termino_municipal", 100).nullable()
    val codigoAgregado = varchar("codigo_agregado", 50).nullable()
    val zona = varchar("zona", 50).nullable()
    val numeroPoligono = varchar("numero_poligono", 20).nullable()
    val numeroParcela = varchar("numero_parcela", 20).nullable()
    val numeroRecinto = varchar("numero_recinto", 20).nullable()
    val usoSigpac = varchar("uso_sigpac", 20).nullable()
    val superficieHa = double("superficie_ha").nullable()
}
// Tabla: datosagronomicos
object DatosAgronomicos : IntIdTable("datosagronomicos") {
    val parcelaId = integer("parcela_id").references(Parcelas.id)
    val especieVariedad = varchar("especie_variedad", 200).nullable()
    val ecoregimenPractica = varchar("ecoregimen_practica", 50).nullable()
    val secanoRegadio = varchar("secano_regadio", 20).nullable()
    val cultivo = varchar("cultivo", 100).nullable()
    val fechaInicio = date("fecha_inicio").nullable()
    val fechaFin = date("fecha_fin").nullable()
    val aireLibreProtegido = varchar("aire_libre_protegido", 50).nullable()
}
// Tabla: datosmedioambientales (pendiente estructura - la verificas tú)
object DatosMedioambientales : IntIdTable("datosmedioambientales") {
    val parcelaId = integer("parcela_id").references(Parcelas.id)


}