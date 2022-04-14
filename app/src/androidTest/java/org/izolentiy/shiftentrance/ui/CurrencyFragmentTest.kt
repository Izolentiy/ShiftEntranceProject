package org.izolentiy.shiftentrance.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.common.utilities.getResourceString
import org.izolentiy.shiftentrance.BASE_CURRENCY
import org.izolentiy.shiftentrance.MainActivity
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.screen.CurrencyItem
import org.izolentiy.shiftentrance.screen.CurrencyListScreen
import org.izolentiy.shiftentrance.screen.CurrencyScreen
import org.junit.Rule
import org.junit.Test

internal class CurrencyFragmentTest : TestCase() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun toolbar_has_currency_name() = run {
        val title = getResourceString(R.string.test_currency_name)
        step("Click on currency item") {
            CurrencyListScreen.currencyList {
                childWith<CurrencyItem> {
                    withDescendant { containsText(title) }
                }.click()
            }
        }
        step("Check that toolbar title is the currency name") {
            CurrencyScreen.toolbar.hasTitle(title)
        }
    }

    @Test
    fun text_views_has_correct_labels() = run {
        val charCode = getResourceString(R.string.test_currency_char_code)
        step("Click on currency item") {
            CurrencyListScreen.currencyList {
                childWith<CurrencyItem> {
                    withDescendant { containsText(charCode) }
                }.click()
            }
        }
        step("Check selected currency label") {
            CurrencyScreen {
                textViewBaseCurrency.hasText(BASE_CURRENCY)
                textViewSelectedCurrency.hasText(charCode)
            }
        }
    }

    @Test
    fun edit_text_works_fine() = run {
        step("Tap on any currency item in list") {
            CurrencyListScreen.currencyList.firstChild(CurrencyItem::click)
        }
        step("Check that edit text fields have hints") {
            CurrencyScreen {
                editTextBaseCurrency.hasHint(R.string.enter_sum_hint)
                editTextSelectedCurrency.hasHint(R.string.enter_sum_hint)
            }
        }
        step("Verify edit text behavior") {
            CurrencyScreen {
                scenario(
                    InputScenario(
                        editTextBaseCurrency,
                        editTextSelectedCurrency, this
                    )
                )
                scenario(
                    InputScenario(
                        editTextSelectedCurrency,
                        editTextBaseCurrency, this
                    )
                )
            }
        }
    }

}