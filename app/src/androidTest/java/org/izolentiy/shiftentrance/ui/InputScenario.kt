package org.izolentiy.shiftentrance.ui

import android.view.KeyEvent
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.api.scenario.Scenario
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.edit.KEditText
import org.izolentiy.shiftentrance.screen.CurrencyScreen

class InputScenario<T : KScreen<T>>(
    private val underEdit: KEditText,
    private val dependent: KEditText,
    private val screen: KScreen<T>
) : Scenario() {

    override val steps: TestContext<Unit>.() -> Unit = {

        step("Type only '.', check it displays '0.'") {
            underEdit {
                typeText(".")
                hasText("0.")
                dependent.hasEmptyText()
                clearText()
            }
        }

        step("Type 4 decimals, check it displays only two") {
            underEdit {
                typeText("0.1234")
                hasText("0.12")
                clearText()
            }
        }

        step("Type several zeros, check it don't display zero before") {
            underEdit {
                typeText("0012")
                hasText("12")
                clearText()
            }
        }

        step("Type zero before already typed text") {
            underEdit {
                typeText("12")
                step("Move cursor at the start and type zero") {
                    screen.pressKey(KeyEvent.KEYCODE_MOVE_HOME)
                    screen.pressKey(KeyEvent.KEYCODE_0)
                }
                hasText("12")
                clearText()
            }
        }

        step("Type '.' in the middle of long int number") {
            underEdit {
                typeText("12345678")
                step("Move cursor in the middle of the text") {
                    repeat(4) {
                        screen.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
                    }
                }
                step("Type '.'") {
                    screen.pressKey(KeyEvent.KEYCODE_NUMPAD_DOT)
                }
                hasText("1234.56")
                clearText()
            }
        }

        step("Spamming with dots") {
            underEdit {
                typeText("......")
                hasText("0.")
                typeText("12....")
                hasText("0.12")
                step("Move cursor at the end of integers") {
                    repeat(3) {
                        screen.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
                    }
                }
                step("Spam with dots, check that result is expected") {
                    repeat(3) {
                        screen.pressKey(KeyEvent.KEYCODE_NUMPAD_DOT)
                    }
                    hasText("0.12")
                }
                clearText()
            }
        }

        step("Type new decimal before already typed") {
            underEdit {
                typeText("1234.56")
                step("Move cursor to the start of decimals") {
                    repeat(2) {
                        screen.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
                    }
                }
                step("Type '78' and check that result is '1234.78'") {
                    screen.pressKey(KeyEvent.KEYCODE_7)
                    screen.pressKey(KeyEvent.KEYCODE_8)
                    hasText("1234.78")
                }
                clearText()
            }
        }

        step("Remove non zero digit before zero at start") {
            underEdit {
                typeText("100020003")
                step("Move cursor to the end of '1'") {
                    repeat(8) {
                        screen.pressKey(KeyEvent.KEYCODE_DPAD_LEFT)
                    }
                }
                step("Remove '1' before '000' check result") {
                    screen.pressKey(KeyEvent.KEYCODE_DEL)
                    hasText("20003")
                }
                clearText()
            }
        }

//        step("") {
//            //        step("Type value, check dependent's text changed") {
//            //            underEdit.typeText(someValue)
//            //            dependent.hasText(calculatedValue)
//            //        }
//            //        step("Clear text field clears dependent's text field too") {
//            //            underEdit.clearText()
//            //            dependent.hasEmptyText()
//            //        }
//
//            step("Type selected, check base's text changed") {
//                CurrencyScreen.editTextSelectedCurrency.typeText("100")
//                CurrencyScreen.editTextBaseCurrency.hasText("16.67")
//            }
//            step("Clear selected text field clears base's text field too") {
//                CurrencyScreen.editTextSelectedCurrency.clearText()
//                CurrencyScreen.editTextBaseCurrency.hasEmptyText()
//            }
//            step("Type base, check selected text changed") {
//                CurrencyScreen.editTextBaseCurrency.typeText("16.67")
//                CurrencyScreen.editTextSelectedCurrency.hasText("100")
//            }
//            step("Clear base text field clears selected text field too") {
//                CurrencyScreen.editTextBaseCurrency.clearText()
//                CurrencyScreen.editTextSelectedCurrency.hasEmptyText()
//            }
//        }

    }

}