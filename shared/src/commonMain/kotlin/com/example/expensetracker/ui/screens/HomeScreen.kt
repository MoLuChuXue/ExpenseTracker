package com.example.expensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.ui.components.AddExpenseDialog
import com.example.expensetracker.ui.components.AssetLineChartDialog
import com.example.expensetracker.ui.components.ExpenseItem
import com.example.expensetracker.ui.components.PieChartDialog
import com.example.expensetracker.ui.components.SettingsDialog
import com.example.expensetracker.ui.components.getCategoryInfo
import com.example.expensetracker.ui.theme.themePresets
import com.example.expensetracker.util.*
import kotlinx.datetime.Clock

private enum class SortMode { BY_DATE, BY_CATEGORY, BY_AMOUNT_DESC, BY_AMOUNT_ASC }
private enum class TimeFilter { ALL, DAY, MONTH, YEAR, RANGE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    expenses: List<Expense>,
    budget: Double,
    balance: Double,
    themeIndex: Int,
    customExpenseJson: String,
    customIncomeJson: String,
    onAddExpense: (Double, String, String, Long, String) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onSaveSettings: (Double, Double, Int) -> Unit,
    onSaveCustomCategories: (String, String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showChartDialog by remember { mutableStateOf(false) }
    var showLineChartDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var sortMode by remember { mutableStateOf(SortMode.BY_DATE) }
    var searchQuery by remember { mutableStateOf("") }
    var collapsedCategories by remember { mutableStateOf(setOf<String>()) }
    var collapsedDays by remember { mutableStateOf(setOf<String>()) }
    val currentPreset = themePresets.getOrElse(themeIndex) { themePresets[0] }
    val focusManager = LocalFocusManager.current

    // Time filter state
    val nowYear = currentYear()
    val nowMonth = currentMonth()
    val nowDay = currentDay()
    var timeFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var filterYear by remember { mutableIntStateOf(nowYear) }
    var filterMonth by remember { mutableIntStateOf(nowMonth) }
    var filterDay by remember { mutableIntStateOf(nowDay) }
    var rangeStart by remember { mutableStateOf<Long?>(null) }
    var rangeEnd by remember { mutableStateOf<Long?>(null) }

    // Date picker state
    var startPickerOpen by remember { mutableStateOf(false) }
    var endPickerOpen by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState()
    val endPickerState = rememberDatePickerState()

    val timeFilteredExpenses = remember(timeFilter, expenses, filterYear, filterMonth, filterDay, rangeStart, rangeEnd) {
        when (timeFilter) {
            TimeFilter.ALL -> expenses
            TimeFilter.DAY -> expenses.filter { isSameDay(it.dateMillis, filterYear, filterMonth, filterDay) }
            TimeFilter.MONTH -> expenses.filter { isSameMonth(it.dateMillis, filterYear, filterMonth) }
            TimeFilter.YEAR -> expenses.filter { isSameYear(it.dateMillis, filterYear) }
            TimeFilter.RANGE -> {
                val s = rangeStart
                val e = rangeEnd
                if (s != null && e != null) expenses.filter { it.dateMillis in s..e }
                else expenses
            }
        }
    }
    val filteredTotalExpense = remember(timeFilteredExpenses) {
        timeFilteredExpenses.filter { it.type == "expense" }.sumOf { it.amount }
    }
    val filteredTotalIncome = remember(timeFilteredExpenses) {
        timeFilteredExpenses.filter { it.type == "income" }.sumOf { it.amount }
    }

    val currentMonthExpense = remember(expenses, nowYear, nowMonth) {
        expenses.filter {
            it.type == "expense" && isSameMonth(it.dateMillis, nowYear, nowMonth)
        }.sumOf { it.amount }
    }

    val filteredExpenses = remember(timeFilteredExpenses, searchQuery) {
        if (searchQuery.isBlank()) timeFilteredExpenses
        else timeFilteredExpenses.filter { it.note.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("记账本", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { showChartDialog = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "饼图")
                    }
                    IconButton(onClick = { showLineChartDialog = true }) {
                        Icon(Icons.Default.ShowChart, contentDescription = "资产走势")
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = currentPreset.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { paddingValues ->
        if (expenses.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            // Pre-compute grouped/sorted lists in @Composable scope
            val categoryGrouped = remember(filteredExpenses) { filteredExpenses.groupBy { it.category } }
            val amountSortedDesc = remember(filteredExpenses) { filteredExpenses.sortedByDescending { it.amount } }
            val amountSortedAsc = remember(filteredExpenses) { filteredExpenses.sortedBy { it.amount } }
            val dateGrouped = remember(filteredExpenses) { filteredExpenses.groupBy { formatDate(it.dateMillis, "yyyyMMdd") } }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Total card
                item {
                    TotalCard(
                        totalExpense = filteredTotalExpense,
                        totalIncome = filteredTotalIncome,
                        budget = budget,
                        balance = balance,
                        monthlyExpense = currentMonthExpense,
                        primaryColor = currentPreset.primary,
                        timeLabel = when (timeFilter) {
                            TimeFilter.ALL -> "全部"
                            TimeFilter.DAY -> "${filterYear}年${filterMonth}月${filterDay}日"
                            TimeFilter.MONTH -> "${filterYear}年${filterMonth}月"
                            TimeFilter.YEAR -> "${filterYear}年"
                            TimeFilter.RANGE -> {
                                val s = rangeStart?.let { formatDate(it, "MM/dd") } ?: "?"
                                val e = rangeEnd?.let { formatDate(it, "MM/dd") } ?: "?"
                                "$s – $e"
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // Time filter row
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val timeOptions = listOf(
                            TimeFilter.ALL to "全部", TimeFilter.DAY to "日",
                            TimeFilter.MONTH to "月", TimeFilter.YEAR to "年",
                            TimeFilter.RANGE to "范围"
                        )
                        timeOptions.forEach { (filter, label) ->
                            val selected = timeFilter == filter
                            Surface(
                                modifier = Modifier.clickable { timeFilter = filter },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) currentPreset.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    label,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Date navigation
                if (timeFilter != TimeFilter.ALL && timeFilter != TimeFilter.RANGE) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val baseMillis = startOfDay(filterYear, filterMonth, if (timeFilter == TimeFilter.DAY) filterDay else 1)
                            IconButton(onClick = {
                                val newMillis = when (timeFilter) {
                                    TimeFilter.DAY -> addDays(baseMillis, -1)
                                    TimeFilter.MONTH -> addMonths(baseMillis, -1)
                                    TimeFilter.YEAR -> addYears(baseMillis, -1)
                                    else -> baseMillis
                                }
                                filterYear = getYear(newMillis)
                                filterMonth = getMonth(newMillis)
                                filterDay = getDayOfMonth(newMillis)
                            }) { Text("<", fontWeight = FontWeight.Light, style = MaterialTheme.typography.titleMedium) }
                            Text(
                                when (timeFilter) {
                                    TimeFilter.DAY -> "${filterYear}年${filterMonth}月${filterDay}日"
                                    TimeFilter.MONTH -> "${filterYear}年${filterMonth}月"
                                    TimeFilter.YEAR -> "${filterYear}年"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = {
                                val newMillis = when (timeFilter) {
                                    TimeFilter.DAY -> addDays(baseMillis, 1)
                                    TimeFilter.MONTH -> addMonths(baseMillis, 1)
                                    TimeFilter.YEAR -> addYears(baseMillis, 1)
                                    else -> baseMillis
                                }
                                filterYear = getYear(newMillis)
                                filterMonth = getMonth(newMillis)
                                filterDay = getDayOfMonth(newMillis)
                            }) { Text(">", fontWeight = FontWeight.Light, style = MaterialTheme.typography.titleMedium) }
                        }
                    }
                }

                // Custom range pickers
                if (timeFilter == TimeFilter.RANGE) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f).clickable { startPickerOpen = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    rangeStart?.let { formatDate(it, "yyyy/MM/dd") } ?: "起始日期",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (rangeStart != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(" – ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                modifier = Modifier.weight(1f).clickable { endPickerOpen = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    rangeEnd?.let { formatDate(it, "yyyy/MM/dd") } ?: "结束日期",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (rangeEnd != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Search & sort
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("搜索备注…") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "清除", modifier = Modifier.size(20.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentPreset.primary,
                                cursorColor = currentPreset.primary
                            )
                        )

                        var sortExpanded by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                modifier = Modifier.clickable { sortExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    when (sortMode) {
                                        SortMode.BY_DATE -> "时间"
                                        SortMode.BY_CATEGORY -> "用途"
                                        SortMode.BY_AMOUNT_DESC -> "金额↓"
                                        SortMode.BY_AMOUNT_ASC -> "金额↑"
                                    },
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                                DropdownMenuItem(text = { Text("按时间") }, onClick = { sortMode = SortMode.BY_DATE; sortExpanded = false })
                                DropdownMenuItem(text = { Text("按用途") }, onClick = { sortMode = SortMode.BY_CATEGORY; sortExpanded = false })
                                DropdownMenuItem(text = { Text("金额从高到低") }, onClick = { sortMode = SortMode.BY_AMOUNT_DESC; sortExpanded = false })
                                DropdownMenuItem(text = { Text("金额从低到高") }, onClick = { sortMode = SortMode.BY_AMOUNT_ASC; sortExpanded = false })
                            }
                        }
                    }

                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "找到 ${filteredExpenses.size} 条记录",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Expense list
                when (sortMode) {
                    SortMode.BY_CATEGORY -> {
                        categoryGrouped.forEach { (category, list) ->
                            val isExpanded = category !in collapsedCategories
                            item(key = "header_$category") {
                                CategoryHeader(
                                    categoryInfo = getCategoryInfo(category, customExpenseJson, customIncomeJson),
                                    count = list.size,
                                    subtotal = list.sumOf { it.amount },
                                    isExpanded = isExpanded,
                                    onToggle = {
                                        collapsedCategories = if (isExpanded) collapsedCategories + category
                                        else collapsedCategories - category
                                    }
                                )
                            }
                            if (isExpanded) {
                                items(list, key = { it.id }) { expense ->
                                    ExpenseItem(expense = expense, onDelete = { onDeleteExpense(expense) }, onEdit = { editingExpense = expense }, customExpenseJson = customExpenseJson, customIncomeJson = customIncomeJson)
                                }
                            }
                        }
                    }
                    SortMode.BY_AMOUNT_DESC -> {
                        items(amountSortedDesc, key = { it.id }) { expense ->
                            ExpenseItem(expense = expense, onDelete = { onDeleteExpense(expense) }, onEdit = { editingExpense = expense }, customExpenseJson = customExpenseJson, customIncomeJson = customIncomeJson)
                        }
                    }
                    SortMode.BY_AMOUNT_ASC -> {
                        items(amountSortedAsc, key = { it.id }) { expense ->
                            ExpenseItem(expense = expense, onDelete = { onDeleteExpense(expense) }, onEdit = { editingExpense = expense }, customExpenseJson = customExpenseJson, customIncomeJson = customIncomeJson)
                        }
                    }
                    SortMode.BY_DATE -> {
                        dateGrouped.forEach { (dayKey, dayList) ->
                            val isExpanded = dayKey !in collapsedDays
                            item(key = "day_$dayKey") {
                                DayHeader(
                                    label = formatDate(dayList.first().dateMillis, "yyyy年MM月dd日"),
                                    count = dayList.size,
                                    total = dayList.sumOf { it.amount },
                                    isExpanded = isExpanded,
                                    onToggle = {
                                        collapsedDays = if (isExpanded) collapsedDays + dayKey
                                        else collapsedDays - dayKey
                                    }
                                )
                            }
                            if (isExpanded) {
                                items(dayList, key = { it.id }) { expense ->
                                    ExpenseItem(expense = expense, onDelete = { onDeleteExpense(expense) }, onEdit = { editingExpense = expense }, customExpenseJson = customExpenseJson, customIncomeJson = customIncomeJson)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Date picker dialogs
    if (startPickerOpen) {
        DatePickerDialog(
            onDismissRequest = { startPickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let {
                        rangeStart = startOfDay(getYear(it), getMonth(it), getDayOfMonth(it))
                    }
                    startPickerOpen = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { startPickerOpen = false }) { Text("取消") } }
        ) { DatePicker(state = startPickerState) }
    }
    if (endPickerOpen) {
        DatePickerDialog(
            onDismissRequest = { endPickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let {
                        rangeEnd = endOfDay(getYear(it), getMonth(it), getDayOfMonth(it))
                    }
                    endPickerOpen = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { endPickerOpen = false }) { Text("取消") } }
        ) { DatePicker(state = endPickerState) }
    }

    // Dialogs
    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, category, note, date, type ->
                onAddExpense(amount, category, note, date, type)
                showAddDialog = false
            },
            customExpenseJson = customExpenseJson,
            customIncomeJson = customIncomeJson,
            onSaveCustomCategories = onSaveCustomCategories
        )
    }

    editingExpense?.let { expense ->
        AddExpenseDialog(
            onDismiss = { editingExpense = null },
            onConfirm = { amount, category, note, date, type ->
                onUpdateExpense(expense.copy(amount = amount, category = category, note = note, dateMillis = date, type = type))
                editingExpense = null
            },
            editExpense = expense,
            customExpenseJson = customExpenseJson,
            customIncomeJson = customIncomeJson,
            onSaveCustomCategories = onSaveCustomCategories
        )
    }

    if (showChartDialog) {
        PieChartDialog(expenses = expenses, onDismiss = { showChartDialog = false }, customExpenseJson = customExpenseJson, customIncomeJson = customIncomeJson)
    }

    if (showLineChartDialog) {
        AssetLineChartDialog(
            expenses = expenses,
            initialBalance = balance,
            onDismiss = { showLineChartDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentBudget = budget, currentBalance = balance, currentThemeIndex = themeIndex,
            onDismiss = { showSettingsDialog = false },
            onSave = { newBudget, newBalance, newThemeIndex ->
                onSaveSettings(newBudget, newBalance, newThemeIndex)
                showSettingsDialog = false
            },
            onExport = onExport,
            onImport = onImport
        )
    }
}

@Composable

private fun TotalCard(totalExpense: Double, totalIncome: Double, budget: Double, balance: Double, monthlyExpense: Double, primaryColor: Color, timeLabel: String) {
    val netAmount = totalIncome - totalExpense
    val currentBalance = balance + netAmount
    val budgetProgress = if (budget > 0) (monthlyExpense / budget).toFloat().coerceIn(0f, 1f) else 0f
    val budgetRemaining = (budget - monthlyExpense).coerceAtLeast(0.0)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = primaryColor,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("收支概览 · $timeLabel", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(12.dp))
            Text("¥${currentBalance.toMoneyString()}", style = MaterialTheme.typography.displayLarge, color = Color.White, fontWeight = FontWeight.Bold)
            if (balance > 0) {
                Text("当前余额", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+¥${totalIncome.toMoneyString()}", style = MaterialTheme.typography.titleMedium, color = Color(0xFFA5D6A7), fontWeight = FontWeight.SemiBold)
                    Text("收入", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("-¥${totalExpense.toMoneyString()}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("支出", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
            }

            if (budget > 0) {
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = budgetProgress,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = if (budgetProgress >= 1f) Color(0xFFFFCDD2) else Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("预算 ¥${budget.toMoneyString()}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        if (budgetRemaining > 0) "剩余 ¥${budgetRemaining.toMoneyString()}" else "超支 ¥${(-budgetRemaining).toMoneyString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (budgetRemaining > 0) Color.White.copy(alpha = 0.7f) else Color(0xFFFFCDD2)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    categoryInfo: com.example.expensetracker.ui.components.CategoryInfo,
    count: Int,
    subtotal: Double,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(categoryInfo.icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = categoryInfo.color)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(categoryInfo.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${count}笔", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("¥${subtotal.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null, modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun DayHeader(label: String, count: Int, total: Double, isExpanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${count}笔", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("¥${total.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null, modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            Text("还没有记录", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))
            Text("点击 + 按钮开始记账", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
        }
    }
}
