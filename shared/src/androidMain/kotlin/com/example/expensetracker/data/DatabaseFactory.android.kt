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
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN type TEXT NOT NULL DEFAULT 'expense'")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE expenses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        dateMillis INTEGER NOT NULL,
                        type TEXT NOT NULL DEFAULT 'expense'
                    )
                """)
                db.execSQL("""
                    INSERT INTO expenses_new (id, amount, category, note, dateMillis, type)
                    SELECT id, CAST(amount * 100 AS INTEGER), category, note, dateMillis, type
                    FROM expenses
                """)
                db.execSQL("DROP TABLE expenses")
                db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
            }
        }
    }
}
