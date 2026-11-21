package io.github.ugurbuga.inspecto

actual fun launchInspecto(context: Any?) {
    Inspecto.setOpen(true)
    Inspecto.triggerLaunchCallback()
}
