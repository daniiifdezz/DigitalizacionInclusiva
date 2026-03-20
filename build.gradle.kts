plugins {
    // Declarados aquí una sola vez para evitar cargas duplicadas en subproyectos
    alias(libs.plugins.androidApplication)   apply false
    alias(libs.plugins.androidLibrary)       apply false
    alias(libs.plugins.composeHotReload)     apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler)      apply false
    alias(libs.plugins.kotlinMultiplatform)  apply false

    // Nuevos
    alias(libs.plugins.kotlinJvm)            apply false
    alias(libs.plugins.kotlinSerialization)  apply false
    alias(libs.plugins.sqldelight)           apply false
}