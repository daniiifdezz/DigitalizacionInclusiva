package org.dferna14.project.util

import android.content.Context
import android.content.pm.PackageManager

class OcrScannerAndroid(private val context: Context) : OcrScanner {
    override val isAvailable: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}

private lateinit var appContextOcr: Context

fun registrarContextoOcr(context: Context) {
    appContextOcr = context.applicationContext
}

actual fun crearOcrScanner(): OcrScanner = OcrScannerAndroid(appContextOcr)
