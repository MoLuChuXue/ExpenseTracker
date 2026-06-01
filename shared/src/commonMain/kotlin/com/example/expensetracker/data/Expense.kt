package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String = "",
    val dateMillis: Long = Clock.System.now().toEpochMilliseconds(),
    val type: String = "expense"
)
