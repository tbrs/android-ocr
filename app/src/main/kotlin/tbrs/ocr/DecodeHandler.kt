/*
 * Copyright (C) 2010 ZXing authors
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

import com.googlecode.leptonica.android.ReadFile
import com.googlecode.tesseract.android.TessBaseAPI

import tbrs.ocr.R
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log

/**
 * Class to send bitmap data for OCR.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
internal class DecodeHandler(private val activity: CaptureActivity) : Handler() {
    private var running = true
    private val baseApi: TessBaseAPI
    private val beepManager: BeepManager
    private var bitmap: Bitmap? = null
    private var timeRequired: Long = 0

    private// Check for failure to recognize text
    // Always get the word bounding boxes--we want it for annotating the bitmap after the user
    // presses the shutter button, in addition to maybe wanting to draw boxes/words during the
    // continuous mode recognition.
    //      if (ViewfinderView.DRAW_CHARACTER_BOXES || ViewfinderView.DRAW_CHARACTER_TEXT) {
    //        ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters().getBoxRects());
    //      }
    // Continue
    val ocrResult: OcrResult?
        get() {
            val ocrResult: OcrResult
            val textResult: String?
            val start = System.currentTimeMillis()

            try {
                baseApi.setImage(ReadFile.readBitmap(bitmap))
                textResult = baseApi.utF8Text
                timeRequired = System.currentTimeMillis() - start
                if (textResult == null || textResult == "") {
                    return null
                }
                ocrResult = OcrResult()
                ocrResult.wordConfidences = baseApi.wordConfidences()
                ocrResult.meanConfidence = baseApi.meanConfidence()
                if (ViewfinderView.DRAW_REGION_BOXES) {
                    val regions = baseApi.regions
                    ocrResult.regionBoundingBoxes = regions.boxRects
                    regions.recycle()
                }
                if (ViewfinderView.DRAW_TEXTLINE_BOXES) {
                    val textlines = baseApi.textlines
                    ocrResult.textlineBoundingBoxes = textlines.boxRects
                    textlines.recycle()
                }
                if (ViewfinderView.DRAW_STRIP_BOXES) {
                    val strips = baseApi.strips
                    ocrResult.stripBoundingBoxes = strips.boxRects
                    strips.recycle()
                }
                val words = baseApi.words
                ocrResult.wordBoundingBoxes = words.boxRects
                words.recycle()
            } catch (e: RuntimeException) {
                Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.")
                e.printStackTrace()
                try {
                    baseApi.clear()
                    activity.stopHandler()
                } catch (e1: NullPointerException) {
                }

                return null
            }

            timeRequired = System.currentTimeMillis() - start
            ocrResult.setBitmap(bitmap!!)
            ocrResult.text = textResult
            ocrResult.recognitionTimeRequired = timeRequired
            return ocrResult
        }

    init {
        baseApi = activity.baseApi!!
        beepManager = BeepManager(activity)
        beepManager.updatePrefs()
    }

    override fun handleMessage(message: Message) {
        if (!running) {
            return
        }
        when (message.what) {
            R.id.ocr_continuous_decode ->
                // Only request a decode if a request is not already pending.
                if (!isDecodePending) {
                    isDecodePending = true
                    ocrContinuousDecode(message.obj as ByteArray, message.arg1, message.arg2)
                }
            R.id.ocr_decode -> ocrDecode(message.obj as ByteArray, message.arg1, message.arg2)
            R.id.quit -> {
                running = false
                Looper.myLooper()!!.quit()
            }
        }
    }

    /**
     * Launch an AsyncTask to perform an OCR decode for single-shot mode.
     *
     * @param data Image data
     * @param width Image width
     * @param height Image height
     */
    private fun ocrDecode(data: ByteArray, width: Int, height: Int) {
        beepManager.playBeepSoundAndVibrate()
        activity.displayProgressDialog()

        // Launch OCR asynchronously, so we get the dialog box displayed immediately
        OcrRecognizeAsyncTask(activity, baseApi, data, width, height).execute()
    }

    /**
     * Perform an OCR decode for realtime recognition mode.
     *
     * @param data Image data
     * @param width Image width
     * @param height Image height
     */
    private fun ocrContinuousDecode(data: ByteArray, width: Int, height: Int) {
        val source = activity.cameraManager!!.buildLuminanceSource(data, width, height)
        if (source == null) {
            sendContinuousOcrFailMessage()
            return
        }
        bitmap = source!!.renderCroppedGreyscaleBitmap()

        val ocrResult = ocrResult
        val handler = activity.getHandler() ?: return

        if (ocrResult == null) {
            try {
                sendContinuousOcrFailMessage()
            } catch (e: NullPointerException) {
                activity.stopHandler()
            } finally {
                bitmap!!.recycle()
                baseApi.clear()
            }
            return
        }

        try {
            val message = Message.obtain(handler, R.id.ocr_continuous_decode_succeeded, ocrResult)
            message.sendToTarget()
        } catch (e: NullPointerException) {
            activity.stopHandler()
        } finally {
            baseApi.clear()
        }
    }

    private fun sendContinuousOcrFailMessage() {
        val handler = activity.getHandler()
        if (handler != null) {
            val message = Message.obtain(handler, R.id.ocr_continuous_decode_failed, OcrResultFailure(timeRequired))
            message.sendToTarget()
        }
    }

    companion object {
        private var isDecodePending: Boolean = false

        fun resetDecodeState() {
            isDecodePending = false
        }
    }

}












