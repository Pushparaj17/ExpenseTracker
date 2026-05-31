package com.push.dev.expensetracker.data.local.dao

import androidx.room.*
import com.push.dev.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query(
        "SELECT * FROM expenses WHERE date >= :startMs AND date <= :endMs ORDER BY date DESC"
    )
    fun getMonthlyExpenses(startMs: Long, endMs: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE title LIKE '%' || :query || '%' " +
        "OR notes LIKE '%' || :query || '%' ORDER BY date DESC"
    )
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) FROM expenses " +
        "WHERE date >= :startMs AND date <= :endMs"
    )
    fun getMonthlyTotal(startMs: Long, endMs: Long): Flow<Double>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int = 5): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE date >= :startMs AND date <= :endMs " +
        "ORDER BY date DESC"
    )
    suspend fun getMonthlyExpensesOnce(startMs: Long, endMs: Long): List<ExpenseEntity>
}