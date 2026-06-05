package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.dferna14.project.data.local.SessionStorage

/**
 * Fuente de datos remota del Cuaderno de Campo. Solo necesita descargar los bytes
 * del PDF oficial (RD 1311/2012) generado por el backend para un periodo.
 *
 * Sigue el mismo patrón que el resto de *Api: recibe el HttpClient por constructor
 * (inyectado por Koin) y usa la BASE_URL definida por plataforma.
 */
class CuadernoApi(private val client: HttpClient, private val sessionStorage: SessionStorage) {

    /**
     * Descarga los bytes del PDF del Cuaderno de Campo para el periodo indicado.
     * Lanza excepción si el backend devuelve error (la captura el repositorio).
     *
     * @param desde fecha inicio del periodo en formato ISO "YYYY-MM-DD".
     * @param hasta fecha fin del periodo en formato ISO "YYYY-MM-DD".
     */
    suspend fun descargarPdf(desde: String, hasta: String): ByteArray {
        val response = client.get("${baseUrl(sessionStorage)}/api/cuaderno/pdf") {
            parameter("desde", desde)
            parameter("hasta", hasta)
        }
        return response.readRawBytes()
    }
}
