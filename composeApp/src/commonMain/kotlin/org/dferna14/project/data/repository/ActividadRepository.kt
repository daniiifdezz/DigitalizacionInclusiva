package org.dferna14.project.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dferna14.project.data.local.SessionStorage
import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.ActividadCreateDto
import org.dferna14.project.data.remote.ConflictException
import org.dferna14.project.data.remote.ActividadDto
import org.dferna14.project.data.remote.ActividadProductoCreateDto
import org.dferna14.project.data.remote.DependenciasParcelaDto
import org.dferna14.project.data.remote.DependenciasProductoDto
import org.dferna14.project.data.remote.DatosAgronomicosCreateDto
import org.dferna14.project.data.remote.EstadoActividadDto
import org.dferna14.project.data.remote.LoginRequest
import org.dferna14.project.data.remote.ParcelaApi
import org.dferna14.project.data.remote.ParcelaCreateDto
import org.dferna14.project.data.remote.ProductoCreateDto
import org.dferna14.project.data.remote.ReferenciaSigpacCreateDto
import org.dferna14.project.data.remote.RegisterRequest
import org.dferna14.project.data.remote.SemillaTratadaCreateDto
import org.dferna14.project.data.remote.UsuarioDto
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.ActividadProducto
import org.dferna14.project.domain.model.Cultivo
import org.dferna14.project.domain.model.DatosAgronomicos
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.ParcelaCompleta
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.ReferenciaSigpac
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.SemillaTratada
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.data.remote.FertilizacionCreateDto
import org.dferna14.project.domain.model.Fertilizacion
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

/**
 * Repositorio offline-first para Actividades y Parcelas.
 *
 * Estrategia:
 * 1. Devuelve datos locales inmediatamente (sin esperar red)
 * 2. Intenta sincronizar con el backend en segundo plano
 * 3. Si no hay red, los datos se guardan localmente y se sincronizan después
 *
 * Por ahora trabaja directamente con la API hasta que implementemos SQLDelight.
 * La interfaz no cambiará cuando añadamos la caché local.
 */
