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
import tbrs.ocr.camera.CameraManager

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the result text.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
// This constructor is used when the class is built from an XML resource.
class ViewfinderView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var cameraManager: CameraManager? = null
    /** Setting it to null removes text on next drawing pass. */
    var resultText: OcrResultText? = null
    private var words: Array<String>? = null
    private var regionBoundingBoxes: List<Rect>? = null
    private var textlineBoundingBoxes: List<Rect>? = null
    private var stripBoundingBoxes: List<Rect>? = null
    private var wordBoundingBoxes: List<Rect>? = null

    // Fields extracted to avoid allocations in onDraw().
    // Rect bounds;
    private var previewFrame: Rect? = null
    private var rect: Rect = Rect()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    @ColorInt
    private val maskColor: Int = resources.getColor(R.color.viewfinder_mask)
    @ColorInt
    private val frameColor: Int = resources.getColor(R.color.viewfinder_frame)
    @ColorInt
    private val cornerColor: Int = resources.getColor(R.color.viewfinder_corners)

    fun setCameraManager(cameraManager: CameraManager) {
        this.cameraManager = cameraManager
    }

    public override fun onDraw(canvas: Canvas) {
        val frame = cameraManager!!.framingRect ?: return
        val width = canvas.width
        val height = canvas.height

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.color = maskColor
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(0f, frame.top.toFloat(), frame.left.toFloat(), (frame.bottom + 1).toFloat(), paint)
        canvas.drawRect((frame.right + 1).toFloat(), frame.top.toFloat(), width.toFloat(), (frame.bottom + 1).toFloat(), paint)
        canvas.drawRect(0f, (frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), paint)

        // If we have an OCR result, overlay its information on the viewfinder.
        if (resultText != null) {
            // Only draw text/bounding boxes on viewfinder if it hasn't been resized since the OCR was requested.
            val bitmapSize = resultText!!.bitmapDimensions
            previewFrame = cameraManager!!.getFramingRectInPreview()
            if (bitmapSize.x == previewFrame!!.width() && bitmapSize.y == previewFrame!!.height()) {
                val scaleX = frame.width() / previewFrame!!.width().toFloat()
                val scaleY = frame.height() / previewFrame!!.height().toFloat()

                if (DRAW_REGION_BOXES) {
                    regionBoundingBoxes = resultText!!.regionBoundingBoxes
                    for (i in regionBoundingBoxes!!.indices) {
                        paint.alpha = 0xA0
                        paint.color = Color.MAGENTA
                        paint.style = Style.STROKE
                        paint.strokeWidth = 1f
                        rect = regionBoundingBoxes!![i]
                        canvas.drawRect(frame.left + rect!!.left * scaleX,
                                frame.top + rect!!.top * scaleY,
                                frame.left + rect!!.right * scaleX,
                                frame.top + rect!!.bottom * scaleY, paint)
                    }
                }

                if (DRAW_TEXTLINE_BOXES) {
                    // Draw each textline
                    textlineBoundingBoxes = resultText!!.textlineBoundingBoxes
                    paint.alpha = 0xA0
                    paint.color = Color.RED
                    paint.style = Style.STROKE
                    paint.strokeWidth = 1f
                    for (i in textlineBoundingBoxes!!.indices) {
                        rect = textlineBoundingBoxes!![i]
                        canvas.drawRect(frame.left + rect!!.left * scaleX,
                                frame.top + rect!!.top * scaleY,
                                frame.left + rect!!.right * scaleX,
                                frame.top + rect!!.bottom * scaleY, paint)
                    }
                }

                if (DRAW_STRIP_BOXES) {
                    stripBoundingBoxes = resultText!!.stripBoundingBoxes
                    paint.alpha = 0xFF
                    paint.color = Color.YELLOW
                    paint.style = Style.STROKE
                    paint.strokeWidth = 1f
                    for (i in stripBoundingBoxes!!.indices) {
                        rect = stripBoundingBoxes!![i]
                        canvas.drawRect(frame.left + rect!!.left * scaleX,
                                frame.top + rect!!.top * scaleY,
                                frame.left + rect!!.right * scaleX,
                                frame.top + rect!!.bottom * scaleY, paint)
                    }
                }

                // Split the text into words
                if (DRAW_WORD_BOXES || DRAW_WORD_TEXT) wordBoundingBoxes = resultText!!.wordBoundingBoxes

                if (DRAW_WORD_BOXES) {
                    paint.alpha = 0xFF
                    paint.color = -0xff3301
                    paint.style = Style.STROKE
                    paint.strokeWidth = 1f
                    for (i in wordBoundingBoxes!!.indices) {
                        // Draw a bounding box around the word
                        rect = wordBoundingBoxes!![i]
                        canvas.drawRect(
                                frame.left + rect!!.left * scaleX,
                                frame.top + rect!!.top * scaleY,
                                frame.left + rect!!.right * scaleX,
                                frame.top + rect!!.bottom * scaleY, paint)
                    }
                }

                if (DRAW_WORD_TEXT) {
                    words = resultText!!.text.replace("\n", " ").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val wordConfidences = resultText!!.wordConfidences
                    for (i in wordBoundingBoxes!!.indices) {
                        var isWordBlank = true
                        try {
                            if (words!![i] != "") {
                                isWordBlank = false
                            }
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            e.printStackTrace()
                        }

                        // Only draw if word has characters
                        if (!isWordBlank) {
                            // Draw a white background around each word
                            rect = wordBoundingBoxes!![i]
                            paint.color = Color.WHITE
                            paint.style = Style.FILL
                            if (DRAW_TRANSPARENT_WORD_BACKGROUNDS) {
                                // Higher confidence = more opaque, less transparent background
                                paint.alpha = wordConfidences[i] * 255 / 100
                            } else {
                                paint.alpha = 255
                            }
                            canvas.drawRect(frame.left + rect!!.left * scaleX,
                                    frame.top + rect!!.top * scaleY,
                                    frame.left + rect!!.right * scaleX,
                                    frame.top + rect!!.bottom * scaleY, paint)

                            // Draw the word in black text
                            paint.color = Color.BLACK
                            paint.alpha = 0xFF
                            paint.isAntiAlias = true
                            paint.textAlign = Align.LEFT

                            // Adjust text size to fill rect
                            paint.textSize = 100f
                            paint.textScaleX = 1.0f
                            // ask the paint for the bounding rect if it were to draw this text
                            val bounds = Rect()
                            paint.getTextBounds(words!![i], 0, words!![i].length, bounds)
                            // get the height that would have been produced
                            val h = bounds.bottom - bounds.top
                            // figure out what textSize setting would create that height of text
                            val size = rect!!.height().toFloat() / h * 100f
                            // and set it into the paint
                            paint.textSize = size
                            // Now set the scale.
                            // do calculation with scale of 1.0 (no scale)
                            paint.textScaleX = 1.0f
                            // ask the paint for the bounding rect if it were to draw this text.
                            paint.getTextBounds(words!![i], 0, words!![i].length, bounds)
                            // determine the width
                            val w = bounds.right - bounds.left
                            // calculate the baseline to use so that the entire text is visible including the descenders
                            val text_h = bounds.bottom - bounds.top
                            val baseline = bounds.bottom + (rect!!.height() - text_h) / 2
                            // determine how much to scale the width to fit the view
                            val xscale = rect!!.width().toFloat() / w
                            // set the scale for the text paint
                            paint.textScaleX = xscale
                            canvas.drawText(words!![i], frame.left + rect!!.left * scaleX, frame.top + rect!!.bottom * scaleY - baseline, paint)
                        }

                    }
                }
            }

        }
        // Draw a two pixel solid border inside the framing rect
        paint.alpha = 0
        paint.style = Style.FILL
        paint.color = frameColor
        canvas.drawRect(frame.left.toFloat(), frame.top.toFloat(), (frame.right + 1).toFloat(), (frame.top + 2).toFloat(), paint)
        canvas.drawRect(frame.left.toFloat(), (frame.top + 2).toFloat(), (frame.left + 2).toFloat(), (frame.bottom - 1).toFloat(), paint)
        canvas.drawRect((frame.right - 1).toFloat(), frame.top.toFloat(), (frame.right + 1).toFloat(), (frame.bottom - 1).toFloat(), paint)
        canvas.drawRect(frame.left.toFloat(), (frame.bottom - 1).toFloat(), (frame.right + 1).toFloat(), (frame.bottom + 1).toFloat(), paint)

        // Draw the framing rect corner UI elements
        paint.color = cornerColor
        canvas.drawRect((frame.left - 15).toFloat(), (frame.top - 15).toFloat(), (frame.left + 15).toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect((frame.left - 15).toFloat(), frame.top.toFloat(), frame.left.toFloat(), (frame.top + 15).toFloat(), paint)
        canvas.drawRect((frame.right - 15).toFloat(), (frame.top - 15).toFloat(), (frame.right + 15).toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(frame.right.toFloat(), (frame.top - 15).toFloat(), (frame.right + 15).toFloat(), (frame.top + 15).toFloat(), paint)
        canvas.drawRect((frame.left - 15).toFloat(), frame.bottom.toFloat(), (frame.left + 15).toFloat(), (frame.bottom + 15).toFloat(), paint)
        canvas.drawRect((frame.left - 15).toFloat(), (frame.bottom - 15).toFloat(), frame.left.toFloat(), frame.bottom.toFloat(), paint)
        canvas.drawRect((frame.right - 15).toFloat(), frame.bottom.toFloat(), (frame.right + 15).toFloat(), (frame.bottom + 15).toFloat(), paint)
        canvas.drawRect(frame.right.toFloat(), (frame.bottom - 15).toFloat(), (frame.right + 15).toFloat(), (frame.bottom + 15).toFloat(), paint)
    }

    fun drawViewfinder() = invalidate()

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