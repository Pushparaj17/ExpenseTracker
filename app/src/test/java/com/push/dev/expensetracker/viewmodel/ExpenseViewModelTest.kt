package com.push.dev.expensetracker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import com.push.dev.expensetracker.data.model.Category
import com.push.dev.expensetracker.data.model.Expense
import com.push.dev.expensetracker.data.repository.ExpenseRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val repository: ExpenseRepository = mockk(relaxed = true)
    private val dataStore: DataStore<Preferences> = mockk(relaxed = true)
    private lateinit var viewModel: ExpenseViewModel

    private val testExpense = Expense(
        id = 1L,
        title = "Lunch",
        amount = 250.0,
        category = Category.FOOD,
        date = LocalDate.now()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { dataStore.data } returns flowOf(emptyPreferences())
        every { repository.getAllExpenses() } returns flowOf(listOf(testExpense))
        every { repository.getRecentExpenses(any()) } returns flowOf(listOf(testExpense))
        every { repository.getMonthlyExpenses(any()) } returns flowOf(listOf(testExpense))
        every { repository.getMonthlyTotal(any()) } returns flowOf(250.0)

        viewModel = ExpenseViewModel(repository, dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addExpense calls repository insertExpense with correct data`() = runTest {
        coEvery { repository.insertExpense(any()) } returns 1L

        viewModel.onTitleChange("Coffee")
        viewModel.onAmountChange("150.0")
        viewModel.onCategoryChange(Category.FOOD)
        viewModel.saveExpense()

        advanceUntilIdle()

        coVerify {
            repository.insertExpense(
                match { it.title == "Coffee" && it.amount == 150.0 && it.category == Category.FOOD }
            )
        }
    }

    @Test
    fun `saveExpense sets titleError when title is blank`() = runTest {
        viewModel.onTitleChange("")
        viewModel.onAmountChange("100.0")
        viewModel.saveExpense()

        advanceUntilIdle()

        assertNotNull(viewModel.addEditState.value.titleError)
        coVerify(exactly = 0) { repository.insertExpense(any()) }
    }

    @Test
    fun `saveExpense sets amountError when amount is invalid`() = runTest {
        viewModel.onTitleChange("Test")
        viewModel.onAmountChange("abc")
        viewModel.saveExpense()

        advanceUntilIdle()

        assertNotNull(viewModel.addEditState.value.amountError)
    }

    @Test
    fun `saveExpense sets amountError when amount is zero`() = runTest {
        viewModel.onTitleChange("Test")
        viewModel.onAmountChange("0")
        viewModel.saveExpense()

        advanceUntilIdle()

        assertNotNull(viewModel.addEditState.value.amountError)
    }

    @Test
    fun `deleteExpense calls repository deleteExpense`() = runTest {
        viewModel.deleteExpense(testExpense)
        advanceUntilIdle()
        coVerify { repository.deleteExpense(testExpense) }
    }

    @Test
    fun `updateExpense calls repository updateExpense in edit mode`() = runTest {
        coEvery { repository.getExpenseById(1L) } returns testExpense
        coEvery { repository.updateExpense(any()) } just Runs

        viewModel.loadExpenseForEdit(1L)
        advanceUntilIdle()

        assertTrue(viewModel.addEditState.value.isEditMode)

        viewModel.onAmountChange("300.0")
        viewModel.saveExpense()
        advanceUntilIdle()

        coVerify {
            repository.updateExpense(match { it.id == 1L && it.amount == 300.0 })
        }
    }

    @Test
    fun `onSearchQueryChange updates history state`() = runTest {
        viewModel.historyState.test {
            awaitItem() // initial

            viewModel.onSearchQueryChange("Lunch")
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals("Lunch", updated.searchQuery)
        }
    }

    @Test
    fun `onCategoryFilterChange updates history filter`() = runTest {
        viewModel.historyState.test {
            awaitItem() // initial
            viewModel.onCategoryFilterChange(Category.FOOD)
            advanceUntilIdle()
            val updated = awaitItem()
            assertEquals(Category.FOOD, updated.selectedCategory)
        }
    }

    @Test
    fun `selectMonth updates selected month`() = runTest {
        val targetMonth = YearMonth.of(2024, 6)
        viewModel.selectMonth(targetMonth)
        advanceUntilIdle()
        // Verify through dashboard state update
        verify(atLeast = 1) { repository.getMonthlyExpenses(any()) }
    }

    @Test
    fun `getMonthlySummary returns summary from dashboard state`() = runTest {
        advanceUntilIdle()
        val summary = viewModel.getMonthlySummary()
        assertNotNull(summary)
    }

    @Test
    fun `getCategoryWiseExpense returns category map from dashboard state`() = runTest {
        advanceUntilIdle()
        val categoryMap = viewModel.getCategoryWiseExpense()
        assertNotNull(categoryMap)
    }
}