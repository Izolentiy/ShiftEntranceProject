package org.izolentiy.shiftentrance.screen

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.swiperefresh.KSwipeRefreshLayout
import io.github.kakaocup.kakao.text.KSnackbar
import io.github.kakaocup.kakao.toolbar.KToolbar
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.ui.CurrencyListFragment

object CurrencyListScreen : KScreen<CurrencyListScreen>() {

    override val layoutId: Int = R.layout.fragment_currency_list
    override val viewClass: Class<*> = CurrencyListFragment::class.java

    val toolbar = KToolbar { withId(R.id.toolbar_list) }

    val swipeRefreshLayout = KSwipeRefreshLayout { withId(R.id.swipe_refresh_layout) }

    val currencyList = KRecyclerView(
        builder = { withId(R.id.recycler_view_currencies) },
        itemTypeBuilder = { itemType(::CurrencyItem) }
    )

    val snackbar = KSnackbar()

}