package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.Chart
import dagger.hilt.android.AndroidEntryPoint
import org.izolentiy.shiftentrance.BASE_CURRENCY
import org.izolentiy.shiftentrance.databinding.FragmentCurrencyBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val exchangeRate = arguments?.getDouble(RATE)!!.toFloat()

        _binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        binding.apply {
            root.setOnClickListener { view ->
                val imm: InputMethodManager? = requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(view?.windowToken, 0)
                view.clearFocus()
            }

            textViewBaseCurrency.text = BASE_CURRENCY
            textViewSelectedCurrency.text = arguments?.getString(CHAR_CODE)

            configureEditText(editTextBaseCurrency)
            configureEditText(editTextSelectedCurrency)

            lineChartCurrency.getPaint(Chart.PAINT_INFO).apply {
                textSize = 50f  // TODO: Remake later on SP from pixels
                color = Color.DKGRAY
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
                    else value * arguments?.getDouble(RATE)!!.toFloat()
            }
        }
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) focusedEditTextId = view.id
        }
    }

    companion object {
        const val CHAR_CODE = "char_code"
        const val RATE = "rate"

        fun newInstance(args: Bundle) = CurrencyFragment().apply { arguments = args }

        private val TAG = "${CurrencyFragment::class.java.simpleName}_TAG"
    }

}