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
    val balance: Long,
    val expense: Long,
    val income: Long
)

private data class RangeInfo(
    val yMin: Double,
    val yMax: Double,
    val yRange: Double,
    val yLabels: List<String>
)

@Composable
fun AssetLineChartDialog(
    expenses: List<Expense>,
    initialBalance: Long,
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
                            val netColor = if (pt.income >= pt.expense) Color(0xFF4CAF50) else Color(0xFFE53935)
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

    // Pre-compute range info and Y-axis labels so we can measure them
    val rangeInfo = remember(points) {
        if (points.isEmpty()) return@remember null
        val minBalance = points.minOf { it.balance }.toDouble()
        val maxBalance = points.maxOf { it.balance }.toDouble()
        val dataRange = maxBalance - minBalance
        val fallback = (abs(maxBalance) * 0.1).coerceAtLeast(100.0)
        val range = if (dataRange < 0.01) fallback else dataRange
        val yMin = minBalance - range * 0.15
        val yMax = maxBalance + range * 0.15
        val yRange = yMax - yMin
        val yLabels = (0..3).map { i -> formatAxisValue(yMax - yRange * i / 3) }
        RangeInfo(yMin, yMax, yRange, yLabels)
    }

    // Measure Y-axis labels to determine left margin
    val yLabelWidths = remember(rangeInfo) {
        rangeInfo?.yLabels?.map { textMeasurer.measure(it, axisLabelStyleRight).size.width } ?: emptyList()
    }

    Canvas(
        modifier = modifier.pointerInput(points.size, yLabelWidths) {
            detectTapGestures { offset ->
                if (points.size <= 1) return@detectTapGestures
                val leftPad = 8.dp.toPx()
                val labelGap = 4.dp.toPx()
                val maxYWidth = yLabelWidths.maxOrNull()?.toFloat() ?: 0f
                val chartLeft = leftPad + maxYWidth + labelGap
                val chartRight = size.width - 8.dp.toPx()
                if (offset.x < chartLeft || offset.x > chartRight) return@detectTapGestures
                val stepX = (chartRight - chartLeft) / (points.size - 1)
                val index = ((offset.x - chartLeft) / stepX).roundToInt().coerceIn(0, points.size - 1)
                onSelect(index)
            }
        }
    ) {
        if (points.isEmpty() || rangeInfo == null) return@Canvas

        val leftPad = 8.dp.toPx()
        val labelGap = 4.dp.toPx()
        val maxYWidth = yLabelWidths.maxOrNull()?.toFloat() ?: 0f
        val chartLeft = leftPad + maxYWidth + labelGap

        // Measure X-axis labels to determine bottom margin
        val xLabelSizes = points.mapIndexedNotNull { i, pt ->
            if (i % (if (points.size <= 7) 1 else points.size / 7).coerceAtLeast(1) == 0 || i == points.lastIndex)
                textMeasurer.measure(pt.label, axisLabelStyle).size
            else null
        }
        val xLabelHeight = xLabelSizes.maxOfOrNull { it.height }?.toFloat() ?: 0f

        val chartRight = size.width - 8.dp.toPx()
        val chartTop = 8.dp.toPx()
        val chartBottom = size.height - 4.dp.toPx() - xLabelHeight
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        val (yMin, yMax, yRange, yLabels) = rangeInfo

        fun yPos(value: Long) = chartBottom - ((value.toDouble() - yMin) / yRange * chartHeight).toFloat()
        fun xPos(index: Int) = chartLeft + chartWidth * index / (points.size - 1).coerceAtLeast(1)

        // Horizontal grid lines + Y-axis labels
        val gridColor = onSurfaceVariant.copy(alpha = 0.12f)
        for (i in 0..3) {
            val y = chartTop + chartHeight * i / 3
            drawLine(gridColor, Offset(chartLeft, y), Offset(chartRight, y), strokeWidth = 1.dp.toPx())
            val result = textMeasurer.measure(yLabels[i], axisLabelStyleRight)
            drawText(result, topLeft = Offset(chartLeft - result.size.width - labelGap, y - result.size.height / 2))
        }

        // X-axis labels with edge clamping
        val maxLabels = 7
        val labelStep = if (points.size <= maxLabels) 1 else points.size / maxLabels
        val maxX = chartRight
        val minX = chartLeft
        for (i in points.indices step labelStep) {
            val x = xPos(i)
            val result = textMeasurer.measure(points[i].label, axisLabelStyle)
            val drawX = (x - result.size.width / 2).coerceIn(minX - result.size.width / 2, maxX - result.size.width)
            drawText(result, topLeft = Offset(drawX, chartBottom + 4.dp.toPx()))
        }
        // Always last point label
        if (points.size > 1) {
            val lastX = xPos(points.size - 1)
            val lastResult = textMeasurer.measure(points.last().label, axisLabelStyle)
            val drawX = (lastX - lastResult.size.width / 2).coerceIn(minX - lastResult.size.width / 2, maxX - lastResult.size.width)
            drawText(lastResult, topLeft = Offset(drawX, chartBottom + 4.dp.toPx()))
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
    val v = value / 100.0
    val av = kotlin.math.abs(v)
    return when {
        av >= 100_000_000 -> "${(v / 100_000_000).roundToInt()}亿"
        av >= 100_000 -> "${(v / 10_000).roundToInt()}万"
        av >= 10_000 -> {
            val w = v / 10_000
            val dec = (kotlin.math.abs(w - w.toLong().toDouble()) * 10).roundToInt()
            if (dec == 0) "${w.toLong()}万" else "${w.toLong()}.${dec}万"
        }
        else -> v.toLong().toString()
    }
}

private fun computeBalancePoints(
    expenses: List<Expense>,
    initialBalance: Long,
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
        i == 0 || i == result.lastIndex || pt.expense > 0L || pt.income > 0L
    }
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}
