/*
 * Copyright 2011 Robert Theis
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
package tbrs.ocr

import android.graphics.Point
import android.graphics.Rect

/**
 * Encapsulates text and its character/word coordinates resulting from OCR.
 */
data class OcrResultText(val text: String,
                    val wordConfidences: IntArray,
                    val bitmapDimensions: Point,
                    val regionBoundingBoxes: List<Rect>,
                    val textlineBoundingBoxes: List<Rect>,
                    val stripBoundingBoxes: List<Rect>,
                    val wordBoundingBoxes: List<Rect>)