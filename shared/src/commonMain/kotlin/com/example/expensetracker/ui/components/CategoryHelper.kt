package com.example.expensetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.expensetracker.ui.theme.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class CategoryInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Serializable
data class StoredCategory(
    val name: String,
    val colorHex: String,
    val iconName: String = ""
)

// Default built-in categories (used when nothing is stored yet)
private val defaultExpenseCategories = listOf(
    StoredCategory("餐饮", "FF6B35", "Fastfood"),
    StoredCategory("交通", "FF9800", "DirectionsBus"),
    StoredCategory("购物", "E91E63", "ShoppingCart"),
    StoredCategory("娱乐", "9C27B0", "SportsEsports"),
    StoredCategory("居住", "795548", "Home"),
    StoredCategory("水电", "607D8B", "Build"),
    StoredCategory("医疗", "F44336", "LocalHospital"),
    StoredCategory("教育", "2196F3", "School"),
    StoredCategory("其他", "9E9E9E", "MoreHoriz")
)

private val defaultIncomeCategories = listOf(
    StoredCategory("工资", "4CAF50", "Work"),
    StoredCategory("奖金", "8BC34A", "Star"),
    StoredCategory("投资", "009688", "TrendingUp"),
    StoredCategory("生活费", "00BCD4", "Payments"),
    StoredCategory("兼职", "00BCD4", "Person"),
    StoredCategory("理财", "3F51B5", "AccountBalance"),
    StoredCategory("退款", "2196F3", "Replay"),
    StoredCategory("报销", "607D8B", "Description"),
    StoredCategory("其他收入", "9E9E9E", "MoreHoriz")
)

private val iconNameMap: Map<String, ImageVector> = mapOf(
    "Fastfood" to Icons.Filled.Fastfood,
    "DirectionsBus" to Icons.Filled.DirectionsBus,
    "ShoppingCart" to Icons.Filled.ShoppingCart,
    "SportsEsports" to Icons.Filled.SportsEsports,
    "Home" to Icons.Filled.Home,
    "Build" to Icons.Filled.Build,
    "LocalHospital" to Icons.Filled.LocalHospital,
    "School" to Icons.Filled.School,
    "MoreHoriz" to Icons.Filled.MoreHoriz,
    "Work" to Icons.Filled.Work,
    "Star" to Icons.Filled.Star,
    "TrendingUp" to Icons.Filled.TrendingUp,
    "Payments" to Icons.Filled.Payments,
    "Person" to Icons.Filled.Person,
    "AccountBalance" to Icons.Filled.AccountBalance,
    "Replay" to Icons.Filled.Replay,
    "Description" to Icons.Filled.Description
)

val customColorPalette = listOf(
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4),
    Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A),
    Color(0xFFFF9800), Color(0xFF795548), Color(0xFF607D8B)
)

private val categoryJson = Json { ignoreUnknownKeys = true }

private fun resolveIcon(iconName: String): ImageVector =
    iconNameMap[iconName] ?: Icons.Filled.MoreHoriz

private fun storedToCategory(s: StoredCategory): CategoryInfo {
    val color = try {
        Color(s.colorHex.toLong(16) or 0xFF000000.toLong())
    } catch (_: Exception) {
        Color(0xFF9E9E9E)
    }
    return CategoryInfo(name = s.name, icon = resolveIcon(s.iconName), color = color)
}

private fun categoryToStored(c: CategoryInfo): StoredCategory {
    val red = (c.color.red * 255).toInt().coerceIn(0, 255)
    val green = (c.color.green * 255).toInt().coerceIn(0, 255)
    val blue = (c.color.blue * 255).toInt().coerceIn(0, 255)
    val hex = "${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}"
    val iconName = iconNameMap.entries.find { it.value == c.icon }?.key ?: ""
    return StoredCategory(name = c.name, colorHex = hex, iconName = iconName)
}

fun loadCategories(json: String, defaults: List<StoredCategory>): List<CategoryInfo> {
    if (json.isBlank()) return defaults.map { storedToCategory(it) }
    return try {
        val stored = categoryJson.decodeFromString<List<StoredCategory>>(json)
        stored.map { storedToCategory(it) }.ifEmpty { defaults.map { storedToCategory(it) } }
    } catch (_: Exception) {
        defaults.map { storedToCategory(it) }
    }
}

fun saveCategories(categories: List<CategoryInfo>): String {
    val stored = categories.map { categoryToStored(it) }
    return categoryJson.encodeToString(
        kotlinx.serialization.builtins.ListSerializer(StoredCategory.serializer()), stored
    )
}

fun getDefaultExpenseCategories() = defaultExpenseCategories
fun getDefaultIncomeCategories() = defaultIncomeCategories

fun getCategoriesByType(type: String, customJson: String): List<CategoryInfo> {
    val defaults = if (type == "income") defaultIncomeCategories else defaultExpenseCategories
    return loadCategories(customJson, defaults)
}

fun resolveCategoryInfo(name: String, expenseJson: String, incomeJson: String): CategoryInfo {
    val expenseDefaults = defaultExpenseCategories.map { storedToCategory(it) }
    val incomeDefaults = defaultIncomeCategories.map { storedToCategory(it) }
    val allExpense = loadCategories(expenseJson, defaultExpenseCategories)
    val allIncome = loadCategories(incomeJson, defaultIncomeCategories)
    val all = allExpense + allIncome + expenseDefaults + incomeDefaults
    return all.find { it.name == name } ?: CategoryInfo(name, Icons.Filled.MoreHoriz, Color(0xFF9E9E9E))
}

fun getCategoryInfo(categoryName: String, customExpenseJson: String = "", customIncomeJson: String = ""): CategoryInfo =
    resolveCategoryInfo(categoryName, customExpenseJson, customIncomeJson)

fun moveCategoryUp(list: List<CategoryInfo>, index: Int): List<CategoryInfo> {
    if (index <= 0) return list
    val mutable = list.toMutableList()
    val temp = mutable[index]
    mutable[index] = mutable[index - 1]
    mutable[index - 1] = temp
    return mutable
}

fun moveCategoryDown(list: List<CategoryInfo>, index: Int): List<CategoryInfo> {
    if (index >= list.size - 1) return list
    val mutable = list.toMutableList()
    val temp = mutable[index]
    mutable[index] = mutable[index + 1]
    mutable[index + 1] = temp
    return mutable
}
