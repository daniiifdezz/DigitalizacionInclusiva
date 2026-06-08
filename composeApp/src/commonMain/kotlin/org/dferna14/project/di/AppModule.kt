package org.dferna14.project.di

import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.CuadernoApi
import org.dferna14.project.data.remote.ExplotacionApi
import org.dferna14.project.data.remote.ParcelaApi
import org.dferna14.project.data.remote.TitularApi
import org.dferna14.project.data.remote.createHttpClient
import org.dferna14.project.data.local.SessionStorage
import org.dferna14.project.data.local.crearSessionStorage
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.data.repository.CuadernoRepository
import org.dferna14.project.data.repository.ExplotacionRepository
import org.dferna14.project.data.repository.TitularRepository
import org.dferna14.project.ui.viewmodel.ActividadDetalleVm
import org.dferna14.project.ui.viewmodel.ActividadListaVm
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.dferna14.project.ui.viewmodel.AuthVm
import org.dferna14.project.ui.viewmodel.ConfiguracionVm
import org.dferna14.project.ui.viewmodel.CuadernoVm
import org.dferna14.project.ui.viewmodel.EquipoVm
import org.dferna14.project.ui.viewmodel.FertilizacionVm
import org.dferna14.project.ui.viewmodel.ParcelaVm
import org.dferna14.project.ui.viewmodel.ProductoVm
import org.dferna14.project.ui.viewmodel.SemillaVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.dferna14.project.util.OcrScanner
import org.dferna14.project.util.VoiceRecognizer
import org.dferna14.project.util.crearOcrScanner
import org.dferna14.project.util.crearVoiceRecognizer
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Módulo Koin — define cómo se crean e inyectan las dependencias.
 *
 * Orden de dependencias:
 * HttpClient → *Api → *Repository → ViewModels
 */
val appModule = module {

    // Almacenamiento de sesión (JWT persistido) — por plataforma
    single<SessionStorage> { crearSessionStorage() }

    // Reconocedor de voz — factory para que cada campo tenga su instancia independiente
    factory<VoiceRecognizer> { crearVoiceRecognizer() }

    // Escáner OCR — factory por disponibilidad de cámara por plataforma
    factory<OcrScanner> { crearOcrScanner() }

    // Cliente HTTP — singleton compartido (inyecta SessionStorage para el header Bearer)
    single { createHttpClient(get()) }

    // Fuentes de datos remotas (client + sessionStorage para URL dinámica)
    single { ActividadApi(get(), get()) }
    single { TitularApi(get(), get()) }
    single { ExplotacionApi(get(), get()) }
    single { ParcelaApi(get(), get()) }
    single { CuadernoApi(get(), get()) }

    // Repositorios
    single { ActividadRepository(get(), get(), get()) }
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
    factoryOf(::AjustesVm)
}
