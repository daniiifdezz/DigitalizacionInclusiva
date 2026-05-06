package org.dferna14.project.data.repository

import kotlinx.coroutines.CancellationException
import org.dferna14.project.data.remote.ExplotacionApi
import org.dferna14.project.data.remote.ExplotacionCreateDto
import org.dferna14.project.data.remote.ExplotacionDto
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Result

class ExplotacionRepository(
    private val api: ExplotacionApi
) {

    suspend fun getExplotacion(): Result<Explotacion?> {
        return try {
            val dto = api.getExplotacion()
            Result.Success(dto?.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener explotación: ${e.message}")
        }
    }

    suspend fun crearExplotacion(explotacion: Explotacion): Result<Explotacion> {
        return try {
            val dto = api.crearExplotacion(explotacion.toCreateDto())
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear explotación: ${e.message}")
        }
    }

    suspend fun actualizarExplotacion(explotacion: Explotacion): Result<Explotacion> {
        return try {
            val ok = api.actualizarExplotacion(explotacion.id, explotacion.toCreateDto())
            if (ok) Result.Success(explotacion)
            else Result.Error("No se pudo actualizar la explotación")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al actualizar explotación: ${e.message}")
        }
    }

    private fun ExplotacionDto.toDomain() = Explotacion(
        id                 = id,
        nombre             = nombre,
        titularId          = titularId,
        nifEmpresa         = nifEmpresa,
        registroNacional   = registroNacional,
        registroAutonomico = registroAutonomico,
        direccion          = direccion,
        municipio          = municipio,
        provincia          = provincia,
        codigoPostal       = codigoPostal,
        telefonoFijo       = telefonoFijo,
        telefonoMovil      = telefonoMovil,
        email              = email
    )

    private fun Explotacion.toCreateDto() = ExplotacionCreateDto(
        nombre             = nombre,
        titularId          = titularId,
        nifEmpresa         = nifEmpresa,
        registroNacional   = registroNacional,
        registroAutonomico = registroAutonomico,
        direccion          = direccion,
        municipio          = municipio,
        provincia          = provincia,
        codigoPostal       = codigoPostal,
        telefonoFijo       = telefonoFijo,
        telefonoMovil      = telefonoMovil,
        email              = email
    )
}
