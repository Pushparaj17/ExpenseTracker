package com.push.dev.expensetracker.ui.components.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (data.isEmpty()) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1.0)
    val textPaint = android.graphics.Paint().apply {
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = 28f
    }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(200.dp)) {

        val chartLeft = 48.dp.toPx()
        val chartBottom = size.height - 32.dp.toPx()
        val chartTop = 8.dp.toPx()
        val chartWidth = size.width - chartLeft - 8.dp.toPx()
        val chartHeight = chartBottom - chartTop

        val barCount = data.size
        val barSlotWidth = chartWidth / barCount
        val barWidth = barSlotWidth * 0.55f
        val gap = (barSlotWidth - barWidth) / 2

        // Draw Y-axis grid lines
        val gridColor = labelColor.copy(alpha = 0.12f)
        listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { fraction ->
            val y = chartBottom - chartHeight * fraction
            drawLine(gridColor, Offset(chartLeft, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // Draw bars
        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value / maxValue * chartHeight * animProgress.value).toFloat()
            val left = chartLeft + index * barSlotWidth + gap
            val top = chartBottom - barHeight

            drawRoundRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Label
            drawContext.canvas.nativeCanvas.drawText(
                label,
                left + barWidth / 2,
                size.height,
                textPaint.apply { color = labelColor.toArgb() }
            )
        }

        // Y-axis label (max value)
        drawContext.canvas.nativeCanvas.drawText(
            "₹${String.format("%.0f", maxValue)}",
            0f,
            chartTop + 10f,
            textPaint.apply {
                textAlign = android.graphics.Paint.Align.LEFT
                color = labelColor.copy(alpha = 0.6f).toArgb()
                textSize = 24f
            }
        )
    }
}