class ActividadRepository(
    private val api: ActividadApi,
    private val parcelaApi: ParcelaApi,
    private val sessionStorage: SessionStorage
) {

    // Actividades

    fun getActividades(): Flow<Result<List<Actividad>>> = flow {
        emit(Result.Loading)
        try {
            val actividades = api.getActividades().map { it.toDomain() }
            emit(Result.Success(actividades))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar actividades: ${e.message}"))
        }
    }

    fun getActividadesPendientes(): Flow<Result<List<Actividad>>> = flow {
        emit(Result.Loading)
        try {
            val actividades = api.getActividadesPendientes().map { it.toDomain() }
            emit(Result.Success(actividades))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar actividades pendientes: ${e.message}"))
        }
    }

    suspend fun crearActividad(actividad: Actividad): Result<Actividad> {
        return try {
            val dto = api.crearActividad(
                ActividadCreateDto(
                    parcelaId             = actividad.parcelaId,
                    equipoId              = actividad.equipoId,
                    aplicadorId           = actividad.aplicadorId,
                    fechaInicio           = actividad.fechaInicio,
                    fechaFin              = actividad.fechaFin,
                    superficieTratada     = actividad.superficieTratada,
                    problemaFitosanitario = actividad.problemaFitosanitario,
                    eficacia              = actividad.eficacia,
                    observaciones         = actividad.observaciones,
                    estado                = EstadoActividadDto.BORRADOR
                )
            )
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear actividad: ${e.message}")
        }
    }

    suspend fun enviarActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.enviarActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al enviar actividad: ${e.message}")
        }
    }

    suspend fun validarActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.validarActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al validar actividad: ${e.message}")
        }
    }

    suspend fun devolverActividad(id: Int): Result<Unit> {
        return try {
            val exito = api.devolverActividad(id)
            if (exito) Result.Success(Unit)
            else Result.Error("No se pudo devolver la actividad")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al devolver actividad: ${e.message}")
        }
    }

    suspend fun eliminarActividad(id: Int): Result<Unit> {
        return try {
            val eliminado = api.eliminarActividad(id)
            if (eliminado) Result.Success(Unit)
            else Result.Error("No se pudo eliminar la actividad")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al eliminar actividad: ${e.message}")
        }
    }

    suspend fun getActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.getActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener actividad: ${e.message}")
        }
    }

    suspend fun actualizarActividad(actividad: Actividad): Result<Actividad> {
        return try {
            val dto = ActividadCreateDto(
                parcelaId             = actividad.parcelaId,
                equipoId              = actividad.equipoId,
                aplicadorId           = actividad.aplicadorId,
                fechaInicio           = actividad.fechaInicio,
                fechaFin              = actividad.fechaFin,
                superficieTratada     = actividad.superficieTratada,
                problemaFitosanitario = actividad.problemaFitosanitario,
                eficacia              = actividad.eficacia,
                observaciones         = actividad.observaciones,
                estado                = when (actividad.estado) {
                    EstadoActividad.BORRADOR -> EstadoActividadDto.BORRADOR
                    EstadoActividad.PENDIENTE_VALIDAR -> EstadoActividadDto.PENDIENTE_VALIDAR
                    EstadoActividad.VALIDADA -> EstadoActividadDto.VALIDADA
                }
            )
            val exito = api.actualizarActividad(actividad.id, dto)
            if (exito) Result.Success(actividad)
            else Result.Error("No se pudo actualizar la actividad")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al actualizar actividad: ${e.message}")
        }
    }

    // Parcelas

    fun getParcelas(): Flow<Result<List<Parcela>>> = flow {
        emit(Result.Loading)
        try {
            val parcelas = api.getParcelas().map { dto ->
                Parcela(
                    id                   = dto.id,
                    explotacionId        = dto.explotacionId,
                    orden                = dto.orden,
                    alias                = dto.alias,
                    sistemaAsesoramiento = dto.sistemaAsesoramiento,
                    zonaNitratos         = dto.zonaNitratos
                )
            }
            emit(Result.Success(parcelas))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar parcelas: ${e.message}"))
        }
    }

    // TODO(auth): cuando se implemente el login, obtener explotacionId
    // del usuario autenticado en lugar de cargarlo de la lista. La parcela
    // entrante debería traer ya el explotacionId del JWT y este método solo
    // validaría que coincide con el del usuario logueado.
    suspend fun crearParcela(parcela: Parcela): Result<Parcela> {
        return try {
            val dto = api.crearParcela(
                ParcelaCreateDto(
                    explotacionId        = parcela.explotacionId,
                    orden                = parcela.orden,
                    alias                = parcela.alias,
                    sistemaAsesoramiento = parcela.sistemaAsesoramiento,
                    zonaNitratos         = parcela.zonaNitratos
                )
            )
            Result.Success(
                Parcela(
                    id                   = dto.id,
                    explotacionId        = dto.explotacionId,
                    orden                = dto.orden,
                    alias                = dto.alias,
                    sistemaAsesoramiento = dto.sistemaAsesoramiento,
                    zonaNitratos         = dto.zonaNitratos
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear parcela: ${e.message}")
        }
    }

    suspend fun actualizarParcela(parcela: Parcela): Result<Parcela> {
        return try {
            val ok = api.actualizarParcela(
                id = parcela.id,
                parcela = ParcelaCreateDto(
                    explotacionId        = parcela.explotacionId,
                    orden                = parcela.orden,
                    alias                = parcela.alias,
                    sistemaAsesoramiento = parcela.sistemaAsesoramiento,
                    zonaNitratos         = parcela.zonaNitratos
                )
            )
            if (ok) Result.Success(parcela)
            else Result.Error("No se pudo actualizar la parcela")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al actualizar parcela: ${e.message}")
        }
    }

    suspend fun eliminarParcela(id: Int): Result<Unit> {
        return try {
            val exito = api.eliminarParcela(id)
            if (exito) Result.Success(Unit)
            else Result.Error("No se pudo eliminar la parcela")
        } catch (e: CancellationException) {
            throw e
        } catch (e: ConflictException) {
            Result.Error(
                "Esta parcela tiene actividades, semillas o datos SIGPAC asociados. " +
                    "Pide al técnico desde el escritorio que la elimine si es necesario."
            )
        } catch (e: Exception) {
            Result.Error("Error al eliminar parcela: ${e.message}")
        }
    }

    // Borrado en cascada de parcela Desktop
    suspend fun getDependenciasParcela(id: Int): Result<DependenciasParcelaDto> {
        return try {
            Result.Success(api.getDependenciasParcela(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al consultar dependencias de la parcela: ${e.message}")
        }
    }

    suspend fun eliminarParcelaEnCascada(id: Int): Result<Boolean> {
        return try {
            val ok = api.eliminarParcelaEnCascada(id)
            if (ok) Result.Success(true)
            else Result.Error("No se pudo eliminar la parcela y sus datos")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al eliminar la parcela: ${e.message}")
        }
    }

    // Productos

    fun getProductos(): Flow<Result<List<Producto>>> = flow {
        emit(Result.Loading)
        try {
            val productos = api.getProductos().map { it.toDomain() }
            emit(Result.Success(productos))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar productos: ${e.message}"))
        }
    }

    suspend fun getFitosanitarios(): Result<List<Producto>> {
        return try {
            Result.Success(api.getProductosPorTipo("FITOSANITARIO").map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al cargar fitosanitarios: ${e.message}")
        }
    }

    suspend fun getFertilizantes(): Result<List<Producto>> {
        return try {
            Result.Success(api.getProductosPorTipo("FERTILIZANTE").map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al cargar fertilizantes: ${e.message}")
        }
    }

    suspend fun crearProducto(producto: Producto): Result<Producto> {
        return try {
            val dto = api.crearProducto(
                ProductoCreateDto(
                    nombreComercial  = producto.nombreComercial,
                    materiaActiva    = producto.materiaActiva,
                    numeroRegistro   = producto.numeroRegistro,
                    tipo             = producto.tipo,
                    riquezaNpk       = producto.riquezaNpk,
                    tipoFertilizante = producto.tipoFertilizante
                )
            )
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear producto: ${e.message}")
        }
    }

    private fun org.dferna14.project.data.remote.ProductoDto.toDomain() = Producto(
        id               = id,
        nombreComercial  = nombreComercial ?: "Sin nombre",
        materiaActiva    = materiaActiva,
        numeroRegistro   = numeroRegistro,
        tipo             = tipo,
        riquezaNpk       = riquezaNpk,
        tipoFertilizante = tipoFertilizante
    )

    suspend fun eliminarProducto(id: Int): Result<Unit> {
        return try {
            val exito = api.eliminarProducto(id)
            if (exito) Result.Success(Unit)
            else Result.Error("No se pudo eliminar el producto")
        } catch (e: CancellationException) {
            throw e
        } catch (e: ConflictException) {
            Result.Error(
                "Este producto está siendo usado en actividades. " +
                    "Pide al técnico desde el escritorio que lo elimine si es necesario."
            )
        } catch (e: Exception) {
            Result.Error("Error al eliminar producto: ${e.message}")
        }
    }

    suspend fun getDependenciasProducto(id: Int): Result<DependenciasProductoDto> {
        return try {
            Result.Success(api.getDependenciasProducto(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al consultar dependencias del producto: ${e.message}")
        }
    }

    suspend fun eliminarProductoEnCascada(id: Int): Result<Boolean> {
        return try {
            val ok = api.eliminarProductoEnCascada(id)
            if (ok) Result.Success(true)
            else Result.Error("No se pudo eliminar el producto y sus referencias")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al eliminar el producto en cascada: ${e.message}")
        }
    }

    // Productos aplicados a una actividad

    suspend fun getActividadProductos(actividadId: Int): Result<List<ActividadProducto>> {
        return try {
            val productos = api.getActividadProductos(actividadId).map { it.toDomain() }
            Result.Success(productos)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al cargar productos de actividad: ${e.message}")
        }
    }

    suspend fun crearActividadProducto(
        actividadId: Int,
        productoId: Int,
        dosis: Double
    ): Result<ActividadProducto> {
        return try {
            val dto = api.crearActividadProducto(
                actividadId,
                ActividadProductoCreateDto(productoId = productoId, dosis = dosis)
            )
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al añadir producto: ${e.message}")
        }
    }

    private fun org.dferna14.project.data.remote.ActividadProductoDto.toDomain() = ActividadProducto(
        id                      = id,
        actividadId             = actividadId,
        productoId              = productoId,
        dosis                   = dosis,
        productoNombreComercial = productoNombreComercial,
        productoNumeroRegistro  = productoNumeroRegistro,
        productoMateriaActiva   = productoMateriaActiva
    )

    suspend fun eliminarActividadProducto(
        actividadId: Int,
        actividadProductoId: Int
    ): Result<Unit> {
        return try {
            val ok = api.eliminarActividadProducto(actividadId, actividadProductoId)
            if (ok) Result.Success(Unit)
            else Result.Error("No se pudo eliminar el producto")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al eliminar producto: ${e.message}")
        }
    }

    private fun ActividadDto.toDomain() = Actividad(
        id                    = id,
        parcelaId             = parcelaId,
        parcelaAlias          = parcelaAlias,
        equipoId              = equipoId,
        aplicadorId           = aplicadorId,
        fechaInicio           = fechaInicio,
        fechaFin              = fechaFin,
        superficieTratada     = superficieTratada,
        problemaFitosanitario = problemaFitosanitario,
        eficacia              = eficacia,
        observaciones         = observaciones,
        estado                = when (estado) {
            EstadoActividadDto.BORRADOR -> EstadoActividad.BORRADOR
            EstadoActividadDto.PENDIENTE_VALIDAR -> EstadoActividad.PENDIENTE_VALIDAR
            EstadoActividadDto.VALIDADA -> EstadoActividad.VALIDADA
        },
        sincronizado          = true
    )

    // Semillas tratadas

    suspend fun getSemillaTratada(actividadId: Int): Result<SemillaTratada?> {
        return try {
            val dto = api.getSemillaTratada(actividadId)
            if (dto == null) {
                // No hay registro: Devolver Success(null) para que la UI muestre el formulario vacío
                println("INFO: No hay semilla tratada para actividad $actividadId")
                Result.Success(null)
            } else {
                Result.Success(
                    SemillaTratada(
                        id                = dto.id,
                        actividadId       = dto.actividadId,
                        parcelaId         = dto.parcelaId,
                        aplica            = dto.aplica,
                        fechaSiembra      = dto.fechaSiembra,
                        superficieHa      = dto.superficieHa,
                        cantidadSemillaKg = dto.cantidadSemillaKg,
                        productoId        = dto.productoId,
                        variedadSemilla   = dto.variedadSemilla,
                        cultivoId         = dto.cultivoId
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener semilla tratada: ${e.message ?: "Error desconocido"}")
        }
    }

    suspend fun crearSemillaTratada(semilla: SemillaTratada): Result<SemillaTratada> {
        return try {
            println("DEBUG REPO: Enviando SemillaTratada al servidor: $semilla")
            val dto = api.crearSemillaTratada(
                SemillaTratadaCreateDto(
                    actividadId       = semilla.actividadId,
                    parcelaId         = semilla.parcelaId,
                    aplica            = semilla.aplica,
                    fechaSiembra      = semilla.fechaSiembra,
                    superficieHa      = semilla.superficieHa,
                    cantidadSemillaKg = semilla.cantidadSemillaKg,
                    productoId        = semilla.productoId,
                    variedadSemilla   = semilla.variedadSemilla,
                    cultivoId         = semilla.cultivoId
                )
            )
            println("DEBUG REPO: Respuesta SemillaTratada recibida: $dto")
            Result.Success(
                SemillaTratada(
                    id                = dto.id,
                    actividadId       = dto.actividadId,
                    parcelaId         = dto.parcelaId,
                    aplica            = dto.aplica,
                    fechaSiembra      = dto.fechaSiembra,
                    superficieHa      = dto.superficieHa,
                    cantidadSemillaKg = dto.cantidadSemillaKg,
                    productoId        = dto.productoId,
                    variedadSemilla   = dto.variedadSemilla,
                    cultivoId         = dto.cultivoId
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear semilla tratada: ${e.message ?: "Error desconocido"}")
        }
    }



    // Fertilizacion functions
    suspend fun getFertilizacionPorActividad(actividadId: Int): Result<Fertilizacion?> {
        return try {
            val dto = api.getFertilizacionByActividad(actividadId)
            if (dto == null) Result.Success(null)
            else Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener fertilización: ${e.message}")
        }
    }

    suspend fun guardarFertilizacion(
        actividadId: Int,
        fertilizacion: Fertilizacion
    ): Result<Fertilizacion> {
        return try {
            val dto = api.upsertFertilizacionDeActividad(
                actividadId,
                FertilizacionCreateDto(
                    actividadId       = actividadId,
                    productoId        = fertilizacion.productoId,
                    cultivoId         = fertilizacion.cultivoId,
                    aplica            = fertilizacion.aplica,
                    fechaInicio       = fertilizacion.fechaInicio,
                    fechaFin          = fertilizacion.fechaFin,
                    tipoProducto      = fertilizacion.tipoProducto,
                    numeroAlbaran     = fertilizacion.numeroAlbaran,
                    riquezaNPK        = fertilizacion.riquezaNPK,
                    dosis             = fertilizacion.dosis,
                    tipoFertilizacion = fertilizacion.tipoFertilizacion,
                    observaciones     = fertilizacion.observaciones
                )
            )
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al guardar fertilización: ${e.message ?: "Error desconocido"}")
        }
    }

    private fun org.dferna14.project.data.remote.FertilizacionDto.toDomain() = Fertilizacion(
        id                = id,
        actividadId       = actividadId,
        productoId        = productoId,
        cultivoId         = cultivoId,
        aplica            = aplica,
        fechaInicio       = fechaInicio,
        fechaFin          = fechaFin,
        tipoProducto      = tipoProducto,
        numeroAlbaran     = numeroAlbaran,
        riquezaNPK        = riquezaNPK,
        dosis             = dosis,
        tipoFertilizacion = tipoFertilizacion,
        observaciones     = observaciones
    )

    // Equipos de aplicación

    suspend fun getEquipos(): Result<List<EquipoAplicacion>> {
        return try {
            Result.Success(api.getEquipos().map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al cargar equipos: ${e.message}")
        }
    }

    suspend fun crearEquipo(equipo: EquipoAplicacion): Result<EquipoAplicacion> {
        return try {
            val dto = api.crearEquipo(
                org.dferna14.project.data.remote.EquipoCreateDto(
                    explotacionId         = equipo.explotacionId,
                    tipo                  = equipo.tipo,
                    marca                 = equipo.marca,
                    modelo                = equipo.modelo,
                    numeroRoma            = equipo.numeroRoma,
                    anyoFabricacion       = equipo.anyoFabricacion,
                    fechaUltimaInspeccion = equipo.fechaUltimaInspeccion
                )
            )
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear equipo: ${e.message}")
        }
    }

    suspend fun eliminarEquipo(id: Int): Result<Unit> {
        return try {
            val ok = api.eliminarEquipo(id)
            if (ok) Result.Success(Unit)
            else Result.Error("No se pudo eliminar el equipo")
        } catch (e: CancellationException) {
            throw e
        } catch (e: ConflictException) {
            Result.Error(e.message ?: "El equipo está asignado a actividades y no se puede eliminar")
        } catch (e: Exception) {
            Result.Error("Error al eliminar equipo: ${e.message}")
        }
    }

    private fun org.dferna14.project.data.remote.EquipoDto.toDomain() = EquipoAplicacion(
        id                    = id,
        explotacionId         = explotacionId,
        tipo                  = tipo,
        marca                 = marca,
        modelo                = modelo,
        numeroRoma            = numeroRoma,
        anyoFabricacion       = anyoFabricacion,
        fechaUltimaInspeccion = fechaUltimaInspeccion
    )

    // Usuarios (para dropdown de aplicador)

    suspend fun getUsuarios(rol: String? = null): Result<List<Usuario>> {
        return try {
            Result.Success(api.getUsuarios(rol).map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al cargar usuarios: ${e.message}")
        }
    }

    suspend fun crearUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val dto = api.crearUsuario(
                org.dferna14.project.data.remote.UsuarioCreateDto(
                    nombre         = usuario.nombre,
                    apellidos      = usuario.apellidos,
                    email          = usuario.email,
                    rol            = usuario.rol,
                    explotacionId  = usuario.explotacionId,
                    tipoCarnetRopo = usuario.tipoCarnetRopo
                )
            )
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ConflictException) {
            Result.Error(e.message ?: "Ya existe un usuario con ese email")
        } catch (e: Exception) {
            Result.Error("Error al crear usuario: ${e.message}")
        }
    }

    suspend fun eliminarUsuario(id: Int): Result<Unit> {
        return try {
            val ok = api.eliminarUsuario(id)
            if (ok) Result.Success(Unit)
            else Result.Error("No se pudo eliminar el aplicador")
        } catch (e: CancellationException) {
            throw e
        } catch (e: ConflictException) {
            Result.Error(e.message ?: "El aplicador está asignado a actividades y no se puede eliminar")
        } catch (e: Exception) {
            Result.Error("Error al eliminar aplicador: ${e.message}")
        }
    }

    private fun UsuarioDto.toDomain() = Usuario(
        id             = id,
        nombre         = nombre,
        apellidos      = apellidos,
        email          = email,
        rol            = rol,
        explotacionId  = explotacionId,
        fechaAlta      = fechaAlta,
        tipoCarnetRopo = tipoCarnetRopo
    )

    // Parcela completa (parcela + sigpac + agronómicos) y sus operaciones

    /**
     * Devuelve la superficie SIGPAC (ha) de la parcela o null si no tiene
     * SIGPAC registrado. Lo usan los formularios para pre-rellenar el campo
     * de superficie tratada por defecto al elegir parcela.
     */
    suspend fun getSuperficieParcela(parcelaId: Int): Double? {
        return try {
            val resultado = getParcelaCompleta(parcelaId)
            if (resultado is Result.Success) {
                resultado.data?.referenciaSigpac?.superficieHa
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getParcelaCompleta(id: Int): Result<ParcelaCompleta?> {
        return try {
            val dto = parcelaApi.getParcelaCompleta(id)
            if (dto == null) {
                Result.Success(null)
            } else {
                Result.Success(
                    ParcelaCompleta(
                        parcela = Parcela(
                            id                   = dto.parcela.id,
                            explotacionId        = dto.parcela.explotacionId,
                            orden                = dto.parcela.orden,
                            alias                = dto.parcela.alias,
                            sistemaAsesoramiento = dto.parcela.sistemaAsesoramiento,
                            zonaNitratos         = dto.parcela.zonaNitratos
                        ),
                        referenciaSigpac = dto.referenciaSigpac?.let { s ->
                            ReferenciaSigpac(
                                id               = s.id,
                                parcelaId        = s.parcelaId,
                                provincia        = s.provincia,
                                terminoMunicipal = s.terminoMunicipal,
                                codigoAgregado   = s.codigoAgregado,
                                zona             = s.zona,
                                numeroPoligono   = s.numeroPoligono,
                                numeroParcela    = s.numeroParcela,
                                numeroRecinto    = s.numeroRecinto,
                                usoSigpac        = s.usoSigpac,
                                superficieHa     = s.superficieHa
                            )
                        },
                        datosAgronomicos = dto.datosAgronomicos?.let { a ->
                            DatosAgronomicos(
                                id                 = a.id,
                                parcelaId          = a.parcelaId,
                                especieVariedad    = a.especieVariedad,
                                ecoregimenPractica = a.ecoregimenPractica,
                                secanoRegadio      = a.secanoRegadio,
                                cultivoId          = a.cultivoId,
                                fechaInicio        = a.fechaInicio,
                                fechaFin           = a.fechaFin,
                                aireLibreProtegido = a.aireLibreProtegido
                            )
                        }
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al cargar parcela completa: ${e.message}")
        }
    }

    suspend fun guardarSigpac(
        parcelaId: Int,
        sigpac: ReferenciaSigpac,
        esActualizacion: Boolean
    ): Result<Boolean> {
        return try {
            val ok = parcelaApi.crearOActualizarSigpac(
                parcelaId = parcelaId,
                request = ReferenciaSigpacCreateDto(
                    provincia        = sigpac.provincia,
                    terminoMunicipal = sigpac.terminoMunicipal,
                    codigoAgregado   = sigpac.codigoAgregado,
                    zona             = sigpac.zona,
                    numeroPoligono   = sigpac.numeroPoligono,
                    numeroParcela    = sigpac.numeroParcela,
                    numeroRecinto    = sigpac.numeroRecinto,
                    usoSigpac        = sigpac.usoSigpac,
                    superficieHa     = sigpac.superficieHa
                ),
                esActualizacion = esActualizacion
            )
            if (ok) Result.Success(true)
            else Result.Error("El backend rechazó la operación SIGPAC")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al guardar SIGPAC: ${e.message}")
        }
    }

    suspend fun guardarAgronomico(
        parcelaId: Int,
        agronomico: DatosAgronomicos,
        esActualizacion: Boolean
    ): Result<Boolean> {
        return try {
            val ok = parcelaApi.crearOActualizarAgronomico(
                parcelaId = parcelaId,
                request = DatosAgronomicosCreateDto(
                    especieVariedad    = agronomico.especieVariedad,
                    ecoregimenPractica = agronomico.ecoregimenPractica,
                    secanoRegadio      = agronomico.secanoRegadio,
                    cultivoId          = agronomico.cultivoId,
                    fechaInicio        = agronomico.fechaInicio,
                    fechaFin           = agronomico.fechaFin,
                    aireLibreProtegido = agronomico.aireLibreProtegido
                ),
                esActualizacion = esActualizacion
            )
            if (ok) Result.Success(true)
            else Result.Error("El backend rechazó la operación agronómicos")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al guardar datos agronómicos: ${e.message}")
        }
    }

    // Autenticación — flujos asíncronos para Login y Registro.
    // Devolvemos UsuarioDto tal cual para mantener este sprint aislado del
    // resto del dominio (todavía no persistimos sesión).

    fun login(request: LoginRequest): Flow<Result<UsuarioDto>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.login(request)
            sessionStorage.guardarSesion(
                token  = resp.token,
                userId = resp.usuario.id,
                email  = resp.usuario.email,
                rol    = resp.usuario.rol
            )
            emit(Result.Success(resp.usuario))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Error al iniciar sesión"))
        }
    }

    fun register(request: RegisterRequest): Flow<Result<UsuarioDto>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.register(request)
            sessionStorage.guardarSesion(
                token  = resp.token,
                userId = resp.usuario.id,
                email  = resp.usuario.email,
                rol    = resp.usuario.rol
            )
            emit(Result.Success(resp.usuario))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Error al registrar usuario"))
        }
    }

    /**
     * Valida la sesión persistida llamando a GET /me con el JWT guardado.
     * Lo usa AuthVm al arrancar para decidir si restaurar sesión o ir a login.
     */
    suspend fun getMe(): Result<UsuarioDto> {
        return try {
            Result.Success(api.getMe())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Sesión no válida: ${e.message}")
        }
    }

    suspend fun getCultivos(): Result<List<Cultivo>> {
        return try {
            val cultivos = parcelaApi.getCultivos().map { dto ->
                Cultivo(
                    id       = dto.id,
                    especie  = dto.especie,
                    variedad = dto.variedad
                )
            }
            Result.Success(cultivos)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al cargar cultivos: ${e.message}")
        }
    }

    suspend fun cambiarRolUsuario(usuarioId: Int, nuevoRol: String): Result<Unit> {
        return try {
            val response = api.cambiarRolUsuario(usuarioId, nuevoRol)
            when (response.status) {
                HttpStatusCode.OK -> Result.Success(Unit)
                HttpStatusCode.Forbidden -> {
                    val cuerpo = response.bodyAsText()
                    val mensaje = parsearMensajeError(cuerpo) ?: "No tienes permiso para cambiar el rol"
                    Result.Error(mensaje)
                }
                HttpStatusCode.NotFound -> Result.Error("Usuario no encontrado")
                HttpStatusCode.BadRequest -> {
                    val cuerpo = response.bodyAsText()
                    Result.Error(parsearMensajeError(cuerpo) ?: "Petición inválida")
                }
                else -> Result.Error("Error inesperado: ${response.status}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error de red: ${e.message}")
        }
    }
    private fun parsearMensajeError(jsonStr: String): String? {
        return try {
            val kotlinxJson = Json { ignoreUnknownKeys = true }
            val mapa = kotlinxJson.decodeFromString<Map<String, String>>(jsonStr)
            mapa["message"]
        } catch (e: Exception) {
            null
        }
    }
}