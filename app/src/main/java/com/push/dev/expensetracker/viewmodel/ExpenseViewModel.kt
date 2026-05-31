package com.push.dev.expensetracker.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.push.dev.expensetracker.data.model.Category
import com.push.dev.expensetracker.data.model.Expense
import com.push.dev.expensetracker.data.model.MonthlySummary
import com.push.dev.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val BUDGET_KEY = doublePreferencesKey("monthly_budget")
    }

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    // ─── Dashboard State ──────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    val dashboardState: StateFlow<DashboardUiState> = combine(
        _selectedMonth.flatMapLatest { month ->
            combine(
                repository.getRecentExpenses(limit = 5),
                repository.getMonthlyTotal(month),
                repository.getMonthlyExpenses(month)
            ) { recent, total, monthly -> Triple(recent, total, monthly)
            }
        },
        dataStore.data.map { it[BUDGET_KEY] ?: 0.0 }
    ) { (recent, total, monthly), budget ->
        val categorySpending = monthly
            .groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

        val highestCat = categorySpending.maxByOrNull { it.value }
        val daysInMonth = _selectedMonth.value.lengthOfMonth().toDouble()

        DashboardUiState(
            recentExpenses = recent,
            monthlyTotal = total,
            categorySpending = categorySpending,
            monthlySummary = MonthlySummary(
                totalSpending = total,
                highestCategory = highestCat?.key,
                highestCategoryAmount = highestCat?.value ?: 0.0,
                averageDailySpending = if (daysInMonth > 0) total / daysInMonth else 0.0,
                transactionCount = monthly.size,
                budgetLimit = budget
            ),
            selectedMonth = _selectedMonth.value,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    // ─── History State ────────────────────────────────────────────────────────

    private val _historySearch = MutableStateFlow("")
    private val _historyCategory = MutableStateFlow<Category?>(null)
    private val _historySortOrder = MutableStateFlow(SortOrder.DATE_DESC)

    val historyState: StateFlow<HistoryUiState> = combine(
        repository.getAllExpenses(),
        _historySearch,
        _historyCategory,
        _historySortOrder
    ) { all, query, category, sort ->
        val filtered = all
            .filter { expense ->
                (query.isBlank() ||
                    expense.title.contains(query, ignoreCase = true) ||
                    expense.notes.contains(query, ignoreCase = true)) &&
                (category == null || expense.category == category)
            }
            .let { list ->
                when (sort) {
                    SortOrder.DATE_DESC -> list.sortedByDescending { it.date }
                    SortOrder.DATE_ASC -> list.sortedBy { it.date }
                    SortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
                    SortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
                }
            }
        HistoryUiState(
            allExpenses = all,
            filteredExpenses = filtered,
            searchQuery = query,
            selectedCategory = category,
            sortOrder = sort,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    // ─── Analytics State ──────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        _selectedMonth.flatMapLatest { month ->
            repository.getMonthlyExpenses(month)
        },
        repository.getAllExpenses(),
        dataStore.data.map { it[BUDGET_KEY] ?: 0.0 }
    ) { monthly, all, budget ->
        val categorySpending = monthly
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val monthlyTrend = buildMonthlyTrend(all)
        val dailyTrend = buildDailyTrend(monthly)

        val highestCat = categorySpending.maxByOrNull { it.value }
        val daysInMonth = _selectedMonth.value.lengthOfMonth().toDouble()
        val total = monthly.sumOf { it.amount }

        AnalyticsUiState(
            categorySpending = categorySpending,
            monthlyTrend = monthlyTrend,
            dailyTrend = dailyTrend,
            monthlySummary = MonthlySummary(
                totalSpending = total,
                highestCategory = highestCat?.key,
                highestCategoryAmount = highestCat?.value ?: 0.0,
                averageDailySpending = if (daysInMonth > 0) total / daysInMonth else 0.0,
                transactionCount = monthly.size,
                budgetLimit = budget
            ),
            selectedMonth = _selectedMonth.value,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalyticsUiState()
    )

    // ─── Add / Edit State ─────────────────────────────────────────────────────

    private val _addEditState = MutableStateFlow(AddEditExpenseUiState())
    val addEditState: StateFlow<AddEditExpenseUiState> = _addEditState.asStateFlow()

    fun loadExpenseForEdit(id: Long) {
        viewModelScope.launch {
            val expense = repository.getExpenseById(id) ?: return@launch
            _addEditState.value = AddEditExpenseUiState(
                id = expense.id,
                title = expense.title,
                amount = expense.amount.toString(),
                category = expense.category,
                date = expense.date,
                notes = expense.notes,
                isEditMode = true
            )
        }
    }

    fun onTitleChange(value: String) {
        _addEditState.update { it.copy(title = value, titleError = null) }
    }

    fun onAmountChange(value: String) {
        _addEditState.update { it.copy(amount = value, amountError = null) }
    }

    fun onCategoryChange(value: Category) {
        _addEditState.update { it.copy(category = value) }
    }

    fun onDateChange(value: LocalDate) {
        _addEditState.update { it.copy(date = value) }
    }

    fun onNotesChange(value: String) {
        _addEditState.update { it.copy(notes = value) }
    }

    fun resetAddEditState() {
        _addEditState.value = AddEditExpenseUiState()
    }

    fun saveExpense() {
        val state = _addEditState.value
        val titleError = if (state.title.isBlank()) "Title is required" else null
        val amountError = when {
            state.amount.isBlank() -> "Amount is required"
            state.amount.toDoubleOrNull() == null -> "Enter a valid number"
            state.amount.toDouble() <= 0 -> "Amount must be greater than 0"
            else -> null
        }

        if (titleError != null || amountError != null) {
            _addEditState.update { it.copy(titleError = titleError, amountError = amountError) }
            return
        }

        _addEditState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val expense = Expense(
                id = state.id,
                title = state.title.trim(),
                amount = state.amount.toDouble(),
                category = state.category,
                date = state.date,
                notes = state.notes.trim()
            )
            if (state.isEditMode) repository.updateExpense(expense)
            else repository.insertExpense(expense)
            _addEditState.update { it.copy(isSaving = false, isSuccess = true) }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    // ─── History filters ──────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) { _historySearch.value = query }
    fun onCategoryFilterChange(category: Category?) { _historyCategory.value = category }
    fun onSortOrderChange(order: SortOrder) { _historySortOrder.value = order }

    // ─── Month navigation ─────────────────────────────────────────────────────

    fun selectMonth(month: YearMonth) { _selectedMonth.value = month }
    fun previousMonth() { _selectedMonth.update { it.minusMonths(1) } }
    fun nextMonth() { _selectedMonth.update { it.plusMonths(1) } }

    // ─── Budget ───────────────────────────────────────────────────────────────

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            dataStore.edit { it[BUDGET_KEY] = amount }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun buildMonthlyTrend(expenses: List<Expense>): List<Pair<String, Double>> {
        val now = YearMonth.now()
        return (5 downTo 0).map { offset ->
            val month = now.minusMonths(offset.toLong())
            val total = expenses
                .filter { YearMonth.from(it.date) == month }
                .sumOf { it.amount }
            month.month.name.take(3) to total
        }
    }

    private fun buildDailyTrend(expenses: List<Expense>): List<Pair<String, Double>> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val total = expenses
                .filter { it.date == day }
                .sumOf { it.amount }
            "${day.dayOfMonth}/${day.monthValue}" to total
        }
    }

    fun getMonthlySummary(): MonthlySummary = dashboardState.value.monthlySummary

    fun getCategoryWiseExpense(): Map<Category, Double> = dashboardState.value.categorySpending
}