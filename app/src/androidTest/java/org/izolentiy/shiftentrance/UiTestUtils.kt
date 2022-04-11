package org.izolentiy.shiftentrance

import androidx.annotation.StringRes
import io.github.kakaocup.kakao.common.utilities.getResourceString
import io.github.kakaocup.kakao.text.KTextView

fun KTextView.hasFormattedText(@StringRes resId: Int) {
    val regex = Regex("""%\d.\w""")
    val text = getResourceString(resId).replace(regex, "")
    containsText(text)
}