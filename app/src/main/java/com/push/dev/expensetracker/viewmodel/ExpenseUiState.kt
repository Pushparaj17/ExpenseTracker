package com.push.dev.expensetracker.viewmodel

import com.push.dev.expensetracker.data.model.Category
import com.push.dev.expensetracker.data.model.Expense
import com.push.dev.expensetracker.data.model.MonthlySummary
import java.time.LocalDate
import java.time.YearMonth

data class DashboardUiState(
    val recentExpenses: List<Expense> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val categorySpending: Map<Category, Double> = emptyMap(),
    val monthlySummary: MonthlySummary = MonthlySummary(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true
)

data class HistoryUiState(
    val allExpenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val isLoading: Boolean = true
)

data class AnalyticsUiState(
    val categorySpending: Map<Category, Double> = emptyMap(),
    val monthlyTrend: List<Pair<String, Double>> = emptyList(),
    val dailyTrend: List<Pair<String, Double>> = emptyList(),
    val monthlySummary: MonthlySummary = MonthlySummary(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true
)

data class AddEditExpenseUiState(
    val id: Long = 0L,
    val title: String = "",
    val amount: String = "",
    val category: Category = Category.OTHER,
    val date: LocalDate = LocalDate.now(),
    val notes: String = "",
    val titleError: String? = null,
    val amountError: String? = null,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false
)

enum class SortOrder(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount")
}