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

import android.text.style.ForegroundColorSpan
import androidx.core.text.toSpanned
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharSequenceExtensionsTest {
    @Test
    fun `test setSpanBetweenTokens()`() {
        val span = ForegroundColorSpan(-0x10000)
        val assertSpan: (String, String, Int, Int) -> Unit = { str, token, start, end ->
            (str as CharSequence).setSpanBetweenTokens(token, span).toSpanned().apply {
                Truth.assertThat(getSpanStart(span)).isEqualTo(start)
                Truth.assertThat(getSpanEnd(span)).isEqualTo(end)
            }
        }
        val assertNoSpan: (String, String) -> Unit = { str, token -> assertSpan(str, token, -1, -1) }

        assertNoSpan("", "-")
        assertNoSpan("xxxx yyyy zzzz", "-")
        assertNoSpan("xxxx - yyyy zzzz", "-")

        assertSpan("xxxx - yyyy - zzzz", "-", 6, 12)
        assertSpan("- yyyy - zzzz", "-", 1, 7)
        assertSpan("xxxx - yyyy -", "-", 6, 12)
    }
}