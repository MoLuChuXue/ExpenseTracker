package com.example.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.abs
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensetracker.data.Expense
import com.example.expensetracker.util.*
import kotlin.math.roundToInt

private enum class LineChartPeriod { WEEK, MONTH, YEAR }

private data class BalancePoint(
    val dateMillis: Long,
    val label: String,
    val balance: Double,
    val expense: Double,
    val income: Double
)

@Composable
fun AssetLineChartDialog(
    expenses: List<Expense>,
    initialBalance: Double,
    onDismiss: () -> Unit
) {
    val nowYear = remember { currentYear() }
    val nowMonth = remember { currentMonth() }
    val nowDay = remember { currentDay() }

    var period by remember { mutableStateOf(LineChartPeriod.MONTH) }
    var anchorYear by remember { mutableIntStateOf(nowYear) }
    var anchorMonth by remember { mutableIntStateOf(nowMonth) }
    var anchorDay by remember { mutableIntStateOf(nowDay) }

    val (startMillis, endMillis) = remember(period, anchorYear, anchorMonth, anchorDay) {
        when (period) {
            LineChartPeriod.WEEK -> {
                val end = startOfDay(anchorYear, anchorMonth, anchorDay)
                val start = addDays(end, -6)
                start to end
            }
            LineChartPeriod.MONTH -> {
                val start = startOfDay(anchorYear, anchorMonth, 1)
                val end = startOfDay(anchorYear, anchorMonth, daysInMonth(anchorYear, anchorMonth))
                start to end
            }
            LineChartPeriod.YEAR -> {
                val start = startOfDay(anchorYear, 1, 1)
                val end = startOfDay(anchorYear, 12, 31)
                start to end
            }
        }
    }

    val points = remember(expenses, initialBalance, startMillis, endMillis, period) {
        computeBalancePoints(expenses, initialBalance, startMillis, endMillis, period)
    }

    var selectedIndex by remember(points.size) { mutableIntStateOf((points.size - 1).coerceAtLeast(0)) }

    LaunchedEffect(points.size) {
        selectedIndex = (points.size - 1).coerceAtLeast(0)
    }

    val selectedPoint = points.getOrNull(selectedIndex)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val navLabel = remember(period, anchorYear, anchorMonth, anchorDay, endMillis) {
        when (period) {
            LineChartPeriod.WEEK -> {
                val weekStart = addDays(endMillis, -6)
                "${formatDate(weekStart, "MM/dd")} – ${formatDate(endMillis, "MM/dd")}"
            }
            LineChartPeriod.MONTH -> "${anchorYear}年${anchorMonth}月"
            LineChartPeriod.YEAR -> "${anchorYear}年"
        }
    }

    val canGoForward = remember(endMillis) {
        val today = startOfDay(currentYear(), currentMonth(), currentDay())
        endMillis < today
    }

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
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                    Text(
                        "资产走势",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val options = listOf(
                        LineChartPeriod.WEEK to "周",
                        LineChartPeriod.MONTH to "月",
                        LineChartPeriod.YEAR to "年"
                    )
                    options.forEach { (p, label) ->
                        val selected = period == p
                        Surface(
                            modifier = Modifier.weight(1f).clickable { period = p },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) primaryColor
                            else surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                label,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = {
                        when (period) {
                            LineChartPeriod.WEEK -> {
                                val newEnd = addDays(endMillis, -7)
                                anchorYear = getYear(newEnd); anchorMonth = getMonth(newEnd); anchorDay = getDayOfMonth(newEnd)
                            }
                            LineChartPeriod.MONTH -> {
                                val millis = addMonths(startOfDay(anchorYear, anchorMonth, 1), -1)
                                anchorYear = getYear(millis); anchorMonth = getMonth(millis)
                            }
                            LineChartPeriod.YEAR -> { anchorYear -= 1 }
                        }
                    }) { Text("<", fontWeight = FontWeight.Light) }

                    Text(navLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp))

                    TextButton(onClick = {
                        when (period) {
                            LineChartPeriod.WEEK -> {
                                val newEnd = addDays(endMillis, 7)
                                anchorYear = getYear(newEnd); anchorMonth = getMonth(newEnd); anchorDay = getDayOfMonth(newEnd)
                            }
                            LineChartPeriod.MONTH -> {
                                val millis = addMonths(startOfDay(anchorYear, anchorMonth, 1), 1)
                                anchorYear = getYear(millis); anchorMonth = getMonth(millis)
                            }
                            LineChartPeriod.YEAR -> { anchorYear += 1 }
                        }
                    }, enabled = canGoForward) { Text(">", fontWeight = FontWeight.Light) }
                }

                if (points.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("该时段暂无数据", style = MaterialTheme.typography.bodyLarge, color = onSurfaceVariant)
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        LineChartCanvas(
                            points = points,
                            selectedIndex = selectedIndex,
                            primaryColor = primaryColor,
                            onSurface = MaterialTheme.colorScheme.onSurface,
                            onSurfaceVariant = onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(start = 8.dp, end = 16.dp, top = 8.dp),
                            onSelect = { selectedIndex = it }
                        )

                        selectedPoint?.let { pt ->
                            val netColor = if (pt.income - pt.expense >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(pt.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("支出", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                                            Text("¥${pt.expense.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFFE53935))
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("收入", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                                            Text("¥${pt.income.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("净资产", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                                            Text("¥${pt.balance.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = netColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineChartCanvas(
    points: List<BalancePoint>,
    selectedIndex: Int,
    primaryColor: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val axisLabelStyle = remember { TextStyle(fontSize = 10.sp, color = onSurfaceVariant, textAlign = TextAlign.Center) }
    val axisLabelStyleRight = remember { TextStyle(fontSize = 10.sp, color = onSurfaceVariant, textAlign = TextAlign.End) }

    Canvas(
        modifier = modifier.pointerInput(points.size) {
            detectTapGestures { offset ->
                if (points.size <= 1) return@detectTapGestures
                val chartLeft = 52.dp.toPx()
                val chartRight = size.width - 8.dp.toPx()
                if (offset.x < chartLeft || offset.x > chartRight) return@detectTapGestures
                val stepX = (chartRight - chartLeft) / (points.size - 1)
                val index = ((offset.x - chartLeft) / stepX).roundToInt().coerceIn(0, points.size - 1)
                onSelect(index)
            }
        }
    ) {
        if (points.isEmpty()) return@Canvas

        val chartLeft = 52.dp.toPx()
        val chartRight = size.width - 8.dp.toPx()
        val chartTop = 8.dp.toPx()
        val chartBottom = size.height - 24.dp.toPx()
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        val minBalance = points.minOf { it.balance }
        val maxBalance = points.maxOf { it.balance }
        val dataRange = maxBalance - minBalance
        val fallback = (abs(maxBalance) * 0.1).coerceAtLeast(100.0)
        val range = if (dataRange < 0.01) fallback else dataRange
        val yMin = minBalance - range * 0.15
        val yMax = maxBalance + range * 0.15
        val yRange = yMax - yMin

        fun yPos(value: Double) = chartBottom - ((value - yMin) / yRange * chartHeight).toFloat()
        fun xPos(index: Int) = chartLeft + chartWidth * index / (points.size - 1).coerceAtLeast(1)

        // Horizontal grid lines + Y-axis labels
        val gridColor = onSurfaceVariant.copy(alpha = 0.12f)
        for (i in 0..3) {
            val y = chartTop + chartHeight * i / 3
            drawLine(gridColor, Offset(chartLeft, y), Offset(chartRight, y), strokeWidth = 1.dp.toPx())
            val value = yMax - yRange * i / 3
            val label = formatAxisValue(value)
            val result = textMeasurer.measure(label, axisLabelStyleRight)
            drawText(result, topLeft = Offset(chartLeft - result.size.width - 4.dp.toPx(), y - result.size.height / 2))
        }

        // X-axis labels (max ~7 labels, keyed to index)
        val maxLabels = 7
        val labelStep = if (points.size <= maxLabels) 1 else points.size / maxLabels
        for (i in points.indices step labelStep) {
            val x = xPos(i)
            val result = textMeasurer.measure(points[i].label, axisLabelStyle)
            drawText(result, topLeft = Offset(x - result.size.width / 2, chartBottom + 4.dp.toPx()))
        }
        // Always last point label
        if (points.size > 1) {
            val lastX = xPos(points.size - 1)
            val lastResult = textMeasurer.measure(points.last().label, axisLabelStyle)
            drawText(lastResult, topLeft = Offset(lastX - lastResult.size.width / 2, chartBottom + 4.dp.toPx()))
        }

        // Line
        if (points.size >= 2) {
            val path = Path()
            points.forEachIndexed { i, pt ->
                val x = xPos(i)
                val y = yPos(pt.balance)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, primaryColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        }

        // Data points
        points.forEachIndexed { i, pt ->
            val x = xPos(i)
            val y = yPos(pt.balance)
            if (i == selectedIndex) {
                drawCircle(primaryColor, radius = 8.dp.toPx(), center = Offset(x, y))
                drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(x, y))
                // Dashed drop line
                val dashPath = Path().apply {
                    moveTo(x, y + 10.dp.toPx())
                    lineTo(x, chartBottom)
                }
                drawPath(dashPath, primaryColor.copy(alpha = 0.35f),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))))
            } else {
                drawCircle(primaryColor.copy(alpha = 0.6f), radius = 3.5.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

private fun formatAxisValue(value: Double): String {
    val av = abs(value)
    return when {
        av >= 100_000_000 -> "${(value / 100_000_000).roundToInt()}亿"
        av >= 100_000 -> "${(value / 10_000).roundToInt()}万"
        av >= 10_000 -> {
            val v = value / 10_000
            val dec = (abs(v - v.toLong().toDouble()) * 10).roundToInt()
            if (dec == 0) "${v.toLong()}万" else "${v.toLong()}.${dec}万"
        }
        else -> value.toLong().toString()
    }
}

private fun computeBalancePoints(
    expenses: List<Expense>,
    initialBalance: Double,
    startMillis: Long,
    endMillis: Long,
    period: LineChartPeriod
): List<BalancePoint> {
    val sorted = expenses.sortedBy { it.dateMillis }

    val beforeNet = sorted
        .filter { it.dateMillis < startMillis }
        .sumOf { if (it.type == "income") it.amount else -it.amount }

    var running = initialBalance + beforeNet
    val result = mutableListOf<BalancePoint>()

    when (period) {
        LineChartPeriod.WEEK, LineChartPeriod.MONTH -> {
            var dayStart = startMillis
            while (dayStart <= endMillis) {
                val dayEnd = dayStart + 86_400_000L - 1
                val dayExpenses = sorted.filter { it.dateMillis in dayStart..dayEnd }
                val dayExpense = dayExpenses.filter { it.type == "expense" }.sumOf { it.amount }
                val dayIncome = dayExpenses.filter { it.type == "income" }.sumOf { it.amount }
                running += dayIncome - dayExpense
                result.add(BalancePoint(dayStart, formatDate(dayStart, "MM/dd"), running, dayExpense, dayIncome))
                dayStart = addDays(dayStart, 1)
            }
        }
        LineChartPeriod.YEAR -> {
            for (m in 1..12) {
                val monthStart = startOfDay(getYear(startMillis), m, 1)
                val monthEnd = if (m == 12) endMillis
                else startOfDay(getYear(startMillis), m + 1, 1) - 1
                val monthExpenses = sorted.filter { it.dateMillis in monthStart..monthEnd }
                val monthExpense = monthExpenses.filter { it.type == "expense" }.sumOf { it.amount }
                val monthIncome = monthExpenses.filter { it.type == "income" }.sumOf { it.amount }
                running += monthIncome - monthExpense
                result.add(BalancePoint(monthStart, "${m}月", running, monthExpense, monthIncome))
            }
        }
    }

    // Keep only days with transactions, plus first and last as anchors
    return result.filterIndexed { i, pt ->
        i == 0 || i == result.lastIndex || pt.expense > 0 || pt.income > 0
    }
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}
