package org.dferna14.project.di

import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.CuadernoApi
import org.dferna14.project.data.remote.ExplotacionApi
import org.dferna14.project.data.remote.ParcelaApi
import org.dferna14.project.data.remote.TitularApi
import org.dferna14.project.data.remote.createHttpClient
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.data.repository.CuadernoRepository
import org.dferna14.project.data.repository.ExplotacionRepository
import org.dferna14.project.data.repository.TitularRepository
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.AuthVm
import org.dferna14.project.ui.viewmodel.ConfiguracionVm
import org.dferna14.project.ui.viewmodel.CuadernoVm
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
 * HttpClient → *Api → *Repository → ViewModels
 */
val appModule = module {

    // Cliente HTTP — singleton compartido
    single { createHttpClient() }

    // Fuentes de datos remotas
    single { ActividadApi(get()) }
    single { TitularApi(get()) }
    single { ExplotacionApi(get()) }
    single { ParcelaApi(get()) }
    single { CuadernoApi(get()) }

    // Repositorios
    single { ActividadRepository(get(), get()) }
    factoryOf(::TitularRepository)
    factoryOf(::ExplotacionRepository)
    factoryOf(::CuadernoRepository)

    // ViewModels — cada pantalla recibe su propia instancia (factory)
    factoryOf(::ActividadListaVm)
    factoryOf(::ActividadDetalleVm)
    factoryOf(::ParcelaVm)
    factoryOf(::ProductoVm)
    factoryOf(::SemillaVm)
    factoryOf(::FertilizacionVm)
    factoryOf(::EquipoVm)
    factoryOf(::UsuarioVm)
    factoryOf(::ConfiguracionVm)
    factoryOf(::AuthVm)
    factoryOf(::CuadernoVm)
}
