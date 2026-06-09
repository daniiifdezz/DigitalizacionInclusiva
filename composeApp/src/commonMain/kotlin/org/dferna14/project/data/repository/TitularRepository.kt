package org.dferna14.project.data.repository

import kotlinx.coroutines.CancellationException
import org.dferna14.project.data.remote.TitularApi
import org.dferna14.project.data.remote.TitularCreateDto
import org.dferna14.project.data.remote.TitularDto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Titular

class TitularRepository(
    private val api: TitularApi
) {

    suspend fun getTitular(): Result<Titular?> {
        return try {
            val dto = api.getTitular()
            Result.Success(dto?.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener titular: ${e.message}")
        }
    }

    suspend fun crearTitular(titular: Titular): Result<Titular> {
        return try {
            val dto = api.crearTitular(titular.toCreateDto())
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear titular: ${e.message}")
        }
    }

    suspend fun actualizarTitular(titular: Titular): Result<Titular> {
        return try {
            val dto = api.actualizarTitular(titular.id, titular.toCreateDto())
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al actualizar titular: ${e.message}")
        }
    }

    private fun TitularDto.toDomain() = Titular(
        id           = id,
        nombre       = nombre,
        apellidos    = apellidos,
        nif          = nif,
        direccion    = direccion,
        localidad    = localidad,
        codigoPostal = codigoPostal,
        provincia    = provincia,
        telefono     = telefono,
        email        = email
    )

    private fun Titular.toCreateDto() = TitularCreateDto(
        nombre       = nombre,
        apellidos    = apellidos,
        nif          = nif,
        direccion    = direccion,
        localidad    = localidad,
        codigoPostal = codigoPostal,
        provincia    = provincia,
        telefono     = telefono,
        email        = email
    )
}
