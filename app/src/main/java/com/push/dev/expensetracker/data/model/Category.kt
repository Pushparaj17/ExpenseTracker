package com.push.dev.expensetracker.data.model

import androidx.compose.ui.graphics.Color

enum class Category(
    val displayName: String,
    val emoji: String,
    val colorValue: Long
) {
    FOOD("Food", "🍔", 0xFFEF5350),
    TRAVEL("Travel", "✈️", 0xFF42A5F5),
    SHOPPING("Shopping", "🛍️", 0xFFAB47BC),
    BILLS("Bills", "📄", 0xFFFF7043),
    ENTERTAINMENT("Entertainment", "🎬", 0xFF26A69A),
    OTHER("Other", "💡", 0xFF78909C);

    val color: Color get() = Color(colorValue)
}