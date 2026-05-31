package com.push.dev.expensetracker.data.repository

import com.push.dev.expensetracker.data.local.dao.ExpenseDao
import com.push.dev.expensetracker.data.model.Category
import com.push.dev.expensetracker.data.model.Expense
import com.push.dev.expensetracker.data.model.toDomain
import com.push.dev.expensetracker.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
) {
    fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { it.map { e -> e.toDomain() } }

    fun getRecentExpenses(limit: Int = 5): Flow<List<Expense>> =
        dao.getRecentExpenses(limit).map { it.map { e -> e.toDomain() } }

    fun getMonthlyExpenses(yearMonth: YearMonth): Flow<List<Expense>> =
        dao.getMonthlyExpenses(yearMonth.startMs(), yearMonth.endMs())
            .map { it.map { e -> e.toDomain() } }

    fun getMonthlyTotal(yearMonth: YearMonth): Flow<Double> =
        dao.getMonthlyTotal(yearMonth.startMs(), yearMonth.endMs())

    fun getExpensesByCategory(category: Category): Flow<List<Expense>> =
        dao.getExpensesByCategory(category.name).map { it.map { e -> e.toDomain() } }

    fun searchExpenses(query: String): Flow<List<Expense>> =
        dao.searchExpenses(query).map { it.map { e -> e.toDomain() } }

    suspend fun getExpenseById(id: Long): Expense? =
        dao.getExpenseById(id)?.toDomain()

    suspend fun getMonthlyExpensesOnce(yearMonth: YearMonth): List<Expense> =
        dao.getMonthlyExpensesOnce(yearMonth.startMs(), yearMonth.endMs())
            .map { it.toDomain() }

    suspend fun insertExpense(expense: Expense): Long =
        dao.insertExpense(expense.toEntity())

    suspend fun updateExpense(expense: Expense) =
        dao.updateExpense(expense.toEntity())

    suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense.toEntity())

    private fun YearMonth.startMs(): Long =
        atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun YearMonth.endMs(): Long =
        atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}