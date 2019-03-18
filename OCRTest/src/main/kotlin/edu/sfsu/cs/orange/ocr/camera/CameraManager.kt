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

package edu.sfsu.cs.orange.ocr.camera

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.os.Handler
import android.preference.PreferenceManager
import android.view.SurfaceHolder
import edu.sfsu.cs.orange.ocr.PlanarYUVLuminanceSource
import edu.sfsu.cs.orange.ocr.PreferencesActivity
import java.io.IOException

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
class CameraManager(private val context: Context) {
    private val configManager: CameraConfigurationManager
    private var camera: Camera? = null
    private var autoFocusManager: AutoFocusManager? = null
    internal var framingRect: Rect? = null
    private var framingRectInPreview: Rect? = null
    private var initialized: Boolean = false
    private var previewing: Boolean = false
    private var reverseImage: Boolean = false
    private var requestedFramingRectWidth: Int = 0
    private var requestedFramingRectHeight: Int = 0
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private val previewCallback: PreviewCallback

    init {
        this.configManager = CameraConfigurationManager(context)
        previewCallback = PreviewCallback(configManager)
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    @Synchronized
    @Throws(IOException::class)
    fun openDriver(holder: SurfaceHolder) {
        var theCamera = camera
        if (theCamera == null) {
            theCamera = Camera.open()
            if (theCamera == null) {
                throw IOException()
            }
            camera = theCamera
        }
        camera!!.setPreviewDisplay(holder)
        if (!initialized) {
            initialized = true
            configManager.initFromCameraParameters(theCamera)
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                adjustFramingRect(requestedFramingRectWidth, requestedFramingRectHeight)
                requestedFramingRectWidth = 0
                requestedFramingRectHeight = 0
            }
        }
        configManager.setDesiredCameraParameters(theCamera)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        reverseImage = prefs.getBoolean(PreferencesActivity.KEY_REVERSE_IMAGE, false)
    }

    /**
     * Closes the camera driver if still in use.
     */
    @Synchronized
    fun closeDriver() {
        if (camera != null) {
            camera!!.release()
            camera = null

            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null
            framingRectInPreview = null
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    @Synchronized
    fun startPreview() {
        val theCamera = camera
        if (theCamera != null && !previewing) {
            theCamera.startPreview()
            previewing = true
            autoFocusManager = AutoFocusManager(context, camera!!)
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    @Synchronized
    fun stopPreview() {
        if (autoFocusManager != null) {
            autoFocusManager!!.stop()
            autoFocusManager = null
        }
        if (camera != null && previewing) {
            camera!!.stopPreview()
            previewCallback.setHandler(null, 0)
            previewing = false
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    @Synchronized
    fun requestOcrDecode(handler: Handler, message: Int) {
        val theCamera = camera
        if (theCamera != null && previewing) {
            previewCallback.setHandler(handler, message)
            theCamera.setOneShotPreviewCallback(previewCallback)
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     * @param delay Time delay to send with the request
     */
    @Synchronized
    fun requestAutoFocus(delay: Long) {
        autoFocusManager!!.start(delay)
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    @Synchronized
    fun getFramingRect(): Rect? {
        if (framingRect == null) {
            if (camera == null) {
                return null
            }
            val screenResolution = configManager.screenResolution
                    ?: // Called early, before init even finished
                    return null
            var width = screenResolution.x * 3 / 5
            if (width < MIN_FRAME_WIDTH) {
                width = MIN_FRAME_WIDTH
            } else if (width > MAX_FRAME_WIDTH) {
                width = MAX_FRAME_WIDTH
            }
            var height = screenResolution.y * 1 / 5
            if (height < MIN_FRAME_HEIGHT) {
                height = MIN_FRAME_HEIGHT
            } else if (height > MAX_FRAME_HEIGHT) {
                height = MAX_FRAME_HEIGHT
            }
            val leftOffset = (screenResolution.x - width) / 2
            val topOffset = (screenResolution.y - height) / 2
            framingRect = Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
        }
        return framingRect
    }

    /**
     * Like [.getFramingRect] but coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    @Synchronized
    fun getFramingRectInPreview(): Rect? {
        if (framingRectInPreview == null) {
            val rect = Rect(getFramingRect())
            val cameraResolution = configManager.cameraResolution
            val screenResolution = configManager.screenResolution
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x
            rect.right = rect.right * cameraResolution.x / screenResolution.x
            rect.top = rect.top * cameraResolution.y / screenResolution.y
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y
            framingRectInPreview = rect
        }
        return framingRectInPreview
    }

    /**
     * Changes the size of the framing rect.
     *
     * @param deltaWidth Number of pixels to adjust the width
     * @param deltaHeight Number of pixels to adjust the height
     */
    @Synchronized
    fun adjustFramingRect(deltaWidth: Int, deltaHeight: Int) {
        var deltaWidth = deltaWidth
        var deltaHeight = deltaHeight
        if (initialized) {
            val screenResolution = configManager.screenResolution

            // Set maximum and minimum sizes
            if (framingRect!!.width() + deltaWidth > screenResolution!!.x - 4 || framingRect!!.width() + deltaWidth < 50) {
                deltaWidth = 0
            }
            if (framingRect!!.height() + deltaHeight > screenResolution.y - 4 || framingRect!!.height() + deltaHeight < 50) {
                deltaHeight = 0
            }

            val newWidth = framingRect!!.width() + deltaWidth
            val newHeight = framingRect!!.height() + deltaHeight
            val leftOffset = (screenResolution.x - newWidth) / 2
            val topOffset = (screenResolution.y - newHeight) / 2
            framingRect = Rect(leftOffset, topOffset, leftOffset + newWidth, topOffset + newHeight)
            framingRectInPreview = null
        } else {
            requestedFramingRectWidth = deltaWidth
            requestedFramingRectHeight = deltaHeight
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    fun buildLuminanceSource(data: ByteArray, width: Int, height: Int): PlanarYUVLuminanceSource? {
        val rect = getFramingRectInPreview() ?: return null
// Go ahead and assume it's YUV rather than die.
        return PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), reverseImage)
    }

    companion object {

        private val TAG = CameraManager::class.java.simpleName

        private val MIN_FRAME_WIDTH = 50 // originally 240
        private val MIN_FRAME_HEIGHT = 20 // originally 240
        private val MAX_FRAME_WIDTH = 800 // originally 480
        private val MAX_FRAME_HEIGHT = 600 // originally 360
    }

}
