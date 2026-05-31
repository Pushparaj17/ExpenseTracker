package com.push.dev.expensetracker.data.model

data class MonthlySummary(
    val totalSpending: Double = 0.0,
    val highestCategory: Category? = null,
    val highestCategoryAmount: Double = 0.0,
    val averageDailySpending: Double = 0.0,
    val transactionCount: Int = 0,
    val budgetLimit: Double = 0.0
) {
    val isBudgetExceeded: Boolean
        get() = budgetLimit > 0 && totalSpending > budgetLimit

    val budgetUsagePercent: Float
        get() = if (budgetLimit > 0) (totalSpending / budgetLimit).coerceAtMost(1.0).toFloat() else 0f
}