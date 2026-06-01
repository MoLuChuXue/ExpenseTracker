package com.example.expensetracker.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): ExpenseDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ExpenseDatabase::class.java,
            "expense_database"
        ).addMigrations(MIGRATION_1_2)
            .build()
    }

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN type TEXT NOT NULL DEFAULT 'expense'")
            }
        }
    }
}
