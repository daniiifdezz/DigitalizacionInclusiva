package org.dferna14.project

import android.app.Application
import org.dferna14.project.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Clase Application de Android.
 * Es el punto de entrada de la app — se ejecuta antes que cualquier Activity.
 * Aquí iniciamos Koin para que las dependencias estén disponibles en toda la app.
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Proporciona el contexto de Android a Koin
            // (necesario para DatabaseDriverFactory en Android)
            androidContext(this@MainApplication)

            // Carga nuestro módulo con todas las dependencias
            modules(appModule)
        }
    }
}