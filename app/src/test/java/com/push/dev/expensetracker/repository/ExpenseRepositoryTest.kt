package com.push.dev.expensetracker.repository

import app.cash.turbine.test
import com.push.dev.expensetracker.data.local.dao.ExpenseDao
import com.push.dev.expensetracker.data.local.entity.ExpenseEntity
import com.push.dev.expensetracker.data.model.Category
import com.push.dev.expensetracker.data.model.Expense
import com.push.dev.expensetracker.data.repository.ExpenseRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ExpenseRepositoryTest {

    private val dao: ExpenseDao = mockk(relaxed = true)
    private lateinit var repository: ExpenseRepository

    private val testEntity = ExpenseEntity(
        id = 1L,
        title = "Coffee",
        amount = 120.0,
        category = "FOOD",
        date = LocalDate.now().toEpochDay() * 86_400_000L,
        notes = "Morning coffee"
    )

    private val testExpense = Expense(
        id = 1L,
        title = "Coffee",
        amount = 120.0,
        category = Category.FOOD,
        date = LocalDate.now(),
        notes = "Morning coffee"
    )

    @Before
    fun setup() {
        repository = ExpenseRepository(dao)
    }

    @Test
    fun `getAllExpenses maps entities to domain objects`() = runTest {
        every { dao.getAllExpenses() } returns flowOf(listOf(testEntity))

        repository.getAllExpenses().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(testExpense.title, result[0].title)
            assertEquals(testExpense.amount, result[0].amount, 0.01)
            assertEquals(Category.FOOD, result[0].category)
            awaitComplete()
        }
    }

    @Test
    fun `insertExpense maps domain to entity and delegates to dao`() = runTest {
        coEvery { dao.insertExpense(any()) } returns 1L

        val id = repository.insertExpense(testExpense)

        assertEquals(1L, id)
        coVerify {
            dao.insertExpense(match {
                it.title == testExpense.title && it.amount == testExpense.amount
            })
        }
    }

    @Test
    fun `updateExpense delegates to dao with correct entity`() = runTest {
        repository.updateExpense(testExpense)

        coVerify {
            dao.updateExpense(match {
                it.id == testExpense.id && it.title == testExpense.title
            })
        }
    }

    @Test
    fun `deleteExpense delegates to dao`() = runTest {
        repository.deleteExpense(testExpense)
        coVerify { dao.deleteExpense(any()) }
    }

    @Test
    fun `getExpenseById returns null when not found`() = runTest {
        coEvery { dao.getExpenseById(99L) } returns null
        val result = repository.getExpenseById(99L)
        assertNull(result)
    }

    @Test
    fun `getExpenseById maps entity to domain when found`() = runTest {
        coEvery { dao.getExpenseById(1L) } returns testEntity
        val result = repository.getExpenseById(1L)
        assertNotNull(result)
        assertEquals(testExpense.title, result!!.title)
    }

    @Test
    fun `getMonthlyTotal emits correct value`() = runTest {
        val month = YearMonth.now()
        every { dao.getMonthlyTotal(any(), any()) } returns flowOf(500.0)

        repository.getMonthlyTotal(month).test {
            assertEquals(500.0, awaitItem(), 0.01)
            awaitComplete()
        }
    }

    @Test
    fun `searchExpenses returns filtered domain objects`() = runTest {
        every { dao.searchExpenses("Coffee") } returns flowOf(listOf(testEntity))

        repository.searchExpenses("Coffee").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Coffee", result[0].title)
            awaitComplete()
        }
    }
}