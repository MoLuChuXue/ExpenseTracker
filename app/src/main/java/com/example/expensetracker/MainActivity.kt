package com.example.expensetracker

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

        setContent {
            ExpenseTrackerApp(database = database, settingsManager = settingsManager)
        }
    }
}
