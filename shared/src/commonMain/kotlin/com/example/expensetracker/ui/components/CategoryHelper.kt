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
    val color: Color,
    val isCustom: Boolean = false
)

@Serializable
data class CustomCategoryData(
    val name: String,
    val colorHex: String
)

val expenseCategories = listOf(
    CategoryInfo("餐饮", Icons.Filled.Fastfood, CategoryFood),
    CategoryInfo("交通", Icons.Filled.DirectionsBus, CategoryTransport),
    CategoryInfo("购物", Icons.Filled.ShoppingCart, CategoryShopping),
    CategoryInfo("娱乐", Icons.Filled.SportsEsports, CategoryEntertainment),
    CategoryInfo("居住", Icons.Filled.Home, CategoryHousing),
    CategoryInfo("水电", Icons.Filled.Build, CategoryUtilities),
    CategoryInfo("医疗", Icons.Filled.LocalHospital, CategoryHealthcare),
    CategoryInfo("教育", Icons.Filled.School, CategoryEducation),
    CategoryInfo("其他", Icons.Filled.MoreHoriz, CategoryOther)
)

val incomeCategories = listOf(
    CategoryInfo("工资", Icons.Filled.Work, Color(0xFF4CAF50)),
    CategoryInfo("奖金", Icons.Filled.Star, Color(0xFF8BC34A)),
    CategoryInfo("投资", Icons.Filled.TrendingUp, Color(0xFF009688)),
    CategoryInfo("生活费", Icons.Filled.Payments, Color(0xFF00BCD4)),
    CategoryInfo("兼职", Icons.Filled.Person, Color(0xFF00BCD4)),
    CategoryInfo("理财", Icons.Filled.AccountBalance, Color(0xFF3F51B5)),
    CategoryInfo("退款", Icons.Filled.Replay, Color(0xFF2196F3)),
    CategoryInfo("报销", Icons.Filled.Description, Color(0xFF607D8B)),
    CategoryInfo("其他收入", Icons.Filled.MoreHoriz, Color(0xFF9E9E9E))
)

private val customCategoryIcon = Icons.Filled.MoreHoriz
private val customColors = listOf(
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4),
    Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A),
    Color(0xFFFF9800), Color(0xFF795548), Color(0xFF607D8B)
)

fun getCustomCategoryColors() = customColors

private val categoryJson = Json { ignoreUnknownKeys = true }

fun parseCustomCategories(json: String): List<CategoryInfo> {
    if (json.isBlank()) return emptyList()
    return try {
        val data = categoryJson.decodeFromString<List<CustomCategoryData>>(json)
        data.map { d ->
            val color = try {
                Color(d.colorHex.toLong(16) or 0xFF000000.toLong())
            } catch (_: Exception) {
                Color(0xFF9E9E9E)
            }
            CategoryInfo(name = d.name, icon = customCategoryIcon, color = color, isCustom = true)
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun serializeCustomCategories(categories: List<CategoryInfo>): String {
    val data = categories.filter { it.isCustom }.map {
        CustomCategoryData(name = it.name, colorHex = it.color.value.toLong().toString(16).takeLast(6))
    }
    return categoryJson.encodeToString(kotlinx.serialization.builtins.ListSerializer(CustomCategoryData.serializer()), data)
}

val allCategories = expenseCategories + incomeCategories

fun getCategoryInfo(categoryName: String, customExpenseJson: String = "", customIncomeJson: String = ""): CategoryInfo {
    val all = allCategories + parseCustomCategories(customExpenseJson) + parseCustomCategories(customIncomeJson)
    return all.find { it.name == categoryName } ?: allCategories.last()
}

fun getCategoriesByType(type: String, customJson: String): List<CategoryInfo> {
    val builtIn = if (type == "income") incomeCategories else expenseCategories
    return builtIn + parseCustomCategories(customJson)
}
