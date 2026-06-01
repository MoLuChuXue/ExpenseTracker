package com.example.expensetracker.data

import kotlin.math.roundToLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val version: Int = 2,
    val exportDate: String,
    val budget: Long,
    val balance: Long,
    val expenses: List<ExpenseBackup>
)

@Serializable
data class ExpenseBackup(
    val id: Long,
    val amount: Long,
    val category: String,
    val note: String,
    val dateMillis: Long,
    val type: String
)

// Legacy format (v1) for backward compatibility
@Serializable
private data class LegacyBackupData(
    val version: Int = 1,
    val exportDate: String,
    val budget: Double,
    val balance: Double,
    val expenses: List<LegacyExpenseBackup>
)

@Serializable
private data class LegacyExpenseBackup(
    val id: Long,
    val amount: Double,
    val category: String,
    val note: String,
    val dateMillis: Long,
    val type: String
)

private val backupJson = Json { prettyPrint = true }

fun exportToJson(data: BackupData): String = backupJson.encodeToString(data)

fun importFromJson(json: String): BackupData {
    return try {
        backupJson.decodeFromString<BackupData>(json)
    } catch (_: Exception) {
        val legacy = backupJson.decodeFromString<LegacyBackupData>(json)
        BackupData(
            version = 2,
            exportDate = legacy.exportDate,
            budget = (legacy.budget * 100).roundToLong(),
            balance = (legacy.balance * 100).roundToLong(),
            expenses = legacy.expenses.map { e ->
                ExpenseBackup(
                    id = e.id,
                    amount = (e.amount * 100).roundToLong(),
                    category = e.category,
                    note = e.note,
                    dateMillis = e.dateMillis,
                    type = e.type
                )
            }
        )
    }
}

fun Expense.toBackup() = ExpenseBackup(
    id = id,
    amount = amount,
    category = category,
    note = note,
    dateMillis = dateMillis,
    type = type
)

fun ExpenseBackup.toExpense() = Expense(
    id = 0, // let Room auto-generate new IDs to avoid conflicts
    amount = amount,
    category = category,
    note = note,
    dateMillis = dateMillis,
    type = type
)
