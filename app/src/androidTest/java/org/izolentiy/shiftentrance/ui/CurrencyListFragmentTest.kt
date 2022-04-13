package org.izolentiy.shiftentrance.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.izolentiy.shiftentrance.MainActivity
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.hasFormattedText
import org.izolentiy.shiftentrance.screen.CurrencyListScreen
import org.junit.Rule
import org.junit.Test

internal class CurrencyListFragmentTest : TestCase() {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun toolbar_has_app_name() = run {
        step("Check that toolbar title has app name") {
            CurrencyListScreen.toolbar.hasTitle(R.string.app_name)
        }
    }

    @Test
    fun snackbar_text_meets_expectation() = run {
        CurrencyListScreen {
            step("Check that snackbar is displayed") {
                snackbar.isDisplayed()
            }
            step("Check currency list size") {
                if (currencyList.getSize() > 0) {
                    snackbar.text.hasFormattedText(R.string.data_loaded)
                } else {
                    snackbar {
                        text.hasText(R.string.unable_to_download)
                        action.click()
                        text.hasText(R.string.empty_data)
                    }
                }
            }
        }
    }

    @Test
    fun snackbar_after_swipe_refresh_show_error() = run {
        CurrencyListScreen {
            step("Swipe to refresh") {
                swipeRefreshLayout.swipeDown()
            }
            step("Check snackbar text") {
                snackbar {
                    isDisplayed()
                    text.hasText(R.string.unable_to_download)
                    action.click()
                    text.hasFormattedText(R.string.local_data_loaded)
                }
            }
        }
    }

}