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

import android.text.style.CharacterStyle
import androidx.core.text.toSpannable

/**
 * Applies a span between the first and the second occurrences of a token.
 *
 * If less than two tokens are found, no span is applied.
 *
 * NOTE: This method was adapted from:
 * http://www.androidengineer.com/2010/08/easy-method-for-formatting-android.html
 *
 * For example, `setSpanBetweenTokens("Hello ##world##!", "##", new
 * ForegroundColorSpan(0xFFFF0000));` will return a CharSequence `"Hello world!"` with `world` in red.
 */
fun CharSequence.setSpanBetweenTokens(token: String, vararg styles: CharacterStyle): CharSequence {
    val start = indexOf(token).let { if (it == -1) it else it + token.length }
    if (start == -1) return this
    val end = indexOf(token, start)
    if (end == -1) return this
    return toSpannable().apply { for (style in styles) setSpan(style, start, end, 0) }
}