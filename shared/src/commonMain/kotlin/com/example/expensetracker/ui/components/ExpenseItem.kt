package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.util.formatDate
import com.example.expensetracker.util.toMoneyString

@Composable
fun ExpenseItem(
    expense: Expense,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    customExpenseJson: String = "",
    customIncomeJson: String = ""
) {
    val category = remember(expense.category, customExpenseJson, customIncomeJson) {
        getCategoryInfo(expense.category, customExpenseJson, customIncomeJson)
    }
    val isIncome = expense.type == "income"
    val formattedDate = remember(expense.dateMillis) { formatDate(expense.dateMillis, "MM/dd HH:mm") }
    val formattedAmount = remember(expense.amount) { expense.amount.toMoneyString() }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = category.color.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = expense.category,
                    modifier = Modifier.padding(9.dp).fillMaxSize(),
                    tint = category.color
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.note.isNotBlank()) {
                        Text(
                            text = expense.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Text(
                text = if (isIncome) "+¥$formattedAmount" else "-¥$formattedAmount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        val amountColor = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "确认删除",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = category.color.copy(alpha = 0.12f)
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.padding(7.dp).fillMaxSize(),
                                    tint = category.color
                                )
                            }
                            Column {
                                Text(
                                    expense.category,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                if (expense.note.isNotBlank()) {
                                    Text(
                                        expense.note,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = if (isIncome) "+¥$formattedAmount" else "-¥$formattedAmount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            )
                        }
                    }

                    Text(
                        "删除后无法恢复",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("确认删除", modifier = Modifier.padding(horizontal = 12.dp))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("取消", modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        )
    }
}
