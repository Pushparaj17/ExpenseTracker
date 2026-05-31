package com.push.dev.expensetracker.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.push.dev.expensetracker.data.model.Category
import com.push.dev.expensetracker.ui.components.EmptyState
import com.push.dev.expensetracker.ui.components.ExpenseCard
import com.push.dev.expensetracker.ui.components.LoadingState
import com.push.dev.expensetracker.util.CsvExporter
import com.push.dev.expensetracker.viewmodel.ExpenseViewModel
import com.push.dev.expensetracker.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(
    viewModel: ExpenseViewModel,
    onEditExpense: (Long) -> Unit
) {
    val state by viewModel.historyState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    if (state.isLoading) { LoadingState(); return }

    if (showFilterSheet) {
        FilterBottomSheet(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { viewModel.onCategoryFilterChange(it) },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense History", fontWeight = FontWeight.Bold) },
                actions = {
                    // Filter
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (state.selectedCategory != null)
                                    Badge { Text("1") }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                    // Sort
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        viewModel.onSortOrderChange(order)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (state.sortOrder == order)
                                            Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                    // Export
                    IconButton(onClick = {
                        val uri = CsvExporter.export(context, state.allExpenses)
                        context.startActivity(
                            android.content.Intent.createChooser(
                                CsvExporter.shareIntent(context, uri), "Export CSV"
                            )
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search expenses…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            // Active filters
            AnimatedVisibility(state.selectedCategory != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedCategory?.let { cat ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onCategoryFilterChange(null) },
                            label = { Text("${cat.emoji} ${cat.displayName}") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Result count
            if (state.allExpenses.isNotEmpty()) {
                Text(
                    text = "${state.filteredExpenses.size} expense${if (state.filteredExpenses.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (state.filteredExpenses.isEmpty()) {
                EmptyState(
                    icon = if (state.searchQuery.isNotEmpty() || state.selectedCategory != null) "🔍" else "📭",
                    title = if (state.searchQuery.isNotEmpty() || state.selectedCategory != null)
                        "No results found"
                    else "No expenses yet",
                    message = if (state.searchQuery.isNotEmpty() || state.selectedCategory != null)
                        "Try adjusting your search or filters"
                    else "Tap + to record your first expense",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filteredExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEdit = onEditExpense,
                            onDelete = { viewModel.deleteExpense(it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filter by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null); onDismiss() },
                label = { Text("All Categories") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Category.entries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onCategorySelected(cat); onDismiss() },
                            label = { Text("${cat.emoji} ${cat.displayName}") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}