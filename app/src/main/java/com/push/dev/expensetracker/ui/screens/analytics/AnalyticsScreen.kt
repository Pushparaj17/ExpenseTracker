package com.push.dev.expensetracker.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.push.dev.expensetracker.ui.components.*
import com.push.dev.expensetracker.ui.components.charts.BarChart
import com.push.dev.expensetracker.ui.components.charts.LineChart
import com.push.dev.expensetracker.ui.components.charts.PieChart
import com.push.dev.expensetracker.ui.components.charts.PieSlice
import com.push.dev.expensetracker.util.toShortString
import com.push.dev.expensetracker.viewmodel.ExpenseViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: ExpenseViewModel) {
    val state by viewModel.analyticsState.collectAsStateWithLifecycle()

    if (state.isLoading) { LoadingState(); return }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Month selector
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                    }
                    Text(
                        text = state.selectedMonth.toShortString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        enabled = state.selectedMonth < YearMonth.now()
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                }
            }
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monthly summary cards
            MonthlySummarySection(state = state)

            // Category pie chart
            if (state.categorySpending.isNotEmpty()) {
                CategoryPieSection(state = state)
            }

            // Monthly spending bar chart
            if (state.monthlyTrend.isNotEmpty()) {
                ChartCard(title = "Monthly Spending Trend") {
                    BarChart(
                        data = state.monthlyTrend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }

            // Daily expense line chart
            if (state.dailyTrend.any { it.second > 0 }) {
                ChartCard(title = "Last 7 Days") {
                    LineChart(
                        data = state.dailyTrend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }

            if (state.categorySpending.isEmpty()) {
                EmptyState(
                    icon = "📊",
                    title = "No data yet",
                    message = "Start tracking expenses to see analytics"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MonthlySummarySection(
    state: com.push.dev.expensetracker.viewmodel.AnalyticsUiState
) {
    val summary = state.monthlySummary
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Spent",
            value = "₹${String.format("%.2f", summary.totalSpending)}",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Transactions",
            value = "${summary.transactionCount}",
            subtitle = "this month",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Daily Average",
            value = "₹${String.format("%.2f", summary.averageDailySpending)}",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Top Category",
            value = summary.highestCategory?.let { "${it.emoji} ${it.displayName}" } ?: "—",
            subtitle = summary.highestCategory?.let {
                "₹${String.format("%.2f", summary.highestCategoryAmount)}"
            },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CategoryPieSection(
    state: com.push.dev.expensetracker.viewmodel.AnalyticsUiState
) {
    val total = state.categorySpending.values.sum()
    val slices = state.categorySpending.entries
        .sortedByDescending { it.value }
        .map { (cat, amt) -> PieSlice(cat.displayName, amt.toFloat(), cat.color) }

    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Category Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                    state.categorySpending.entries
                        .sortedByDescending { it.value }
                        .forEach { (cat, amt) ->
                            CategoryLegendItem(
                                category = cat,
                                amount = amt,
                                percentage = if (total > 0) (amt / total).toFloat() else 0f
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}