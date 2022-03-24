package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import org.izolentiy.shiftentrance.DETAIL_MARKER_OFFSET_MULTIPLIER
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.toStringDate

class DetailMarkerView(
    context: Context?,
    attrs: AttributeSet? = null,
    layoutResource: Int = R.layout.view_marker_detail
) : MarkerView(context, layoutResource) {

    private val textViewDate: TextView by lazy { findViewById(R.id.text_view_date) }
    private val textViewValue: TextView by lazy { findViewById(R.id.text_view_value) }

    /**
     * There we point where to draw marker view relative to highlighted point
     * In our case centered horizontally and a slightly above the dot
     */
    override fun getOffset(): MPPointF {
        val multiplier = DETAIL_MARKER_OFFSET_MULTIPLIER
        return MPPointF((-width / 2).toFloat(), -height.toFloat() * multiplier)
    }

    /**
     * There we assign new values to marker view children
     */
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            textViewDate.text = e?.x?.toStringDate()
            textViewValue.text = e?.y.toString()
        } catch (error: Throwable) {
            Log.e(TAG, "refreshContent: ERROR")
        }
        super.refreshContent(e, highlight)
    }

    companion object {
        private val TAG = "${DetailMarkerView::class.java.simpleName}_TAG"
    }

}

class RoundMarkerView(
    context: Context?,
    attrs: AttributeSet? = null,
    layoutResource: Int = R.layout.view_marker_round
) : MarkerView(context, layoutResource)