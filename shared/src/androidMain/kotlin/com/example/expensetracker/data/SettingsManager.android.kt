package com.example.expensetracker.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private var appContext: Context? = null

fun initAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun getSettings(): Settings {
    val ctx = appContext ?: throw IllegalStateException("initAndroidContext() must be called first")
    return SharedPreferencesSettings(
        ctx.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    )
}
