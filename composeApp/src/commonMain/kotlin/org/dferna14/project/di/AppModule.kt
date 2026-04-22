package org.dferna14.project.di

import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.createHttpClient
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.ui.viewmodel.ActividadViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Módulo Koin — define cómo se crean e inyectan las dependencias.
 *
 * Orden de dependencias:
 * HttpClient → ActividadApi → ActividadRepository → ActividadViewModel
 */
val appModule = module {

    // Cliente HTTP — singleton compartido
    single { createHttpClient() }

    // Fuente de datos remota
    single { ActividadApi(get()) }

    // Repositorio
    single { ActividadRepository(get()) }

    // ViewModel - singleton para compartir estado entre pantallas
    singleOf(::ActividadViewModel)
}