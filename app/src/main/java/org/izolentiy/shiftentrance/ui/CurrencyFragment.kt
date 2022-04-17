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
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.izolentiy.shiftentrance.BASE_CURRENCY
import org.izolentiy.shiftentrance.DISPLAY_FORMAT
import org.izolentiy.shiftentrance.FocusedEditTextId
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.databinding.FragmentCurrencyBinding
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.Resource

@AndroidEntryPoint
class CurrencyFragment : Fragment() {

    private var _binding: FragmentCurrencyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CurrencyViewModel by viewModels()
    private val focusedEditTextId = FocusedEditTextId(-1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val charCode = requireArguments().getString(CHAR_CODE)!!

        _binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        binding.apply {
            // Set toolbar title
            val arrayItem = resources.getStringArray(R.array.currency_names)
                .find { it.contains(charCode) }
            if (arrayItem != null) {
                // array item = "key:value"
                val title = arrayItem.split(":").last()
                toolbarDetail.title = title
            }

            root.setOnClickListener { view ->
                val imm: InputMethodManager? = requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(view?.windowToken, 0)
                view.clearFocus()
            }

            textViewBaseCurrency.text = BASE_CURRENCY
            textViewSelectedCurrency.text = charCode

            configureSpinner(spinnerCount)

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

        val exchangeRate = requireArguments().getDouble(RATE).toFloat()
        viewModel.baseSum.observe(viewLifecycleOwner) { base ->
            val selected = base / exchangeRate

            val selText = if (selected != 0.0f) DISPLAY_FORMAT.format(selected) else ""
            val baseText = if (base != 0.0f) DISPLAY_FORMAT.format(base) else ""

            with(binding) {
                when (focusedEditTextId.value) {
                    editTextSelectedCurrency.id ->
                        editTextBaseCurrency.setText(baseText)
                    editTextBaseCurrency.id ->
                        editTextSelectedCurrency.setText(selText)
                }
            }
        }

        viewModel.latestRates.observe(viewLifecycleOwner) { resource ->
            handleResource(resource)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleResource(resource: Resource<List<ExchangeRate>?>) = with(binding) {
        progressBar.isVisible = resource is Resource.Loading
        layoutError.root.isVisible = resource is Resource.Error

        with(layoutError) {
            if (resource is Resource.Error) {
                val errorTarget = ErrorTarget(
                    messageTarget = textViewErrorMessage,
                    detailTarget = textViewErrorDetail,
                    actionTarget = buttonRetry,
                    requireContext()
                )
                handleError(resource.error, errorTarget)
                buttonRetry.setOnClickListener {
                    val count = spinnerCount.selectedItem.toString().toInt()
                    viewModel.loadLatestRates(count)
                }
            }
        }
        lineChart.apply {
            when (resource) {
                is Resource.Loading -> {
                    data = prepareData(context, requireArguments(), null)
                    setNoDataText(getString(R.string.loading_chart_data))
                }
                is Resource.Success -> {
                    data = prepareData(context, requireArguments(), resource.data)
                    setNoDataText(getString(R.string.no_char_data))
                }
                is Resource.Error -> {
                    data = prepareData(context, requireArguments(), resource.data)
                    setNoDataText("")
                }
            }
            invalidate()
        }
    }

    private fun configureSpinner(spinner: Spinner) {
        val spinnerItem = R.layout.spinner_text
        val arrayAdapter = ArrayAdapter
            .createFromResource(requireContext(), R.array.load_rate_count, spinnerItem)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown)

        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, pos: Int, foo: Long
            ) = viewModel.loadLatestRates(parent?.getItemAtPosition(pos).toString().toInt())

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configureEditText(editText: EditText) {
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) focusedEditTextId.value = view.id
        }
        val rate = requireArguments().getDouble(RATE)
        val watcher = InputWatcher(focusedEditTextId, editText, viewModel, rate, binding)
        editText.addTextChangedListener(watcher)
    }

    companion object {
        const val CHAR_CODE = "char_code"
        const val RATE = "rate"
        const val NOMINAL = "nominal"

        fun newInstance(args: Bundle) = CurrencyFragment().apply { arguments = args }

        private val TAG = "${CurrencyFragment::class.java.simpleName}_TAG"
    }

}