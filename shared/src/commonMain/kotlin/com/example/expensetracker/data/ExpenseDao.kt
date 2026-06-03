package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int = 3000): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'expense'")
    fun getTotalExpense(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE type = 'income'")
    fun getTotalIncome(): Flow<Long>

    @Insert
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Insert
    suspend fun insertAll(expenses: List<Expense>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}
