package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart

class ModifiedLineChart(context: Context, attrs: AttributeSet) : LineChart(context, attrs) {
    var detailMarkerView: DetailMarkerView? = null
    var roundMarkerView: RoundMarkerView? = null

    override fun drawMarkers(canvas: Canvas?) = with(this) {
        // Check if there is no marker view or drawing marker is disabled
        if (!isDrawMarkersEnabled || isAllMarkersNull() || !valuesToHighlight()) return
        val detailMarkerView = detailMarkerView!!
        val roundMarkerView = roundMarkerView!!

        for (i in mIndicesToHighlight.indices) {
            val highlight = mIndicesToHighlight[i]
            val dataSet = mData.getDataSetByIndex(highlight.dataSetIndex)

            val entry = mData.getEntryForHighlight(mIndicesToHighlight[i])
            val index = dataSet.getEntryIndex(entry)

            if (index > dataSet.entryCount * mAnimator.phaseX) continue

            val pos = getMarkerPosition(highlight)
            if (!viewPortHandler.isInBounds(pos[0], pos[1])) continue

            // Update and draw the content
            with(detailMarkerView) {
                // Draw detail marker only if there is enough space by Y
                if (pos[1] > this.height * DETAIL_MARKER_OFFSET_MULTIPLIER) {
                    refreshContent(entry, highlight)
                    draw(canvas, pos[0], pos[1])
                }
            }

            with(roundMarkerView) {
                val posX = pos[0] - (this.width / 2)
                val posY = pos[1] - (this.height / 2)

                refreshContent(entry, highlight)
                draw(canvas, posX, posY)
            }
        }
    }

    private fun isAllMarkersNull() = detailMarkerView == null && roundMarkerView == null
}