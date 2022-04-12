package org.izolentiy.shiftentrance.ui

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import org.izolentiy.shiftentrance.FocusedEditTextId
import org.izolentiy.shiftentrance.databinding.FragmentCurrencyBinding

class InputWatcher(
    private val focusedEditTextId: FocusedEditTextId,
    private val editText: EditText,
    private val viewModel: CurrencyViewModel,
    private val rate: Double,
    private val binding: FragmentCurrencyBinding
) : TextWatcher {

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(text: Editable?) {
        if (focusedEditTextId.value == editText.id) {
            editText.removeTextChangedListener(this)

            var input = text.toString()
            val expAfterDot = input.substringAfter('.')
            val expBeforeDot = input.substringBefore('.')
            val expContainsDec = input.contains('.')

            Log.d(TAG, "watcher: input $input")
            Log.d(TAG, "watcher: beforeDot $expBeforeDot")
            Log.d(TAG, "watcher: afterDot ${if (expContainsDec) expAfterDot else ""}")

            val calculate = {
                val value = input.toFloat()
                viewModel.baseSum.value =
                    if (editText.id == binding.editTextBaseCurrency.id) value
                    else value * rate.toFloat()
            }

            when {
                input.isBlank() -> {
                    viewModel.baseSum.value = 0.0f
                }
                input.startsWith('.') -> {
                    input = "0.$expAfterDot"
                    editText.apply {
                        setText(input)
                        setSelection(input.length)
                    }
                }
                (expBeforeDot.length > 1 && expBeforeDot.startsWith('0')) -> {
                    val nonZeroDigIndex = expBeforeDot.indexOfFirst { it != '0' }

                    // If int part is made of zeros, take only one
                    val intPart = if (nonZeroDigIndex == -1) "0"
                    // Else remove zeros before first non zero digit
                    else expBeforeDot.substring(nonZeroDigIndex, expBeforeDot.length)

                    input = input.replace(expBeforeDot, intPart)
                    editText.apply {
                        setText(input)
                        setSelection(intPart.length)
                    }
                    calculate.invoke()
                }
                (expContainsDec && expAfterDot.length > 2) -> {
                    val sel = editText.selectionStart
                    val decPart = expAfterDot.substring(0, 2)

                    input = input.replace(expAfterDot, decPart)
                    editText.apply {
                        setText(input)
                        setSelection(Integer.min(sel, input.length))
                    }
                    calculate.invoke()
                }
                else -> calculate.invoke()
            }

            editText.addTextChangedListener(this)
        }
    }

    companion object {
        private val TAG = "${InputWatcher::class.simpleName}_TAG"
    }

}