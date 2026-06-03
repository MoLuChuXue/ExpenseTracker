package com.example.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.expensetracker.data.Expense
import com.example.expensetracker.util.formatDate
import com.example.expensetracker.util.toMoneyString

private val ItemShape = RoundedCornerShape(12.dp)
private val IconBgShape = RoundedCornerShape(10.dp)
private val DialogShape = RoundedCornerShape(20.dp)
private val DialogInnerShape = RoundedCornerShape(12.dp)
private val DialogIconShape = RoundedCornerShape(8.dp)
private val DialogBtnShape = RoundedCornerShape(10.dp)

@Composable
fun ExpenseItem(
    expense: Expense,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    categoryMap: Map<String, CategoryInfo> = emptyMap()
) {
    val category = remember(expense.category) {
        categoryMap[expense.category] ?: CategoryInfo(expense.category, Icons.Filled.MoreHoriz, Color(0xFF9E9E9E))
    }
    val isIncome = expense.type == "income"
    val formattedDate = remember(expense.dateMillis) { formatDate(expense.dateMillis, "MM/dd HH:mm") }
    val formattedAmount = remember(expense.amount) { expense.amount.toMoneyString() }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        shape = ItemShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(IconBgShape).background(category.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = expense.category,
                    modifier = Modifier.fillMaxSize().padding(9.dp),
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
            shape = DialogShape,
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
                        shape = DialogInnerShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(DialogIconShape).background(category.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(7.dp),
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
                    shape = DialogBtnShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("确认删除", modifier = Modifier.padding(horizontal = 12.dp))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    shape = DialogBtnShape
                ) {
                    Text("取消", modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        )
    }
}
