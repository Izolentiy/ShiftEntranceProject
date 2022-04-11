package org.izolentiy.shiftentrance.screen

import android.view.View
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher
import org.izolentiy.shiftentrance.R

class CurrencyItem(parent: Matcher<View>) : KRecyclerItem<CurrencyItem>(parent) {

    val nominalCharCode = KTextView(parent) { withId(R.id.text_view_nominal_char_code) }

    val currencyName = KTextView(parent) { withId(R.id.text_view_name) }

    val exchangeRate = KTextView(parent) { withId(R.id.text_view_exchange_rate) }

}