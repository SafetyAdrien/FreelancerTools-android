package com.freelancertools.app.ui.finances

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.theme.SuccessGreen

/** Simple stacked bar chart: income stacked above expense, one bar per day of the month. */
@Composable
fun MonthlyBarChart(bars: List<DayBar>, modifier: Modifier = Modifier) {
    val incomeColor = SuccessGreen
    val expenseColor = MaterialTheme.colorScheme.error
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        if (bars.isEmpty()) return@Canvas
        val maxTotal = bars.maxOf { it.income + it.expense }.coerceAtLeast(1.0)
        val slotWidth = size.width / bars.size
        val barWidth = (slotWidth * 0.55f).coerceAtLeast(1f)

        bars.forEachIndexed { index, bar ->
            val x = index * slotWidth + (slotWidth - barWidth) / 2f
            drawTrack(x, barWidth, trackColor)

            val incomeHeight = (bar.income / maxTotal * size.height).toFloat()
            val expenseHeight = (bar.expense / maxTotal * size.height).toFloat()

            if (expenseHeight > 0f) {
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(x, size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                )
            }
            if (incomeHeight > 0f) {
                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(x, size.height - expenseHeight - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                )
            }
        }
    }
}

private fun DrawScope.drawTrack(x: Float, width: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, size.height - 2.dp.toPx()),
        size = Size(width, 2.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
    )
}
