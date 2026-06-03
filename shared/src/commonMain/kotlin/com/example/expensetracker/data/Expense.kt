package com.example.expensetracker.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Immutable
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long,
    val category: String,
    val note: String = "",
    val dateMillis: Long = Clock.System.now().toEpochMilliseconds(),
    val type: String = "expense"
)
