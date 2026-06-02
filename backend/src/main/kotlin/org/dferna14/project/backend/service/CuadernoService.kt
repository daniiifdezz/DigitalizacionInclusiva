package org.dferna14.project.backend.service

import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.DatosAgronomicos
import org.dferna14.project.backend.db.EquiposAplicacion
import org.dferna14.project.backend.db.Explotaciones
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.ReferenciaSigpac
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.db.Titulares
import org.dferna14.project.backend.db.Usuarios
import org.dferna14.project.backend.mapper.toActividadProductoResponse
import org.dferna14.project.backend.mapper.toActividadResponse
import org.dferna14.project.backend.mapper.toDatosAgronomicosResponse
import org.dferna14.project.backend.mapper.toEquipoResponse
import org.dferna14.project.backend.mapper.toExplotacionResponse
import org.dferna14.project.backend.mapper.toFertilizacionResponse
import org.dferna14.project.backend.mapper.toParcelaResponse
import org.dferna14.project.backend.mapper.toReferenciaSigpacResponse
import org.dferna14.project.backend.mapper.toSemillaTratadaResponse
import org.dferna14.project.backend.mapper.toTitularResponse
import org.dferna14.project.backend.mapper.toUsuarioResponse
import org.dferna14.project.backend.model.ActividadCompletaDto
import org.dferna14.project.backend.model.CuadernoCompletoDto
import org.dferna14.project.backend.model.EstadoActividad
import org.dferna14.project.backend.model.ParcelaCompletaDto
import org.dferna14.project.backend.model.PeriodoDto
import org.dferna14.project.backend.model.ResumenCuadernoDto
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

/**
 * Servicio que consolida todos los datos necesarios para generar el PDF del
 * Cuaderno de Campo Digital conforme al RD 1311/2012.
 *
 * Solo se incluyen actividades en estado VALIDADA dentro del periodo
 * [fechaInicio, fechaFin]. Las parcelas se devuelven todas (no se filtran
 * por periodo) porque el cuaderno oficial debe describir la explotación
 * tal y como existe en el momento de la generación.
 */
object CuadernoService {

    fun obtenerCuadernoCompleto(
        fechaInicio: LocalDate,
        fechaFin: LocalDate
    ): CuadernoCompletoDto = transaction {

        // 1. Titular (sistema monoexplotación: el primer registro)
        val titular = Titulares
            .selectAll()
            .firstOrNull()
            ?.toTitularResponse()

        // 2. Explotación (sistema monoexplotación: el primer registro)
        val explotacion = Explotaciones
            .selectAll()
            .firstOrNull()
            ?.toExplotacionResponse()

        // 3. Parcelas con SIGPAC y datos agronómicos
        val parcelas = construirListaParcelas()

        // 4. Actividades VALIDADAS del periodo con todos sus datos relacionados
        val actividades = construirListaActividades(fechaInicio, fechaFin)

        // 5. Resumen estadístico
        val resumen = construirResumen(parcelas, actividades)

        CuadernoCompletoDto(
            fechaGeneracion = LocalDate.now().toString(),
            periodo = PeriodoDto(
                fechaInicio = fechaInicio.toString(),
                fechaFin = fechaFin.toString()
            ),
            titular = titular,
            explotacion = explotacion,
            parcelas = parcelas,
            actividades = actividades,
            resumen = resumen
        )
    }

    /**
     * Construye la lista de parcelas con sus datos SIGPAC y agronómicos.
     * Estos datos no se filtran por periodo: el cuaderno debe mostrar todas
     * las parcelas existentes en el momento de la generación.
     */
    private fun construirListaParcelas(): List<ParcelaCompletaDto> {
        return Parcelas.selectAll().map { parcelaRow ->
            val parcelaId = parcelaRow[Parcelas.id].value

            val sigpac = ReferenciaSigpac
                .selectAll()
                .where { ReferenciaSigpac.parcelaId eq parcelaId }
                .firstOrNull()
                ?.toReferenciaSigpacResponse()

            val agronomicos = DatosAgronomicos
                .selectAll()
                .where { DatosAgronomicos.parcelaId eq parcelaId }
                .firstOrNull()
                ?.toDatosAgronomicosResponse()

            ParcelaCompletaDto(
                parcela = parcelaRow.toParcelaResponse(),
                referenciaSigpac = sigpac,
                datosAgronomicos = agronomicos
            )
        }
    }

    /**
     * Construye la lista de actividades validadas en el periodo, cada una con
     * sus productos aplicados, semilla, fertilización, equipo usado y aplicador.
     *
     * Se hace LEFT JOIN con Parcelas para que el mapper de actividad pueda
     * incluir el alias de la parcela en la respuesta.
     */
    private fun construirListaActividades(
        fechaInicio: LocalDate,
        fechaFin: LocalDate
    ): List<ActividadCompletaDto> {
        return (Actividades leftJoin Parcelas)
            .selectAll()
            .where {
                (Actividades.estado eq EstadoActividad.VALIDADA.name) and
                        (Actividades.fechaInicio greaterEq fechaInicio) and
                        (Actividades.fechaInicio lessEq fechaFin)
            }
            .orderBy(Actividades.fechaInicio to SortOrder.ASC)
            .map { actRow ->
                val actId = actRow[Actividades.id].value

                val productosAplicados = ActividadProductos
                    .selectAll()
                    .where { ActividadProductos.actividadId eq actId }
                    .map { it.toActividadProductoResponse() }

                val semilla = SemillasTratadas
                    .selectAll()
                    .where { SemillasTratadas.actividadId eq actId }
                    .firstOrNull()
                    ?.toSemillaTratadaResponse()

                val fertilizacion = Fertilizaciones
                    .selectAll()
                    .where { Fertilizaciones.actividadId eq actId }
                    .firstOrNull()
                    ?.toFertilizacionResponse()

                val equipo = actRow[Actividades.equipoId]?.let { equipoId ->
                    EquiposAplicacion
                        .selectAll()
                        .where { EquiposAplicacion.id eq equipoId }
                        .firstOrNull()
                        ?.toEquipoResponse()
                }

                val aplicador = actRow[Actividades.aplicadorId]?.let { aplicadorId ->
                    Usuarios
                        .selectAll()
                        .where { Usuarios.id eq aplicadorId }
                        .firstOrNull()
                        ?.toUsuarioResponse()
                }

                ActividadCompletaDto(
                    actividad = actRow.toActividadResponse(),
                    productosAplicados = productosAplicados,
                    semillaTratada = semilla,
                    fertilizacion = fertilizacion,
                    equipoUsado = equipo,
                    aplicador = aplicador
                )
            }
    }

    /**
     * Calcula el resumen estadístico del periodo. Como solo recopilamos
     * actividades VALIDADAS, total y totalValidadas coinciden — se exponen
     * por separado para que la firma del DTO siga siendo útil si en el
     * futuro se decide incluir también BORRADOR/PENDIENTE_VALIDAR.
     */
    private fun construirResumen(
        parcelas: List<ParcelaCompletaDto>,
        actividades: List<ActividadCompletaDto>
    ): ResumenCuadernoDto {
        return ResumenCuadernoDto(
            totalActividades = actividades.size,
            totalActividadesValidadas = actividades.size,
            totalParcelas = parcelas.size,
            superficieTotalTratada = actividades.sumOf {
                it.actividad.superficieTratada ?: 0.0
            },
            productosUnicosUsados = actividades
                .flatMap { it.productosAplicados }
                .map { it.productoId }
                .distinct()
                .size
        )
    }
}
