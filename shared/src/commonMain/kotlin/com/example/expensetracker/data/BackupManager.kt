package com.example.expensetracker.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val budget: Double,
    val balance: Double,
    val expenses: List<ExpenseBackup>
)

@Serializable
data class ExpenseBackup(
    val id: Long,
    val amount: Double,
    val category: String,
    val note: String,
    val dateMillis: Long,
    val type: String
)

private val backupJson = Json { prettyPrint = true }

fun exportToJson(data: BackupData): String = backupJson.encodeToString(data)

fun importFromJson(json: String): BackupData = backupJson.decodeFromString<BackupData>(json)

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
