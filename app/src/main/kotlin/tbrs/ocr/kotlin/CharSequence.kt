/*
 * Copyright 2019 tbrs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tbrs.ocr.kotlin

import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle

/**
 * Given either a Spannable String or a regular String and a token, apply
 * the given CharacterStyle to the span between the tokens.
 *
 * NOTE: This method was adapted from:
 * http://www.androidengineer.com/2010/08/easy-method-for-formatting-android.html
 *
 * For example, `setSpanBetweenTokens("Hello ##world##!", "##", new
 * ForegroundColorSpan(0xFFFF0000));` will return a CharSequence `"Hello world!"` with `world` in red.
 */
fun CharSequence.setSpanBetweenTokens(token: String, vararg cs: CharacterStyle): CharSequence {
    var text = this
    // Start and end refer to the points where the span will apply.
    val tokenLen = token.length
    val start = text.toString().indexOf(token) + tokenLen
    val end = text.toString().indexOf(token, start)

    if (start > -1 && end > -1) {
        // Copy the spannable string to a mutable spannable string.
        val ssb = SpannableStringBuilder(text)
        for (c in cs)
            ssb.setSpan(c, start, end, 0)
        text = ssb
    }
    return text
}