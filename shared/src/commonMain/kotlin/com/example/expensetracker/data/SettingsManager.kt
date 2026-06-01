package com.example.expensetracker.data

import com.russhwolf.settings.Settings

class SettingsManager {
    private val settings: Settings = getSettings()

    var budget: Long
        get() = settings.getLong("budget", 0L)
        set(value) = settings.putLong("budget", value)

    var balance: Long
        get() = settings.getLong("balance", 0L)
        set(value) = settings.putLong("balance", value)

    var themeIndex: Int
        get() = settings.getInt("theme_index", 0)
        set(value) = settings.putInt("theme_index", value)

    var lastV50Date: String
        get() = settings.getString("last_v50_date", "")
        set(value) = settings.putString("last_v50_date", value)

    var customExpenseCategories: String
        get() = settings.getString("custom_expense_categories", "")
        set(value) = settings.putString("custom_expense_categories", value)

    var customIncomeCategories: String
        get() = settings.getString("custom_income_categories", "")
        set(value) = settings.putString("custom_income_categories", value)

    var backupFolderUri: String
        get() = settings.getString("backup_folder_uri", "")
        set(value) = settings.putString("backup_folder_uri", value)

    var backupEnabled: Boolean
        get() = settings.getBoolean("backup_enabled", false)
        set(value) = settings.putBoolean("backup_enabled", value)

    var balanceHidden: Boolean
        get() = settings.getBoolean("balance_hidden", false)
        set(value) = settings.putBoolean("balance_hidden", value)
}

expect fun getSettings(): Settings
