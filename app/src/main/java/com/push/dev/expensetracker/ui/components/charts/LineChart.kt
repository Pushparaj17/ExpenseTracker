package com.push.dev.expensetracker.ui.components.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (data.size < 2) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1.0)
    val textPaint = android.graphics.Paint().apply {
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = 26f
    }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(200.dp)) {

        val chartLeft = 48.dp.toPx()
        val chartBottom = size.height - 32.dp.toPx()
        val chartTop = 12.dp.toPx()
        val chartWidth = size.width - chartLeft - 8.dp.toPx()
        val chartHeight = chartBottom - chartTop

        val pointCount = data.size
        val xStep = chartWidth / (pointCount - 1)

        fun xFor(index: Int) = chartLeft + index * xStep
        fun yFor(value: Double) = (chartBottom - (value / maxValue * chartHeight)).toFloat()

        // Grid lines
        val gridColor = labelColor.copy(alpha = 0.1f)
        listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { fraction ->
            val y = chartBottom - chartHeight * fraction
            drawLine(gridColor, Offset(chartLeft, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // Build path with animation clipping
        val animatedPointCount = (animProgress.value * (pointCount - 1)).toInt()
        val partialFraction = (animProgress.value * (pointCount - 1)) - animatedPointCount

        val linePath = Path()
        val fillPath = Path()

        linePath.moveTo(xFor(0), yFor(data[0].second))
        fillPath.moveTo(xFor(0), chartBottom)
        fillPath.lineTo(xFor(0), yFor(data[0].second))

        for (i in 1..animatedPointCount.coerceAtMost(pointCount - 1)) {
            linePath.lineTo(xFor(i), yFor(data[i].second))
            fillPath.lineTo(xFor(i), yFor(data[i].second))
        }

        // Partial last segment
        if (animatedPointCount < pointCount - 1) {
            val fromX = xFor(animatedPointCount)
            val fromY = yFor(data[animatedPointCount].second)
            val toX = xFor(animatedPointCount + 1)
            val toY = yFor(data[animatedPointCount + 1].second)
            val px = fromX + (toX - fromX) * partialFraction
            val py = fromY + (toY - fromY) * partialFraction
            linePath.lineTo(px, py)
            fillPath.lineTo(px, py)
        }

        // Gradient fill
        val gradientEnd = if (animatedPointCount < pointCount - 1)
            xFor(animatedPointCount) else xFor(pointCount - 1)

        fillPath.lineTo(gradientEnd, chartBottom)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                startY = chartTop,
                endY = chartBottom
            )
        )

        // Line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dots at visible points
        for (i in 0..animatedPointCount.coerceAtMost(pointCount - 1)) {
            drawCircle(
                color = lineColor,
                radius = 5.dp.toPx(),
                center = Offset(xFor(i), yFor(data[i].second))
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(xFor(i), yFor(data[i].second))
            )
        }

        // X-axis labels
        data.forEachIndexed { index, (label, _) ->
            drawContext.canvas.nativeCanvas.drawText(
                label,
                xFor(index),
                size.height,
                textPaint.apply { color = labelColor.copy(alpha = 0.7f).toArgb() }
            )
        }
    }
}