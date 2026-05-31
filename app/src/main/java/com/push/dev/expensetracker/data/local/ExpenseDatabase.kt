package com.push.dev.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.push.dev.expensetracker.data.local.dao.ExpenseDao
import com.push.dev.expensetracker.data.local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_db"
    }
}