/*
 * Copyright 2011 Robert Theis
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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Point
import android.graphics.Rect

/**
 * Encapsulates the result of OCR.
 */
class OcrResult {
    private var bitmap: Bitmap? = null
    var text: String? = null

    var wordConfidences: IntArray? = null
    var meanConfidence: Int = 0

    var regionBoundingBoxes: List<Rect>? = null
    var textlineBoundingBoxes: List<Rect>? = null
    var wordBoundingBoxes: List<Rect>? = null
    var stripBoundingBoxes: List<Rect>? = null
    var characterBoundingBoxes: List<Rect>? = null

    var timestamp: Long = 0
        private set
    var recognitionTimeRequired: Long = 0

    private var paint: Paint? = null

    // Draw bounding boxes around each word
    //    // Draw bounding boxes around each character
    //    for (int i = 0; i < characterBoundingBoxes.size(); i++) {
    //      paint.setAlpha(0xA0);
    //      paint.setColor(0xFF00FF00);
    //      paint.setStyle(Style.STROKE);
    //      paint.setStrokeWidth(3);
    //      Rect r = characterBoundingBoxes.get(i);
    //      canvas.drawRect(r, paint);
    //    }
    private val annotatedBitmap: Bitmap
        get() {
            val canvas = Canvas(bitmap!!)
            for (i in wordBoundingBoxes!!.indices) {
                paint!!.alpha = 0xFF
                paint!!.color = -0xff3301
                paint!!.style = Style.STROKE
                paint!!.strokeWidth = 2f
                val r = wordBoundingBoxes!![i]
                canvas.drawRect(r, paint!!)
            }

            return bitmap!!
        }

    val bitmapDimensions: Point
        get() = Point(bitmap!!.width, bitmap!!.height)

    constructor(bitmap: Bitmap,
                text: String,
                wordConfidences: IntArray,
                meanConfidence: Int,
                regionBoundingBoxes: List<Rect>,
                textlineBoundingBoxes: List<Rect>,
                wordBoundingBoxes: List<Rect>,
                stripBoundingBoxes: List<Rect>,
                characterBoundingBoxes: List<Rect>,
                recognitionTimeRequired: Long) {
        this.bitmap = bitmap
        this.text = text
        this.wordConfidences = wordConfidences
        this.meanConfidence = meanConfidence
        this.regionBoundingBoxes = regionBoundingBoxes
        this.textlineBoundingBoxes = textlineBoundingBoxes
        this.wordBoundingBoxes = wordBoundingBoxes
        this.stripBoundingBoxes = stripBoundingBoxes
        this.characterBoundingBoxes = characterBoundingBoxes
        this.recognitionTimeRequired = recognitionTimeRequired
        this.timestamp = System.currentTimeMillis()

        this.paint = Paint()
    }

    constructor() {
        timestamp = System.currentTimeMillis()
        this.paint = Paint()
    }

    fun getBitmap(): Bitmap {
        return annotatedBitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    override fun toString(): String {
        return "$text $meanConfidence $recognitionTimeRequired $timestamp"
    }
}
