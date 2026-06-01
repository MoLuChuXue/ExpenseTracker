package com.example.expensetracker

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.expensetracker.data.ExpenseDatabase
import com.example.expensetracker.data.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseTrackerApp(
    database: ExpenseDatabase,
    settingsManager: SettingsManager
) {
    val context = LocalContext.current
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    val dateStr = remember {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri != null && json != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(json)
                }
                Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (json != null) {
                    pendingImportJson = json
                } else {
                    Toast.makeText(context, "读取文件失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    App(
        database = database,
        settingsManager = settingsManager,
        onExportData = { json ->
            pendingExportJson = json
            exportLauncher.launch("ExpenseTracker_${dateStr}.json")
        },
        onRequestImport = {
            importLauncher.launch(arrayOf("application/json", "*/*"))
        },
        pendingImportJson = pendingImportJson,
        onImportHandled = {
            pendingImportJson = null
            Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
        }
    )
}
