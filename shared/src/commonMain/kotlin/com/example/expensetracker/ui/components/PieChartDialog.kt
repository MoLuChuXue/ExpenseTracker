package com.example.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensetracker.data.Expense
import com.example.expensetracker.util.*
import kotlinx.datetime.Clock

private enum class ChartPeriod { DAY, MONTH, YEAR }

data class PieSlice(
    val category: String,
    val amount: Long,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PieChartDialog(
    expenses: List<Expense>,
    onDismiss: () -> Unit,
    customExpenseJson: String = "",
    customIncomeJson: String = ""
) {
    val nowYear = remember { currentYear() }
    val nowMonth = remember { currentMonth() }
    val nowDay = remember { currentDay() }

    var period by remember { mutableStateOf(ChartPeriod.MONTH) }
    var year by remember { mutableIntStateOf(nowYear) }
    var month by remember { mutableIntStateOf(nowMonth) }
    var day by remember { mutableIntStateOf(nowDay) }

    val expenseOnly = remember(expenses) { expenses.filter { it.type == "expense" } }

    val filteredExpenses = remember(expenseOnly, period, year, month, day) {
        when (period) {
            ChartPeriod.DAY -> expenseOnly.filter { isSameDay(it.dateMillis, year, month, day) }
            ChartPeriod.MONTH -> expenseOnly.filter { isSameMonth(it.dateMillis, year, month) }
            ChartPeriod.YEAR -> expenseOnly.filter { isSameYear(it.dateMillis, year) }
        }
    }

    val slices = remember(filteredExpenses) {
        val grouped = filteredExpenses.groupBy { it.category }
        grouped.map { (cat, list) ->
            val info = getCategoryInfo(cat, customExpenseJson, customIncomeJson)
            PieSlice(cat, list.sumOf { it.amount }, info.color)
        }.sortedByDescending { it.amount }
    }

    val totalAmount = slices.sumOf { it.amount }
    val navMillis = remember { Clock.System.now().toEpochMilliseconds() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                    Text(
                        "支出分析",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Period selector
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val options = listOf(ChartPeriod.DAY to "日", ChartPeriod.MONTH to "月", ChartPeriod.YEAR to "年")
                    options.forEach { (p, label) ->
                        val selected = period == p
                        Surface(
                            modifier = Modifier.weight(1f).clickable { period = p },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                label,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Date navigation
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = {
                        val millis = startOfDay(year, month, if (period == ChartPeriod.DAY) day else 1)
                        val newMillis = when (period) {
                            ChartPeriod.DAY -> addDays(millis, -1)
                            ChartPeriod.MONTH -> addMonths(millis, -1)
                            ChartPeriod.YEAR -> addYears(millis, -1)
                        }
                        year = getYear(newMillis)
                        month = getMonth(newMillis)
                        day = getDayOfMonth(newMillis)
                    }) { Text("<", fontWeight = FontWeight.Light) }

                    Text(
                        when (period) {
                            ChartPeriod.DAY -> "${year}年${month}月${day}日"
                            ChartPeriod.MONTH -> "${year}年${month}月"
                            ChartPeriod.YEAR -> "${year}年"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    TextButton(onClick = {
                        val millis = startOfDay(year, month, if (period == ChartPeriod.DAY) day else 1)
                        val newMillis = when (period) {
                            ChartPeriod.DAY -> addDays(millis, 1)
                            ChartPeriod.MONTH -> addMonths(millis, 1)
                            ChartPeriod.YEAR -> addYears(millis, 1)
                        }
                        year = getYear(newMillis)
                        month = getMonth(newMillis)
                        day = getDayOfMonth(newMillis)
                    }) { Text(">", fontWeight = FontWeight.Light) }
                }

                if (totalAmount == 0L) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("该时段暂无支出记录", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                PieChartCanvas(slices = slices, totalAmount = totalAmount, modifier = Modifier.size(240.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${slices.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text("个分类", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        item {
                            Text(
                                "合计 ¥${totalAmount.toMoneyString()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                        }

                        items(slices) { slice ->
                            val pct = if (totalAmount > 0) (slice.amount.toDouble() / totalAmount.toDouble() * 100).toInt() else 0
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(slice.color))
                                    Text(slice.category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    Text("$pct%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                                    Text("¥${slice.amount.toMoneyString()}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = slice.color)
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChartCanvas(slices: List<PieSlice>, totalAmount: Long, modifier: Modifier = Modifier) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f
        slices.forEach { slice ->
            val sweepAngle = if (totalAmount > 0) (slice.amount.toDouble() / totalAmount.toDouble() * 360).toFloat() else 0f
            drawArc(color = slice.color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2))
            startAngle += sweepAngle
        }
        drawCircle(color = surfaceColor, radius = radius * 0.55f, center = center)
    }
}
