package com.example.expensetracker.util

import kotlinx.datetime.*

fun formatDate(millis: Long, pattern: String): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return when (pattern) {
        "MM/dd HH:mm" -> {
            val m = local.monthNumber.toString().padStart(2, '0')
            val d = local.dayOfMonth.toString().padStart(2, '0')
            val h = local.hour.toString().padStart(2, '0')
            val min = local.minute.toString().padStart(2, '0')
            "$m/$d $h:$min"
        }
        "yyyy年MM月dd日" -> {
            "${local.year}年${local.monthNumber}月${local.dayOfMonth}日"
        }
        "yyyy年MM月" -> {
            "${local.year}年${local.monthNumber}月"
        }
        "yyyy年" -> {
            "${local.year}年"
        }
        "yyyyMMdd" -> {
            val m = local.monthNumber.toString().padStart(2, '0')
            val d = local.dayOfMonth.toString().padStart(2, '0')
            "${local.year}$m$d"
        }
        "MM/dd" -> {
            val m = local.monthNumber.toString().padStart(2, '0')
            val d = local.dayOfMonth.toString().padStart(2, '0')
            "$m/$d"
        }
        "yyyy/MM/dd" -> {
            val m = local.monthNumber.toString().padStart(2, '0')
            val d = local.dayOfMonth.toString().padStart(2, '0')
            "${local.year}/$m/$d"
        }
        "yyyy/MM/dd HH:mm" -> {
            val m = local.monthNumber.toString().padStart(2, '0')
            val d = local.dayOfMonth.toString().padStart(2, '0')
            val h = local.hour.toString().padStart(2, '0')
            val min = local.minute.toString().padStart(2, '0')
            "${local.year}/$m/$d $h:$min"
        }
        else -> millis.toString()
    }
}

fun getYear(millis: Long): Int =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).year

fun getMonth(millis: Long): Int =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber

fun getDayOfMonth(millis: Long): Int =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth

fun isSameDay(millis: Long, year: Int, month: Int, day: Int): Boolean {
    val local = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return local.year == year && local.monthNumber == month && local.dayOfMonth == day
}

fun isSameMonth(millis: Long, year: Int, month: Int): Boolean {
    val local = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return local.year == year && local.monthNumber == month
}

fun isSameYear(millis: Long, year: Int): Boolean {
    val local = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return local.year == year
}

fun addDays(millis: Long, days: Int): Long {
    val tz = TimeZone.currentSystemDefault()
    return Instant.fromEpochMilliseconds(millis).plus(days, DateTimeUnit.DAY, tz).toEpochMilliseconds()
}

fun addMonths(millis: Long, months: Int): Long {
    val tz = TimeZone.currentSystemDefault()
    return Instant.fromEpochMilliseconds(millis).plus(months, DateTimeUnit.MONTH, tz).toEpochMilliseconds()
}

fun addYears(millis: Long, years: Int): Long {
    val tz = TimeZone.currentSystemDefault()
    return Instant.fromEpochMilliseconds(millis).plus(years, DateTimeUnit.YEAR, tz).toEpochMilliseconds()
}

fun startOfDay(year: Int, month: Int, day: Int): Long {
    val tz = TimeZone.currentSystemDefault()
    return LocalDateTime(year, month, day, 0, 0, 0).toInstant(tz).toEpochMilliseconds()
}

fun endOfDay(year: Int, month: Int, day: Int): Long {
    val tz = TimeZone.currentSystemDefault()
    return LocalDateTime(year, month, day, 23, 59, 59, 999_000_000).toInstant(tz).toEpochMilliseconds()
}

fun currentYear(): Int {
    val now = Clock.System.now()
    return now.toLocalDateTime(TimeZone.currentSystemDefault()).year
}

fun currentMonth(): Int {
    val now = Clock.System.now()
    return now.toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber
}

fun currentDay(): Int {
    val now = Clock.System.now()
    return now.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth
}

fun Long.toMoneyString(): String {
    val yuan = this / 100
    val cents = (this % 100).let { if (it < 0) -it else it }
    return "$yuan.${cents.toString().padStart(2, '0')}"
}

fun String.parseCents(): Long {
    if (this.isEmpty()) return 0L
    val parts = this.split(".")
    val yuan = parts[0].toLong()
    val cents = if (parts.size > 1) parts[1].padEnd(2, '0').take(2).toLong() else 0L
    return yuan * 100 + cents
}
