package com.example.expensetracker.data

expect class DatabaseFactory {
    fun create(): ExpenseDatabase
}
