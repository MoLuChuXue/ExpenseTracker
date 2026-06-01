package com.example.expensetracker.data

import com.russhwolf.settings.Settings

class SettingsManager {
    private val settings: Settings = getSettings()

    var budget: Double
        get() = settings.getDouble("budget", 0.0)
        set(value) = settings.putDouble("budget", value)

    var balance: Double
        get() = settings.getDouble("balance", 0.0)
        set(value) = settings.putDouble("balance", value)

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
}

expect fun getSettings(): Settings
