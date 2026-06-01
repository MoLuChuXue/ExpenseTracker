package com.example.expensetracker

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
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
    var backupFolderUri by remember { mutableStateOf(settingsManager.backupFolderUri) }

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

    val backupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Persist permission across reboots
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            backupFolderUri = uri.toString()
            settingsManager.backupFolderUri = uri.toString()
            Toast.makeText(context, "自动备份已设置", Toast.LENGTH_SHORT).show()
        }
    }

    val writeBackup: (String) -> Unit = { json ->
        val uriStr = settingsManager.backupFolderUri
        if (uriStr.isNotEmpty()) {
            try {
                val folderUri = Uri.parse(uriStr)
                val folder = DocumentFile.fromTreeUri(context, folderUri)
                if (folder != null) {
                    val dateStr2 = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val file = folder.createFile("application/json", "ExpenseTracker_$dateStr2")
                    if (file != null) {
                        context.contentResolver.openOutputStream(file.uri)?.bufferedWriter()?.use {
                            it.write(json)
                        }
                        // Keep only latest 5 backups
                        val allFiles = folder.listFiles()
                            .filter { it.name != null && it.name!!.startsWith("ExpenseTracker_") && it.name!!.endsWith(".json") }
                            .sortedByDescending { it.name }
                        if (allFiles.size > 5) {
                            allFiles.drop(5).forEach { it.delete() }
                        }
                    }
                }
            } catch (_: Exception) {
                // Auto-backup fails silently
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
        },
        onAutoBackup = { json ->
            writeBackup(json)
        },
        onPickBackupFolder = {
            backupFolderLauncher.launch(null)
        }
    )
}
