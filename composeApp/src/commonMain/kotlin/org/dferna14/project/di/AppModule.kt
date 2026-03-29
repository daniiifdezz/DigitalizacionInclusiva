package org.dferna14.project.di

import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.createHttpClient
import org.dferna14.project.data.repository.ActividadRepository
import org.koin.dsl.module

/**
 * Módulo Koin — define cómo se crean e inyectan las dependencias.
 *
 * Orden de dependencias:
 * HttpClient → ActividadApi → ActividadRepository → ViewModel
 */
val appModule = module {

    // Cliente HTTP — singleton compartido
    single { createHttpClient() }

    // Fuente de datos remota
    single { ActividadApi(get()) }

    // Repositorio — combina datos locales y remotos
    single { ActividadRepository(get()) }
}