package com.example.expensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.util.*
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, category: String, note: String, dateMillis: Long, type: String) -> Unit,
    editExpense: Expense? = null,
    customExpenseJson: String = "",
    customIncomeJson: String = "",
    onSaveCustomCategories: ((String, String) -> Unit)? = null
) {
    val isEditing = editExpense != null

    var selectedType by remember {
        mutableStateOf(editExpense?.type ?: "expense")
    }
    val currentCustomJson = if (selectedType == "income") customIncomeJson else customExpenseJson
    val currentCategories = remember(selectedType, currentCustomJson) {
        getCategoriesByType(selectedType, currentCustomJson)
    }

    var showCategoryManage by remember { mutableStateOf(false) }

    var amountText by remember {
        mutableStateOf(
            if (isEditing) {
                val amt = editExpense!!.amount
                amt.toMoneyString()
            } else ""
        )
    }
    var selectedCategory by remember(selectedType) {
        val editCat = if (isEditing) editExpense?.category else null
        mutableStateOf(
            if (editCat != null && currentCategories.any { it.name == editCat }) editCat
            else currentCategories.firstOrNull()?.name ?: ""
        )
    }
    var note by remember { mutableStateOf(editExpense?.note ?: "") }
    var selectedDate by remember {
        mutableStateOf(editExpense?.dateMillis ?: Clock.System.now().toEpochMilliseconds())
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    val amountCents = if (amountText.isNotEmpty()) amountText.parseCents() else null
    val isValid = amountCents != null && amountCents > 0L && amountCents <= 9999999900L && selectedCategory.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (isEditing) "修改记录" else "记一笔",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Type toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isExpense = selectedType == "expense"
                    Surface(
                        modifier = Modifier.weight(1f).clickable {
                            selectedType = "expense"
                            selectedCategory = (getCategoriesByType("expense", customExpenseJson).firstOrNull()?.name ?: "")
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Text(
                            "支出",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isExpense) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isExpense) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val isInc = selectedType == "income"
                    Surface(
                        modifier = Modifier.weight(1f).clickable {
                            selectedType = "income"
                            selectedCategory = (getCategoriesByType("income", customIncomeJson).firstOrNull()?.name ?: "")
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isInc) Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Text(
                            "收入",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isInc) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isInc) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = newValue
                        }
                    },
                    placeholder = { Text("0.00", style = MaterialTheme.typography.headlineMedium) },
                    prefix = {
                        Text(
                            if (selectedType == "income") "+¥ " else "-¥ ",
                            fontWeight = FontWeight.Medium,
                            color = if (selectedType == "income") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.headlineMedium
                )

                // Category
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("分类", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showCategoryManage = true }) {
                            Text("管理", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentCategories.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { category ->
                                    val isSelected = selectedCategory == category.name
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { selectedCategory = category.name },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) category.color.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(category.icon, contentDescription = null, modifier = Modifier.size(22.dp),
                                                tint = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(category.name, style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }

                // Date
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("日期", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null,
                                modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                formatDate(selectedDate, "yyyy年MM月dd日"),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("添加备注…") },
                    singleLine = false,
                    maxLines = 3,
                    minLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountCents?.let { onConfirm(it, selectedCategory, note, selectedDate, selectedType) }
                },
                enabled = isValid,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("保存", modifier = Modifier.padding(horizontal = 8.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )

    // Category manage dialog
    if (showCategoryManage) {
        CategoryManageDialog(
            customExpenseJson = customExpenseJson,
            customIncomeJson = customIncomeJson,
            onSaveExpense = { json -> onSaveCustomCategories?.invoke(json, customIncomeJson) },
            onSaveIncome = { json -> onSaveCustomCategories?.invoke(customExpenseJson, json) },
            onDismiss = { showCategoryManage = false }
        )
    }

    // Date picker dialog (shown on top of the AlertDialog)
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
