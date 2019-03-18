/*
 * Copyright (C) 2008 ZXing authors
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
package edu.sfsu.cs.orange.ocr

import edu.sfsu.cs.orange.ocr.R
import edu.sfsu.cs.orange.ocr.camera.CameraManager

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.View

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the result text.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
class ViewfinderView// This constructor is used when the class is built from an XML resource.
(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var cameraManager: CameraManager? = null
    private val paint: Paint
    private val maskColor: Int
    private val frameColor: Int
    private val cornerColor: Int
    private var resultText: OcrResultText? = null
    private var words: Array<String>? = null
    private var regionBoundingBoxes: List<Rect>? = null
    private var textlineBoundingBoxes: List<Rect>? = null
    private var stripBoundingBoxes: List<Rect>? = null
    private var wordBoundingBoxes: List<Rect>? = null
    private val characterBoundingBoxes: List<Rect>? = null
    //  Rect bounds;
    private var previewFrame: Rect? = null
    private var rect: Rect? = null

    init {

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val resources = resources
        maskColor = resources.getColor(R.color.viewfinder_mask)
        frameColor = resources.getColor(R.color.viewfinder_frame)
        cornerColor = resources.getColor(R.color.viewfinder_corners)

        //    bounds = new Rect();
        previewFrame = Rect()
        rect = Rect()
    }

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

                if (DRAW_WORD_BOXES || DRAW_WORD_TEXT) {
                    // Split the text into words
                    wordBoundingBoxes = resultText!!.wordBoundingBoxes
                    //      for (String w : words) {
                    //        Log.e("ViewfinderView", "word: " + w);
                    //      }
                    //Log.d("ViewfinderView", "There are " + words.length + " words in the string array.");
                    //Log.d("ViewfinderView", "There are " + wordBoundingBoxes.size() + " words with bounding boxes.");
                }

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

                //        if (DRAW_CHARACTER_BOXES || DRAW_CHARACTER_TEXT) {
                //          characterBoundingBoxes = resultText.getCharacterBoundingBoxes();
                //        }
                //
                //        if (DRAW_CHARACTER_BOXES) {
                //          // Draw bounding boxes around each character
                //          paint.setAlpha(0xA0);
                //          paint.setColor(0xFF00FF00);
                //          paint.setStyle(Style.STROKE);
                //          paint.setStrokeWidth(1);
                //          for (int c = 0; c < characterBoundingBoxes.size(); c++) {
                //            Rect characterRect = characterBoundingBoxes.get(c);
                //            canvas.drawRect(frame.left + characterRect.left * scaleX,
                //                frame.top + characterRect.top * scaleY,
                //                frame.left + characterRect.right * scaleX,
                //                frame.top + characterRect.bottom * scaleY, paint);
                //          }
                //        }
                //
                //        if (DRAW_CHARACTER_TEXT) {
                //          // Draw letters individually
                //          for (int i = 0; i < characterBoundingBoxes.size(); i++) {
                //            Rect r = characterBoundingBoxes.get(i);
                //
                //            // Draw a white background for every letter
                //            int meanConfidence = resultText.getMeanConfidence();
                //            paint.setColor(Color.WHITE);
                //            paint.setAlpha(meanConfidence * (255 / 100));
                //            paint.setStyle(Style.FILL);
                //            canvas.drawRect(frame.left + r.left * scaleX,
                //                frame.top + r.top * scaleY,
                //                frame.left + r.right * scaleX,
                //                frame.top + r.bottom * scaleY, paint);
                //
                //            // Draw each letter, in black
                //            paint.setColor(Color.BLACK);
                //            paint.setAlpha(0xFF);
                //            paint.setAntiAlias(true);
                //            paint.setTextAlign(Align.LEFT);
                //            String letter = "";
                //            try {
                //              char c = resultText.getText().replace("\n","").replace(" ", "").charAt(i);
                //              letter = Character.toString(c);
                //
                //              if (!letter.equals("-") && !letter.equals("_")) {
                //
                //                // Adjust text size to fill rect
                //                paint.setTextSize(100);
                //                paint.setTextScaleX(1.0f);
                //
                //                // ask the paint for the bounding rect if it were to draw this text
                //                Rect bounds = new Rect();
                //                paint.getTextBounds(letter, 0, letter.length(), bounds);
                //
                //                // get the height that would have been produced
                //                int h = bounds.bottom - bounds.top;
                //
                //                // figure out what textSize setting would create that height of text
                //                float size  = (((float)(r.height())/h)*100f);
                //
                //                // and set it into the paint
                //                paint.setTextSize(size);
                //
                //                // Draw the text as is. We don't really need to set the text scale, because the dimensions
                //                // of the Rect should already be suited for drawing our letter.
                //                canvas.drawText(letter, frame.left + r.left * scaleX, frame.top + r.bottom * scaleY, paint);
                //              }
                //            } catch (StringIndexOutOfBoundsException e) {
                //              e.printStackTrace();
                //            } catch (Exception e) {
                //              e.printStackTrace();
                //            }
                //          }
                //        }
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


        // Request another update at the animation interval, but don't repaint the entire viewfinder mask.
        //postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }

    fun drawViewfinder() {
        invalidate()
    }

    /**
     * Adds the given OCR results for drawing to the view.
     *
     * @param text Object containing OCR-derived text and corresponding data.
     */
    fun addResultText(text: OcrResultText) {
        resultText = text
    }

    /**
     * Nullifies OCR text to remove it at the next onDraw() drawing.
     */
    fun removeResultText() {
        resultText = null
    }

    companion object {
        //private static final long ANIMATION_DELAY = 80L;

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

        /** Flag to draw boxes representing the results from TessBaseAPI::GetCharacters().  */
        internal val DRAW_CHARACTER_BOXES = false

        /** Flag to draw the text of words within their respective boxes from TessBaseAPI::GetWords().  */
        internal val DRAW_WORD_TEXT = false

        /** Flag to draw each character in its respective box from TessBaseAPI::GetCharacters().  */
        internal val DRAW_CHARACTER_TEXT = false
    }
}
