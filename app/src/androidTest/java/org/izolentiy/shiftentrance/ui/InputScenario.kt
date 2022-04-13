package org.izolentiy.shiftentrance.ui

import android.view.KeyEvent
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.api.scenario.Scenario
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.edit.KEditText

class InputScenario<T : KScreen<T>>(
    private val underEdit: KEditText,
    private val dependent: KEditText,
    private val screen: KScreen<T>
) : Scenario() {

    public override val steps: TestContext<Unit>.() -> Unit = {

        step("Type only '.', check it displays '0.'") {
            `1`()
        }
        step("Type 4 decimals, check it displays only two") {
            `2`()
        }
        step("Type several zeros, check it don't display zero before") {
            `3`()
        }
        step("Type zero before already typed text") {
            `4`().invoke(this)
        }
        step("Type '.' in the middle of long int number") {
            `5`().invoke(this)
        }
        step("Spamming with dots") {
            `6`().invoke(this)
        }
        step("Type new decimal before already typed") {
            `7`().invoke(this)
        }
        step("Remove non zero digit before zero at start") {
            `8`().invoke(this)
        }

    }

    private fun `1`() {
        underEdit {
            typeText(".")
            hasText("0.")
            dependent.hasEmptyText()
            clearText()
        }
    }

    private fun `2`() {
        underEdit {
            typeText("0.1234")
            hasText("0.12")
            clearText()
        }
    }

    private fun `3`() {
        underEdit {
            typeText("0012")
            hasText("12")
            clearText()
        }
    }

    private fun `4`(): TestContext<Unit>.() -> Unit = {
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

    private fun `5`(): TestContext<Unit>.() -> Unit = {
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

    private fun `6`(): TestContext<Unit>.() -> Unit = {
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

    private fun `7`(): TestContext<Unit>.() -> Unit = {
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

    private fun `8`(): TestContext<Unit>.() -> Unit = {
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

}