package org.izolentiy.shiftentrance.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.spinner.KSpinner
import io.github.kakaocup.kakao.spinner.KSpinnerItem
import io.github.kakaocup.kakao.text.KTextView
import io.github.kakaocup.kakao.toolbar.KToolbar
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.ui.CurrencyFragment

object CurrencyScreen : KScreen<CurrencyScreen>() {

    override val layoutId: Int = R.layout.fragment_currency
    override val viewClass: Class<*> = CurrencyFragment::class.java

    val toolbar = KToolbar { withId(R.id.toolbar_detail) }

    val textViewSelectedCurrency = KTextView { withId(R.id.text_view_selected_currency) }

    val textViewBaseCurrency = KTextView { withId(R.id.text_view_base_currency) }

    val editTextSelectedCurrency = KEditText { withId(R.id.edit_text_selected_currency) }

    val editTextBaseCurrency = KEditText { withId(R.id.edit_text_base_currency) }

    val textViewRatesCount = KTextView { withId(R.id.text_view_rates_to_display) }

    val spinnerRatesCount = KSpinner(
        builder = { withId(R.id.spinner_count) },
        itemTypeBuilder = { itemType(::KSpinnerItem) }
    )

}