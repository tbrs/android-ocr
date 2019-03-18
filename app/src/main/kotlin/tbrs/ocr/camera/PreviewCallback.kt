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

package tbrs.ocr.camera

import android.hardware.Camera
import android.os.Handler
import android.util.Log

/**
 * Called when the next preview frame is received.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
internal class PreviewCallback(private val configManager: CameraConfigurationManager) : Camera.PreviewCallback {
    private var previewHandler: Handler? = null
    private var previewMessage: Int = 0

    fun setHandler(previewHandler: Handler?, previewMessage: Int) {
        this.previewHandler = previewHandler
        this.previewMessage = previewMessage
    }

    // Since we're not calling setPreviewFormat(int), the data arrives here in the YCbCr_420_SP
    // (NV21) format.
    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val cameraResolution = configManager.cameraResolution
        val thePreviewHandler = previewHandler
        if (cameraResolution != null && thePreviewHandler != null) {
            val message = thePreviewHandler.obtainMessage(previewMessage, cameraResolution.x,
                    cameraResolution.y, data)
            message.sendToTarget()
            previewHandler = null
        } else {
            Log.d(TAG, "Got preview callback, but no handler or resolution available")
        }
    }

    companion object {

        private val TAG = PreviewCallback::class.java.simpleName
    }

}
