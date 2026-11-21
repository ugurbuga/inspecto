package com.ugurbuga.inspecto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

internal class InspectoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val controller = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
        //val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        //controller.isAppearanceLightStatusBars = !isDark
        setContent {
            InspectoScreen(
                onDismiss = { finish() }
            )
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, InspectoActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
