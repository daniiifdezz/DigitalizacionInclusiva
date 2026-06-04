package org.dferna14.project.data.repository

import kotlinx.coroutines.CancellationException
import org.dferna14.project.data.remote.CuadernoApi
import org.dferna14.project.domain.model.Result

/**
 * Repositorio del Cuaderno de Campo. Envuelve la descarga del PDF en el patrón
 * Result<T> del proyecto y relanza CancellationException para no tragarse la
 * cancelación de corrutinas.
 */
class CuadernoRepository(private val api: CuadernoApi) {

    suspend fun descargarPdf(desde: String, hasta: String): Result<ByteArray> {
        return try {
            val bytes = api.descargarPdf(desde, hasta)
            Result.Success(bytes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error descargando el PDF: ${e.message}")
        }
    }
}
