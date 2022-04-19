package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.os.Bundle
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.model.ExchangeRate

fun configureLineChart(lineChart: LineChart) {
    lineChart.apply {
        setExtraOffsets(0f, TOP_EXTRA_OFFSET, RIGHT_EXTRA_OFFSET, BOTTOM_EXTRA_OFFSET)
        setDrawBorders(true)
        setBorderWidth(BORDER_WIDTH)
        setBorderColor(resolveColor(context, R.attr.chartTextColor))

        description.isEnabled = false

        xAxis.apply {
            yOffset = Y_OFFSET
            textSize = CHART_TEXT_SIZE
            textColor = resolveColor(context, R.attr.chartTextColor)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = value
                    .toStringDate(resources.configuration.locales.get(0))
            }
            setLabelCount(LABEL_COUNT_X_AXIS, true)
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }

        axisLeft.apply {
            xOffset = X_OFFSET
            textSize = CHART_TEXT_SIZE
            textColor = resolveColor(context, R.attr.chartTextColor)
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
            color = resolveColor(context, R.attr.chartTextColor)
        }

        legend.apply {
            textSize = CHART_TEXT_SIZE
            textColor = resolveColor(context, R.attr.chartTextColor)
            form = Legend.LegendForm.CIRCLE
        }
    }
}

fun preparedLineData(
    context: Context, dataSet: LineDataSet
): LineData = LineData(
    dataSet.apply {
        circleRadius = CIRCLE_RADIUS
        circleHoleRadius = CIRCLE_HOLE_RADIUS
        lineWidth = LINE_WIDTH
        valueTextSize = CHART_TEXT_SIZE

        circleHoleColor = resolveColor(context, R.attr.circleHoleColor)
        color = resolveColor(context, R.attr.lineColor)
        fillDrawable = resolveDrawable(context, R.attr.fillBackground)

        setCircleColor(color)
        setDrawValues(false)
        setDrawCircles(true)
        setDrawFilled(true)
        setDrawHighlightIndicators(false)
    }
)

fun prepareData(context: Context, args: Bundle, rateList: List<ExchangeRate>?): LineData? {
    val charCode = args.getString(CurrencyFragment.CHAR_CODE)
    val nominal = args.getInt(CurrencyFragment.NOMINAL)
    val entries = if (rateList.isNullOrEmpty()) null
    else rateList.reversed().map { rate ->
        val currency = rate.currencies.find { it.charCode == charCode }!!

        Entry(rate.date.time.toFloat(), currency.value.toFloat())
    }

    return if (!entries.isNullOrEmpty()) {
        val label = "$nominal $charCode  >>  $BASE_CURRENCY"
        preparedLineData(context, LineDataSet(entries, label))
    } else null
}