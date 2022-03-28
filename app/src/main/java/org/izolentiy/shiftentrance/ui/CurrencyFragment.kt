package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint
import org.izolentiy.shiftentrance.BASE_CURRENCY
import org.izolentiy.shiftentrance.CHART_DATE_FORMAT
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.databinding.FragmentCurrencyBinding
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.Resource
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

            val spinnerItem = R.layout.spinner_text
            val arrayAdapter = ArrayAdapter
                .createFromResource(requireContext(), R.array.load_rate_count, spinnerItem)
            arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown)

            spinnerCount.adapter = arrayAdapter
            spinnerCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, pos: Int, foo: Long
                ) = viewModel.loadLatestRates(parent?.getItemAtPosition(pos).toString().toInt())

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

            configureEditText(editTextBaseCurrency)
            configureEditText(editTextSelectedCurrency)

            configureLineChart(lineChart)

            val detailMarker = DetailMarkerView(requireContext()).apply { chartView = lineChart }
            val roundMarker = RoundMarkerView(requireContext())
            lineChart.apply {
                detailMarkerView = detailMarker
                roundMarkerView = roundMarker
            }
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

        viewModel.latestRates.observe(viewLifecycleOwner) { resource ->
            handleResult(resource)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleResult(resource: Resource<List<ExchangeRate>?>) {
        val latestRates = resource.data
        var noDataText = ""
        when (resource.status) {
            Resource.Status.ERROR -> {
                binding.progressBar.visibility = View.GONE
                noDataText = getString(R.string.error_chart_data)
            }
            Resource.Status.LOADING -> {
                binding.progressBar.visibility = View.VISIBLE
                noDataText = getString(R.string.loading_chart_data)
            }
            Resource.Status.SUCCESS -> {
                binding.progressBar.visibility = View.GONE
                noDataText = getString(R.string.no_char_data)
            }
        }
        binding.lineChart.apply {
            data = prepareData(latestRates)
            setNoDataText(noDataText)
            invalidate()
        }
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

    private fun prepareData(rateList: List<ExchangeRate>?): LineData? {
        val entries = if (rateList.isNullOrEmpty()) null
        else rateList.reversed().map { rate ->
            val currency = rate.currencies.find { it.charCode == charCode }!!

            val info = "${currency.value}, ${CHART_DATE_FORMAT.format(rate.date)}"
            Log.i(TAG, "prepareData: $info")

            Entry(rate.date.time.toFloat(), currency.value.toFloat())
        }

        return if (!entries.isNullOrEmpty()) {
            val label = "$nominal $charCode  >>  $BASE_CURRENCY"
            preparedLineData(requireContext(), LineDataSet(entries, label), resources)
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