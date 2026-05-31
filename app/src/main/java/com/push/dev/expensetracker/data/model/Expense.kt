package com.push.dev.expensetracker.data.model

import com.push.dev.expensetracker.data.local.entity.ExpenseEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Expense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: Category,
    val date: LocalDate,
    val notes: String = ""
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    category = category.name,
    date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    notes = notes
)

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    title = title,
    amount = amount,
    category = Category.valueOf(category),
    date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate(),
    notes = notes
)