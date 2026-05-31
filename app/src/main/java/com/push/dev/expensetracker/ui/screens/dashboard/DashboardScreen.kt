package com.push.dev.expensetracker.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.push.dev.expensetracker.data.model.Category
import com.push.dev.expensetracker.ui.components.*
import com.push.dev.expensetracker.ui.components.charts.PieChart
import com.push.dev.expensetracker.ui.components.charts.PieSlice
import com.push.dev.expensetracker.util.toDisplayString
import com.push.dev.expensetracker.util.toShortString
import com.push.dev.expensetracker.viewmodel.ExpenseViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Set Monthly Budget") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Budget Amount (₹)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    budgetInput.toDoubleOrNull()?.let { viewModel.setBudget(it) }
                    showBudgetDialog = false
                    budgetInput = ""
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (state.isLoading) { LoadingState(); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month selector
        item {
            MonthSelector(
                selectedMonth = state.selectedMonth,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )
        }

        // Budget alert banner
        if (state.monthlySummary.isBudgetExceeded) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Budget exceeded! ₹${String.format("%.2f", state.monthlySummary.totalSpending)} / ₹${String.format("%.2f", state.monthlySummary.budgetLimit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Summary cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "This Month",
                    value = "₹${String.format("%.2f", state.monthlyTotal)}",
                    subtitle = "${state.monthlySummary.transactionCount} transactions",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Daily Avg",
                    value = "₹${String.format("%.2f", state.monthlySummary.averageDailySpending)}",
                    subtitle = "per day",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Budget progress
        if (state.monthlySummary.budgetLimit > 0) {
            item {
                BudgetProgressCard(
                    spent = state.monthlySummary.totalSpending,
                    budget = state.monthlySummary.budgetLimit,
                    progress = state.monthlySummary.budgetUsagePercent,
                    onSetBudget = { showBudgetDialog = true }
                )
            }
        } else {
            item {
                OutlinedButton(
                    onClick = { showBudgetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set Monthly Budget")
                }
            }
        }

        // Category spending
        if (state.categorySpending.isNotEmpty()) {
            item {
                SectionHeader(title = "Spending by Category")
                CategorySpendingSection(spending = state.categorySpending)
            }
        }

        // Recent expenses
        item { SectionHeader(title = "Recent Expenses") }

        if (state.recentExpenses.isEmpty()) {
            item { EmptyState() }
        } else {
            items(state.recentExpenses, key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    onEdit = onEditExpense,
                    onDelete = { viewModel.deleteExpense(it) }
                )
            }
        }
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = selectedMonth.toShortString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = onNext,
            enabled = selectedMonth < YearMonth.now()
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun BudgetProgressCard(
    spent: Double,
    budget: Double,
    progress: Float,
    onSetBudget: () -> Unit
) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Budget", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onSetBudget) { Text("Edit") }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (progress >= 1f) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "₹${String.format("%.2f", spent)} spent",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "₹${String.format("%.2f", budget)} budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategorySpendingSection(spending: Map<Category, Double>) {
    val total = spending.values.sum()
    if (total == 0.0) return

    val slices = spending.map { (cat, amt) ->
        PieSlice(cat.displayName, amt.toFloat(), cat.color)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PieChart(
            slices = slices,
            modifier = Modifier.size(160.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            spending.entries.sortedByDescending { it.value }.take(4).forEach { (cat, amt) ->
                CategoryLegendItem(
                    category = cat,
                    amount = amt,
                    percentage = (amt / total).toFloat()
                )
            }
        }
    }
}