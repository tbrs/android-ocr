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
package edu.sfsu.cs.orange.ocr

import java.io.File
import java.util.ArrayList

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log

import com.googlecode.leptonica.android.ReadFile
import com.googlecode.tesseract.android.ResultIterator
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel

/**
 * Class to send OCR requests to the OCR engine in a separate thread, send a success/failure message,
 * and dismiss the indeterminate progress dialog box. Used for non-continuous mode OCR only.
 */
internal class OcrRecognizeAsyncTask(
        //  private static final boolean PERFORM_FISHER_THRESHOLDING = false;
        //  private static final boolean PERFORM_OTSU_THRESHOLDING = false;
        //  private static final boolean PERFORM_SOBEL_THRESHOLDING = false;

        private val activity: CaptureActivity, private val baseApi: TessBaseAPI?, private val data: ByteArray, private val width: Int, private val height: Int) : AsyncTask<Void, Void, Boolean>() {
    private var ocrResult: OcrResult? = null
    private var timeRequired: Long = 0

    override fun doInBackground(vararg arg0: Void): Boolean {
        val start = System.currentTimeMillis()
        val bitmap = activity.getCameraManager().buildLuminanceSource(data, width, height).renderCroppedGreyscaleBitmap()
        val textResult: String?

        //      if (PERFORM_FISHER_THRESHOLDING) {
        //        Pix thresholdedImage = Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bitmap), 48, 48, 0.1F, 2.5F);
        //        Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + bitmap.getWidth() + "x" + bitmap.getHeight());
        //        bitmap = WriteFile.writeBitmap(thresholdedImage);
        //      }
        //      if (PERFORM_OTSU_THRESHOLDING) {
        //        Pix thresholdedImage = Binarize.otsuAdaptiveThreshold(ReadFile.readBitmap(bitmap), 48, 48, 9, 9, 0.1F);
        //        Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + bitmap.getWidth() + "x" + bitmap.getHeight());
        //        bitmap = WriteFile.writeBitmap(thresholdedImage);
        //      }
        //      if (PERFORM_SOBEL_THRESHOLDING) {
        //        Pix thresholdedImage = Thresholder.sobelEdgeThreshold(ReadFile.readBitmap(bitmap), 64);
        //        Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + bitmap.getWidth() + "x" + bitmap.getHeight());
        //        bitmap = WriteFile.writeBitmap(thresholdedImage);
        //      }

        try {
            baseApi!!.setImage(ReadFile.readBitmap(bitmap))
            textResult = baseApi.utF8Text
            timeRequired = System.currentTimeMillis() - start

            // Check for failure to recognize text
            if (textResult == null || textResult == "") {
                return false
            }
            ocrResult = OcrResult()
            ocrResult!!.wordConfidences = baseApi.wordConfidences()
            ocrResult!!.meanConfidence = baseApi.meanConfidence()
            ocrResult!!.regionBoundingBoxes = baseApi.regions.boxRects
            ocrResult!!.textlineBoundingBoxes = baseApi.textlines.boxRects
            ocrResult!!.wordBoundingBoxes = baseApi.words.boxRects
            ocrResult!!.stripBoundingBoxes = baseApi.strips.boxRects

            // Iterate through the results.
            val iterator = baseApi.resultIterator
            var lastBoundingBox: IntArray
            val charBoxes = ArrayList<Rect>()
            iterator.begin()
            do {
                lastBoundingBox = iterator.getBoundingBox(PageIteratorLevel.RIL_SYMBOL)
                val lastRectBox = Rect(lastBoundingBox[0], lastBoundingBox[1],
                        lastBoundingBox[2], lastBoundingBox[3])
                charBoxes.add(lastRectBox)
            } while (iterator.next(PageIteratorLevel.RIL_SYMBOL))
            iterator.delete()
            ocrResult!!.characterBoundingBoxes = charBoxes

        } catch (e: RuntimeException) {
            Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.")
            e.printStackTrace()
            try {
                baseApi!!.clear()
                activity.stopHandler()
            } catch (e1: NullPointerException) {
                // Continue
            }

            return false
        }

        timeRequired = System.currentTimeMillis() - start
        ocrResult!!.bitmap = bitmap
        ocrResult!!.text = textResult
        ocrResult!!.recognitionTimeRequired = timeRequired
        return true
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)

        val handler = activity.getHandler()
        if (handler != null) {
            // Send results for single-shot mode recognition.
            if (result!!) {
                val message = Message.obtain(handler, R.id.ocr_decode_succeeded, ocrResult)
                message.sendToTarget()
            } else {
                val message = Message.obtain(handler, R.id.ocr_decode_failed, ocrResult)
                message.sendToTarget()
            }
            activity.getProgressDialog().dismiss()
        }
        baseApi?.clear()
    }
}
