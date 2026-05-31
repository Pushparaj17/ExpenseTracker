package com.push.dev.expensetracker.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

fun LocalDate.toDisplayString(): String = format(displayFormatter)

fun YearMonth.toDisplayString(): String = format(monthFormatter)

fun YearMonth.toShortString(): String = format(shortMonthFormatter)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this, displayFormatter)

fun LocalDate.isToday(): Boolean = this == LocalDate.now()

fun LocalDate.isThisWeek(): Boolean {
    val today = LocalDate.now()
    return this >= today.minusDays(6) && this <= today
}