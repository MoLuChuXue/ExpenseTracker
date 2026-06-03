package com.example.expensetracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.*
import com.example.expensetracker.ui.screens.HomeScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.themePresets
import com.example.expensetracker.util.*
import com.example.expensetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun App(
    database: ExpenseDatabase,
    settingsManager: SettingsManager,
    onExportData: ((String) -> Unit)? = null,
    onRequestImport: (() -> Unit)? = null,
    pendingImportJson: String? = null,
    onImportHandled: (() -> Unit)? = null,
    onAutoBackup: ((String) -> Unit)? = null,
    onPickBackupFolder: (() -> Unit)? = null,
    onApplyFrameRate: ((Int) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val dataChangeSignal = remember { mutableIntStateOf(0) }
    val viewModel = remember {
        ExpenseViewModel(database.expenseDao(), scope) {
            dataChangeSignal.intValue++
        }
    }

    val expenses by viewModel.expenses.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()

    var themeIndex by remember { mutableIntStateOf(settingsManager.themeIndex) }
    var budget by remember { mutableStateOf(settingsManager.budget) }
    var balance by remember { mutableStateOf(settingsManager.balance) }
    var customExpenseJson by remember { mutableStateOf(settingsManager.customExpenseCategories) }
    var customIncomeJson by remember { mutableStateOf(settingsManager.customIncomeCategories) }
    var backupFolderUri by remember { mutableStateOf(settingsManager.backupFolderUri) }
    var backupEnabled by remember { mutableStateOf(settingsManager.backupEnabled) }
    var balanceHidden by remember { mutableStateOf(settingsManager.balanceHidden) }
    var frameRateMode by remember { mutableIntStateOf(settingsManager.frameRateMode) }

    // Auto-backup on data changes
    LaunchedEffect(dataChangeSignal.intValue) {
        if (dataChangeSignal.intValue > 0 && onAutoBackup != null && backupEnabled) {
            val json = withContext(Dispatchers.Default) {
                exportToJson(
                    BackupData(
                        exportDate = formatDate(Clock.System.now().toEpochMilliseconds(), "yyyy/MM/dd HH:mm"),
                        budget = budget,
                        balance = balance,
                        expenses = allExpenses.map { it.toBackup() }
                    )
                )
            }
            onAutoBackup?.invoke(json)
        }
    }

    // V50 Thursday check
    val todayStr = remember {
        val now = Clock.System.now()
        formatDate(now.toEpochMilliseconds(), "yyyyMMdd")
    }
    val isThursday = remember {
        val now = Clock.System.now()
        now.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek == DayOfWeek.THURSDAY
    }
    var showV50 by remember {
        mutableStateOf(isThursday && settingsManager.lastV50Date != todayStr)
    }

    // Handle import
    LaunchedEffect(pendingImportJson) {
        val json = pendingImportJson ?: return@LaunchedEffect
        try {
            val data = importFromJson(json)
            val importedExpenses = data.expenses.map { it.toExpense() }
            viewModel.importData(importedExpenses)
            budget = data.budget
            balance = data.balance
            settingsManager.budget = data.budget
            settingsManager.balance = data.balance
        } catch (_: Exception) {
            // import failed silently; caller shows feedback
        }
        onImportHandled?.invoke()
    }

    val currentPreset = themePresets.getOrElse(themeIndex) { themePresets[0] }

    ExpenseTrackerTheme(themePreset = currentPreset) {
        HomeScreen(
            expenses = expenses,
            allExpenses = allExpenses,
            budget = budget,
            balance = balance,
            themeIndex = themeIndex,
            customExpenseJson = customExpenseJson,
            customIncomeJson = customIncomeJson,
            onAddExpense = { amount, category, note, date, type ->
                viewModel.addExpense(amount, category, note, date, type)
            },
            onUpdateExpense = { expense ->
                viewModel.updateExpense(expense)
            },
            onDeleteExpense = { expense ->
                viewModel.deleteExpense(expense)
            },
            onSaveSettings = { newBudget, newBalance, newThemeIndex, newFrameRate ->
                budget = newBudget
                balance = newBalance
                themeIndex = newThemeIndex
                frameRateMode = newFrameRate
                settingsManager.budget = newBudget
                settingsManager.balance = newBalance
                settingsManager.themeIndex = newThemeIndex
                settingsManager.frameRateMode = newFrameRate
                onApplyFrameRate?.invoke(newFrameRate)
            },
            onSaveCustomCategories = { expenseJson, incomeJson ->
                customExpenseJson = expenseJson
                customIncomeJson = incomeJson
                settingsManager.customExpenseCategories = expenseJson
                settingsManager.customIncomeCategories = incomeJson
            },
            onExport = {
                val now = Clock.System.now().toEpochMilliseconds()
                val data = BackupData(
                    exportDate = formatDate(now, "yyyy/MM/dd HH:mm"),
                    budget = budget,
                    balance = balance,
                    expenses = allExpenses.map { it.toBackup() }
                )
                onExportData?.invoke(exportToJson(data))
            },
            onImport = { onRequestImport?.invoke() },
            backupFolderUri = backupFolderUri,
            backupEnabled = backupEnabled,
            onPickBackupFolder = { onPickBackupFolder?.invoke() },
            onToggleBackup = { enabled ->
                backupEnabled = enabled
                settingsManager.backupEnabled = enabled
            },
            frameRateMode = frameRateMode,
            balanceHidden = balanceHidden,
            onToggleBalanceHidden = {
                balanceHidden = !balanceHidden
                settingsManager.balanceHidden = balanceHidden
            }
        )

        if (showV50) {
            V50Dialog(
                onDismiss = {
                    settingsManager.lastV50Date = todayStr
                    showV50 = false
                }
            )
        }
    }
}

@Composable
private fun V50Dialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🐱🍗",
                textAlign = TextAlign.Center,
                fontSize = 48.sp,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                "老大，星期四了，该v我50了喵",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = MaterialTheme.shapes.medium) {
                Text("好，这就v", modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    )
}
