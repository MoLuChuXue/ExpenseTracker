package com.example.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CategoryManageDialog(
    customExpenseJson: String,
    customIncomeJson: String,
    onSaveExpense: (String) -> Unit,
    onSaveIncome: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf("expense") }

    var expenseCategories by remember { mutableStateOf(loadCategories(customExpenseJson, getDefaultExpenseCategories())) }
    var incomeCategories by remember { mutableStateOf(loadCategories(customIncomeJson, getDefaultIncomeCategories())) }

    LaunchedEffect(customExpenseJson) {
        expenseCategories = loadCategories(customExpenseJson, getDefaultExpenseCategories())
    }
    LaunchedEffect(customIncomeJson) {
        incomeCategories = loadCategories(customIncomeJson, getDefaultIncomeCategories())
    }

    val currentCategories = if (selectedType == "income") incomeCategories else expenseCategories

    var showAddDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "管理用途",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "关闭")
                    }
                }

                // Type toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isExpense = selectedType == "expense"
                    Surface(
                        modifier = Modifier.weight(1f).clickable { selectedType = "expense" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Text(
                            "支出", modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isExpense) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isExpense) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val isInc = selectedType == "income"
                    Surface(
                        modifier = Modifier.weight(1f).clickable { selectedType = "income" },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isInc) Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Text(
                            "收入", modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isInc) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isInc) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(currentCategories.size) { index ->
                        val category = currentCategories[index]
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Reorder arrows
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = {
                                            val newList = moveCategoryUp(currentCategories, index)
                                            if (selectedType == "income") {
                                                incomeCategories = newList
                                                onSaveIncome(saveCategories(newList))
                                            } else {
                                                expenseCategories = newList
                                                onSaveExpense(saveCategories(newList))
                                            }
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(16.dp),
                                            tint = if (index > 0) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    }
                                    IconButton(
                                        onClick = {
                                            val newList = moveCategoryDown(currentCategories, index)
                                            if (selectedType == "income") {
                                                incomeCategories = newList
                                                onSaveIncome(saveCategories(newList))
                                            } else {
                                                expenseCategories = newList
                                                onSaveExpense(saveCategories(newList))
                                            }
                                        },
                                        enabled = index < currentCategories.size - 1,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp),
                                            tint = if (index < currentCategories.size - 1) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                    }
                                }

                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = category.color.copy(alpha = 0.12f)
                                ) {
                                    Icon(category.icon, null, modifier = Modifier.padding(8.dp).fillMaxSize(), tint = category.color)
                                }

                                Text(
                                    category.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        if (currentCategories.size <= 1) return@IconButton
                                        val newList = currentCategories.filter { it.name != category.name }
                                        if (selectedType == "income") {
                                            incomeCategories = newList
                                            onSaveIncome(saveCategories(newList))
                                        } else {
                                            expenseCategories = newList
                                            onSaveExpense(saveCategories(newList))
                                        }
                                    },
                                    enabled = currentCategories.size > 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(18.dp),
                                        tint = if (currentCategories.size > 1) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("添加用途", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }

        if (showAddDialog) {
            AddCategoryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, color ->
                    val newList = currentCategories + CategoryInfo(name = name, icon = Icons.Filled.MoreHoriz, color = color)
                    if (selectedType == "income") {
                        incomeCategories = newList
                        onSaveIncome(saveCategories(newList))
                    } else {
                        expenseCategories = newList
                        onSaveExpense(saveCategories(newList))
                    }
                    showAddDialog = false
                },
                existingNames = currentCategories.map { it.name }
            )
        }
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Color) -> Unit,
    existingNames: List<String>
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(customColorPalette[0]) }
    val nameError = name.isNotBlank() && existingNames.any { it == name.trim() }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("添加用途", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("用途名称") },
                    placeholder = { Text("例如：宠物") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("名称已存在") }} else null
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("选择颜色", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(customColorPalette) { color ->
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = color }
                            ) {
                                if (isSelected) {
                                    Text("✓", modifier = Modifier.align(Alignment.Center),
                                        color = Color.White, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), selectedColor) },
                enabled = name.isNotBlank() && !nameError,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("添加", modifier = Modifier.padding(horizontal = 8.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
