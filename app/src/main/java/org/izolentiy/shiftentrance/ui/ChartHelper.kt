package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import org.izolentiy.shiftentrance.*

fun configureLineChart(lineChart: LineChart) {
    lineChart.apply {
        setExtraOffsets(0f, TOP_EXTRA_OFFSET, RIGHT_EXTRA_OFFSET, BOTTOM_EXTRA_OFFSET)
        setDrawBorders(true)
        setBorderWidth(BORDER_WIDTH)

        description.isEnabled = false

        xAxis.apply {
            yOffset = Y_OFFSET
            textSize = CHART_TEXT_SIZE
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = value.toStringDate()
            }
            setLabelCount(LABEL_COUNT_X_AXIS, true)
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }

        axisLeft.apply {
            xOffset = X_OFFSET
            textSize = CHART_TEXT_SIZE
            labelCount = LABEL_COUNT_Y_AXIS
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }
        axisRight.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }

        getPaint(Chart.PAINT_INFO).apply {
            textSize = NO_DATA_TEXT_SIZE
            color = Color.DKGRAY
        }

        legend.apply {
            textSize = CHART_TEXT_SIZE
            form = Legend.LegendForm.CIRCLE
        }
    }
}

fun preparedLineData(
    context: Context, dataSet: LineDataSet, resources: Resources
): LineData = LineData(
    dataSet.apply {
        circleRadius = CIRCLE_RADIUS
        circleHoleRadius = CIRCLE_HOLE_RADIUS
        lineWidth = LINE_WIDTH
        valueTextSize = CHART_TEXT_SIZE

        highlightLineWidth = HIGHLIGHT_LINE_WIDTH
        highLightColor = resources.getColor(R.color.teal_200, context.theme)

        color = resources.getColor(R.color.purple_200, context.theme)
        fillDrawable = ResourcesCompat
            .getDrawable(resources, R.drawable.bg_chart_fill, context.theme)

        setCircleColor(color)
        setDrawValues(false)
        setDrawCircles(true)
        setDrawFilled(true)
    }
)