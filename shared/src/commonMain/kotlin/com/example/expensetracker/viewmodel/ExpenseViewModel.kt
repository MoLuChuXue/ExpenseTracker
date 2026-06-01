package com.example.expensetracker.viewmodel

import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val dao: ExpenseDao,
    private val scope: CoroutineScope
) {
    val expenses: StateFlow<List<Expense>> = dao.getAllExpenses()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: StateFlow<Double> = dao.getTotalExpense()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = dao.getTotalIncome()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addExpense(amount: Double, category: String, note: String, dateMillis: Long, type: String = "expense") {
        scope.launch {
            dao.insert(
                Expense(
                    amount = amount,
                    category = category,
                    note = note,
                    dateMillis = dateMillis,
                    type = type
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        scope.launch {
            dao.update(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        scope.launch {
            dao.delete(expense)
        }
    }

    fun importData(expenses: List<Expense>) {
        scope.launch {
            dao.deleteAll()
            dao.insertAll(expenses)
        }
    }
}
