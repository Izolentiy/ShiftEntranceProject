package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import org.izolentiy.shiftentrance.BASE_CURRENCY
import org.izolentiy.shiftentrance.DATE_FORMAT
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.databinding.FragmentCurrencyBinding
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.toStringDate
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


@AndroidEntryPoint
class CurrencyFragment : Fragment() {

    private var _binding: FragmentCurrencyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CurrencyViewModel by viewModels()

    private val symbols = DecimalFormatSymbols(Locale("en", "US"))
    private val displayFormat = DecimalFormat("#.##", symbols)

    private var focusedEditTextId: Int? = null

    private var charCode: String = ""
    private var rate: Double = 0.0
    private var nominal: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            rate = getDouble(RATE)
            charCode = getString(CHAR_CODE)!!
            nominal = getInt(NOMINAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val exchangeRate = rate.toFloat()

        _binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        binding.apply {
            root.setOnClickListener { view ->
                val imm: InputMethodManager? = requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(view?.windowToken, 0)
                view.clearFocus()
            }

            textViewBaseCurrency.text = BASE_CURRENCY
            textViewSelectedCurrency.text = charCode

            configureEditText(editTextBaseCurrency)
            configureEditText(editTextSelectedCurrency)

            configureLineChart(lineChartCurrency)
        }
        viewModel.baseSum.observe(viewLifecycleOwner) { base ->
            val selected = base / exchangeRate

            val selText = if (selected != 0.0f) displayFormat.format(selected) else ""
            val baseText = if (base != 0.0f) displayFormat.format(base) else ""

            when (focusedEditTextId) {
                binding.editTextSelectedCurrency.id -> {
                    binding.editTextBaseCurrency.setText(baseText)
                }
                binding.editTextBaseCurrency.id -> {
                    binding.editTextSelectedCurrency.setText(selText)
                }
            }
        }

        viewModel.loadLatestRates()
        viewModel.latestRates.observe(viewLifecycleOwner) { latestRates ->
            Log.d(TAG, "onCreateView: GIVE ME LATEST RATES!")
            binding.lineChartCurrency.apply {
                data = prepareData(latestRates)
                invalidate()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureEditText(editText: EditText) {
        editText.addTextChangedListener { text ->
            if (focusedEditTextId == editText.id) {
                if (text.isNullOrBlank()) {
                    viewModel.baseSum.value = 0.0f
                    return@addTextChangedListener
                }
                val value = text.toString().toFloat()
                viewModel.baseSum.value =
                    if (editText.id == binding.editTextBaseCurrency.id) value
                    else value * rate.toFloat()
            }
        }
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) focusedEditTextId = view.id
        }
    }

    private fun configureLineChart(lineChart: LineChart) {
        lineChart.apply {
            setExtraOffsets(0f, 12f, 12f, 12f)
            setDrawBorders(true)
            setBorderWidth(0.6f)

            description = Description().apply { text = "" }

            xAxis.apply {
                yOffset = 12f
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = value.toStringDate()
                }
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }

            axisLeft.apply {
                xOffset = 16f
                textSize = 12f
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }
            axisRight.apply {
                setDrawGridLines(false)
                setDrawAxisLine(false)
                setDrawLabels(false)
            }

            getPaint(Chart.PAINT_INFO).apply {
                textSize = 50f
                color = Color.DKGRAY
            }

            legend.apply {
                textSize = 12f
                form = Legend.LegendForm.CIRCLE
            }
        }
    }

    private fun prepareData(rateList: List<ExchangeRate>): LineData? {
        val entries = if (rateList.isEmpty()) null
        else rateList.reversed().map { rate ->
            val currency = rate.currencies.find { it.charCode == charCode }!!

            val info = "${currency.value}, ${DATE_FORMAT.format(rate.date)}"
            Log.i(TAG, "onCreateView: ExchangeRate $info")

            Entry(rate.date.time.toFloat(), currency.value.toFloat())
        }

        return if (!entries.isNullOrEmpty()) {
            val label = "$nominal $charCode  >>  $BASE_CURRENCY"
            val lineDataSet = LineDataSet(entries, label).apply {
                circleRadius = 4f
                lineWidth = 2f
                valueTextSize = 12f
                color = resources.getColor(R.color.purple_200, context?.theme)
                fillColor = color
                setCircleColor(color)
                setDrawCircles(true)
                setDrawFilled(true)
            }
            LineData(lineDataSet)
        } else null
    }

    companion object {
        const val CHAR_CODE = "char_code"
        const val RATE = "rate"
        const val NOMINAL = "nominal"

        fun newInstance(args: Bundle) = CurrencyFragment().apply { arguments = args }

        private val TAG = "${CurrencyFragment::class.java.simpleName}_TAG"
    }

}