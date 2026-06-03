package com.example.expensetracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.expensetracker.data.DatabaseFactory
import com.example.expensetracker.data.SettingsManager
import com.example.expensetracker.data.initAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initAndroidContext(applicationContext)

        val database = DatabaseFactory(applicationContext).create()
        val settingsManager = SettingsManager()

        applyFrameRate(settingsManager.frameRateMode)

        setContent {
            ExpenseTrackerApp(
                database = database,
                settingsManager = settingsManager,
                onApplyFrameRate = { mode -> applyFrameRate(mode) }
            )
        }
    }

    private fun applyFrameRate(mode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val modes = display?.supportedModes ?: return
        if (modes.isEmpty()) return

        val selected = if (mode <= 0) {
            modes.maxByOrNull { it.refreshRate }
        } else {
            modes.filter { it.refreshRate <= mode.toFloat() + 0.5f }
                .maxByOrNull { it.refreshRate }
                ?: modes.minByOrNull { it.refreshRate }
        }
        selected?.let { window.attributes.preferredDisplayModeId = it.modeId }
    }
}
