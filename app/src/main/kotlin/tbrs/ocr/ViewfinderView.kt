/*
 * Copyright (C) 2008 ZXing authors
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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import tbrs.ocr.camera.CameraManager

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the result text.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
// This constructor is used when the class is built from an XML resource.
class ViewfinderView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    internal var cameraManager: CameraManager? = null

    /** Setting it to null removes text on next drawing pass. */
    var resultText: OcrResultText? = null

    // Fields extracted to avoid allocations in onDraw().
    private var bounds: Rect = Rect()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    @ColorInt
    private val maskColor: Int = ResourcesCompat.getColor(resources, R.color.viewfinder_mask, null)
    @ColorInt
    private val frameColor: Int = ResourcesCompat.getColor(resources, R.color.viewfinder_frame, null)
    @ColorInt
    private val cornerColor: Int = ResourcesCompat.getColor(resources, R.color.viewfinder_corners, null)

    fun draw() = invalidate()

    public override fun onDraw(canvas: Canvas) = with(cameraManager?.framingRect) {
        this ?: return

        // Draw the exterior (i.e. outside the framing rect) darkened.
        drawOverlay(canvas, this)

        // If we have an OCR result, display its information on the viewfinder.
        drawOcrResult(canvas, this)

        // Draw a two pixel solid border inside the framing rect.
        paint.alpha = 0
        paint.style = Style.FILL
        paint.color = frameColor
        canvas.drawRect(left.toFloat(), top.toFloat(), (right + 1).toFloat(), (top + 2).toFloat(), paint)
        canvas.drawRect(left.toFloat(), (top + 2).toFloat(), (left + 2).toFloat(), (bottom - 1).toFloat(), paint)
        canvas.drawRect((right - 1).toFloat(), top.toFloat(), (right + 1).toFloat(), (bottom - 1).toFloat(), paint)
        canvas.drawRect(left.toFloat(), (bottom - 1).toFloat(), (right + 1).toFloat(), (bottom + 1).toFloat(), paint)

        // Draw the framing rect corner UI elements.
        paint.color = cornerColor
        canvas.drawRect((left - 15).toFloat(), (top - 15).toFloat(), (left + 15).toFloat(), top.toFloat(), paint)
        canvas.drawRect((left - 15).toFloat(), top.toFloat(), left.toFloat(), (top + 15).toFloat(), paint)
        canvas.drawRect((right - 15).toFloat(), (top - 15).toFloat(), (right + 15).toFloat(), top.toFloat(), paint)
        canvas.drawRect(right.toFloat(), (top - 15).toFloat(), (right + 15).toFloat(), (top + 15).toFloat(), paint)
        canvas.drawRect((left - 15).toFloat(), bottom.toFloat(), (left + 15).toFloat(), (bottom + 15).toFloat(), paint)
        canvas.drawRect((left - 15).toFloat(), (bottom - 15).toFloat(), left.toFloat(), bottom.toFloat(), paint)
        canvas.drawRect((right - 15).toFloat(), bottom.toFloat(), (right + 15).toFloat(), (bottom + 15).toFloat(), paint)
        canvas.drawRect(right.toFloat(), (bottom - 15).toFloat(), (right + 15).toFloat(), (bottom + 15).toFloat(), paint)
    }

    private fun drawOcrResult(canvas: Canvas, drawArea: Rect) = with(resultText) {
        this ?: return

        // Only draw text/bounding boxes on viewfinder if it hasn't been resized since the OCR was requested.
        val previewFrame = cameraManager?.getFramingRectInPreview()
        if (bitmapDimensions.x != previewFrame!!.width() || bitmapDimensions.y != previewFrame.height()) return@with

        val scaleX = drawArea.width() / previewFrame.width().toFloat()
        val scaleY = drawArea.height() / previewFrame.height().toFloat()

        val drawBox: (box: Rect) -> Unit = { box ->
            canvas.drawRect(drawArea.left + box.left * scaleX,
                    drawArea.top + box.top * scaleY,
                    drawArea.left + box.right * scaleX,
                    drawArea.top + box.bottom * scaleY, paint)
        }

        if (DRAW_REGION_BOXES) {
            paint.alpha = 0xA0
            paint.color = Color.MAGENTA
            paint.style = Style.STROKE
            paint.strokeWidth = 1f

            regionBoundingBoxes.forEach(drawBox)
        }

        if (DRAW_TEXTLINE_BOXES) {
            paint.alpha = 0xA0
            paint.color = Color.RED
            paint.style = Style.STROKE
            paint.strokeWidth = 1f

            textlineBoundingBoxes.forEach(drawBox)
        }

        if (DRAW_STRIP_BOXES) {
            paint.alpha = 0xFF
            paint.color = Color.YELLOW
            paint.style = Style.STROKE
            paint.strokeWidth = 1f

            stripBoundingBoxes.forEach(drawBox)
        }

        // Split the text into words.
        val wordBoundingBoxes: List<Rect> = if (DRAW_WORD_BOXES || DRAW_WORD_TEXT) {
            resultText!!.wordBoundingBoxes
        } else {
            emptyList()
        }
        if (DRAW_WORD_BOXES && !wordBoundingBoxes.isEmpty()) {
            paint.alpha = 0xFF
            paint.color = -0xff3301
            paint.style = Style.STROKE
            paint.strokeWidth = 1f

            // Draw a bounding box around the word.
            wordBoundingBoxes.forEach(drawBox)
        }
        if (DRAW_WORD_TEXT) {
            val words = text.replace("\n", " ").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            wordBoundingBoxes.forEachIndexed { i, box ->
                // Only draw if word has characters.
                val word = words.getOrElse(i) { "" }
                if (word.isBlank()) return@forEachIndexed

                // Draw a white background around each word.
                paint.color = Color.WHITE
                paint.style = Style.FILL
                if (DRAW_TRANSPARENT_WORD_BACKGROUNDS) {
                    // Higher confidence = more opaque, less transparent background.
                    paint.alpha = wordConfidences.getOrElse(i) { 0 } * 255 / 100
                } else {
                    paint.alpha = 255
                }
                drawBox(box)

                // Draw the word in black text.
                paint.color = Color.BLACK
                paint.alpha = 0xFF
                paint.isAntiAlias = true
                paint.textAlign = Align.LEFT

                // Adjust text size to fill rect.
                paint.textSize = 100f
                paint.textScaleX = 1.0f
                // ask the paint for the bounding rect if it were to draw this text.
                paint.getTextBounds(word, 0, word.length, bounds)
                // get the height that would have been produced.
                val h = bounds.bottom - bounds.top
                // figure out what textSize setting would create that height of text
                val size = box.height().toFloat() / h * 100f
                // and set it into the paint.
                paint.textSize = size
                // Now set the scale.
                // do calculation with scale of 1.0 (no scale).
                paint.textScaleX = 1.0f
                // ask the paint for the bounding rect if it were to draw this text.
                paint.getTextBounds(word, 0, word.length, bounds)
                // determine the width.
                val w = bounds.right - bounds.left
                // calculate the baseline to use so that the entire text is visible including the descenders.
                val text_h = bounds.bottom - bounds.top
                val baseline = bounds.bottom + (box.height() - text_h) / 2
                // determine how much to scale the width to fit the view.
                val xscale = box.width().toFloat() / w
                // set the scale for the text paint.
                paint.textScaleX = xscale
                canvas.drawText(word, drawArea.left + box.left * scaleX, drawArea.top + box.bottom * scaleY - baseline, paint)
            }
        }
    }

    private fun drawOverlay(canvas: Canvas, transparentArea: Rect) {
        val width = canvas.width
        val height = canvas.height

        paint.color = maskColor
        canvas.drawRect(0f, 0f, width.toFloat(), transparentArea.top.toFloat(), paint)
        canvas.drawRect(0f, transparentArea.top.toFloat(), transparentArea.left.toFloat(), (transparentArea.bottom + 1).toFloat(), paint)
        canvas.drawRect((transparentArea.right + 1).toFloat(), transparentArea.top.toFloat(), width.toFloat(), (transparentArea.bottom + 1).toFloat(), paint)
        canvas.drawRect(0f, (transparentArea.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), paint)
    }

    companion object {
        /** Flag to draw boxes representing the results from TessBaseAPI::GetRegions().  */
        internal val DRAW_REGION_BOXES = false

        /** Flag to draw boxes representing the results from TessBaseAPI::GetTextlines().  */
        internal val DRAW_TEXTLINE_BOXES = true

        /** Flag to draw boxes representing the results from TessBaseAPI::GetStrips().  */
        internal val DRAW_STRIP_BOXES = false

        /** Flag to draw boxes representing the results from TessBaseAPI::GetWords().  */
        internal val DRAW_WORD_BOXES = true

        /** Flag to draw word text with a background varying from transparent to opaque.  */
        internal val DRAW_TRANSPARENT_WORD_BACKGROUNDS = false

        /** Flag to draw the text of words within their respective boxes from TessBaseAPI::GetWords().  */
        internal val DRAW_WORD_TEXT = false
    }
}

/**
 * Encapsulates text and its character/word coordinates resulting from OCR.
 */
data class OcrResultText(val text: String,
                         val wordConfidences: List<Int>,
                         val bitmapDimensions: Point,
                         val regionBoundingBoxes: List<Rect>,
                         val textlineBoundingBoxes: List<Rect>,
                         val stripBoundingBoxes: List<Rect>,
                         val wordBoundingBoxes: List<Rect>)