package org.dferna14.project.di

import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.createHttpClient
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.EquipoVm
import org.dferna14.project.ui.viewmodel.FertilizacionVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.dferna14.project.ui.viewmodel.SemillaVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Módulo Koin — define cómo se crean e inyectan las dependencias.
 *
 * Orden de dependencias:
 * HttpClient → ActividadApi → ActividadRepository → ViewModels
 */
val appModule = module {

    // Cliente HTTP — singleton compartido
    single { createHttpClient() }

    // Fuente de datos remota
    single { ActividadApi(get()) }

    // Repositorio
    single { ActividadRepository(get()) }

    // ViewModels — cada pantalla recibe su propia instancia (factory)
    factoryOf(::ActividadListaVm)
    factoryOf(::ActividadDetalleVm)
    factoryOf(::ParcelaVm)
    factoryOf(::ProductoVm)
    factoryOf(::SemillaVm)
    factoryOf(::FertilizacionVm)
    factoryOf(::EquipoVm)
    factoryOf(::UsuarioVm)
}
