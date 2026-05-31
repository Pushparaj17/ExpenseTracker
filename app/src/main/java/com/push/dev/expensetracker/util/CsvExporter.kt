package com.push.dev.expensetracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.push.dev.expensetracker.data.model.Expense
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CsvExporter {

    fun export(context: Context, expenses: List<Expense>): Uri {
        val csv = buildString {
            appendLine("ID,Title,Amount,Category,Date,Notes")
            expenses.forEach { expense ->
                appendLine(
                    "${expense.id}," +
                    "\"${expense.title.replace("\"", "\"\"")}\"," +
                    "${expense.amount}," +
                    "${expense.category.displayName}," +
                    "${expense.date}," +
                    "\"${expense.notes.replace("\"", "\"\"")}\""
                )
            }
        }

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val file = File(context.cacheDir, "expenses_$timestamp.csv")
        file.writeText(csv)

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareIntent(context: Context, uri: Uri): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Expense Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
}