package io.github.ugurbuga.inspecto

import android.content.Context

actual fun launchInspecto(context: Any?) {
    val ctx = (context as? Context) ?: InspectoContextHolder.applicationContext
    if (ctx != null) {
        InspectoActivity.start(ctx)
    } else {
        Inspecto.triggerLaunchCallback()
    }
}
