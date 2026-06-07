package org.dferna14.project.util

import android.app.Activity

private lateinit var activityRef: Activity

fun registrarActivityParaCierre(activity: Activity) {
    activityRef = activity
}

actual fun cerrarApp() {
    if (::activityRef.isInitialized) {
        activityRef.finish()
    }
}
