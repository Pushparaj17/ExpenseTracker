package com.push.dev.expensetracker.ui.components.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class PieSlice(val label: String, val value: Float, val color: Color)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 60f
) {
    if (slices.isEmpty() || slices.all { it.value == 0f }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp)) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )
            }
        }
        return
    }

    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(durationMillis = 800, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier.size(160.dp)) {
        var startAngle = -90f
        slices.forEach { slice ->
            val sweepAngle = (slice.value / total) * 360f * animProgress.value
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 2f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}