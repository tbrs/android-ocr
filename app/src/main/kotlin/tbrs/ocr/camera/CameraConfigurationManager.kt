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


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.hardware.Camera
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager

import tbrs.ocr.PreferencesActivity

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
internal class CameraConfigurationManager(private val context: Context) {
    var screenResolution: Point? = null
        private set
    var cameraResolution: Point? = null
        private set

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    fun initFromCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        var width = display.width
        var height = display.height
        // We're landscape-only, and have apparently seen issues with display thinking it's portrait
        // when waking from sleep. If it's not landscape, assume it's mistaken and reverse them:
        if (width < height) {
            Log.i(TAG, "Display reports portrait orientation; assuming this is incorrect")
            val temp = width
            width = height
            height = temp
        }
        screenResolution = Point(width, height)
        Log.i(TAG, "Screen resolution: " + screenResolution!!)
        cameraResolution = findBestPreviewSizeValue(parameters, screenResolution!!)
        Log.i(TAG, "Camera resolution: " + cameraResolution!!)
    }

    fun setDesiredCameraParameters(camera: Camera) {
        val parameters = camera.parameters

        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.")
            return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        initializeTorch(parameters, prefs)
        var focusMode: String? = null
        if (prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true)) {
            if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, false)) {
                focusMode = findSettableValue(parameters.supportedFocusModes,
                        Camera.Parameters.FOCUS_MODE_AUTO)
            } else {
                focusMode = findSettableValue(parameters.supportedFocusModes,
                        "continuous-video", // Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO in 4.0+
                        "continuous-picture", // Camera.Paramters.FOCUS_MODE_CONTINUOUS_PICTURE in 4.0+
                        Camera.Parameters.FOCUS_MODE_AUTO)
            }
        }
        // Maybe selected auto-focus but not available, so fall through here:
        if (focusMode == null) {
            focusMode = findSettableValue(parameters.supportedFocusModes,
                    Camera.Parameters.FOCUS_MODE_MACRO,
                    "edof") // Camera.Parameters.FOCUS_MODE_EDOF in 2.2+
        }
        if (focusMode != null) {
            parameters.focusMode = focusMode
        }

        parameters.setPreviewSize(cameraResolution!!.x, cameraResolution!!.y)
        camera.parameters = parameters
    }

    fun setTorch(camera: Camera, newSetting: Boolean) {
        val parameters = camera.parameters
        doSetTorch(parameters, newSetting)
        camera.parameters = parameters
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val currentSetting = prefs.getBoolean(PreferencesActivity.KEY_TOGGLE_LIGHT, false)
        if (currentSetting != newSetting) {
            val editor = prefs.edit()
            editor.putBoolean(PreferencesActivity.KEY_TOGGLE_LIGHT, newSetting)
            editor.commit()
        }
    }

    private fun findBestPreviewSizeValue(parameters: Camera.Parameters, screenResolution: Point): Point {

        // Sort by size, descending
        val supportedPreviewSizes = ArrayList(parameters.supportedPreviewSizes)
        Collections.sort<Camera.Size>(supportedPreviewSizes, Comparator<Camera.Size> { a, b ->
            val aPixels = a.height * a.width
            val bPixels = b.height * b.width
            if (bPixels < aPixels) {
                return@Comparator -1
            }
            if (bPixels > aPixels) {
                1
            } else 0
        })

        if (Log.isLoggable(TAG, Log.INFO)) {
            val previewSizesString = StringBuilder()
            for (supportedPreviewSize in supportedPreviewSizes) {
                previewSizesString.append(supportedPreviewSize.width).append('x')
                        .append(supportedPreviewSize.height).append(' ')
            }
            Log.i(TAG, "Supported preview sizes: $previewSizesString")
        }

        var bestSize: Point? = null
        val screenAspectRatio = screenResolution.x.toFloat() / screenResolution.y.toFloat()

        var diff = java.lang.Float.POSITIVE_INFINITY
        for (supportedPreviewSize in supportedPreviewSizes) {
            val realWidth = supportedPreviewSize.width
            val realHeight = supportedPreviewSize.height
            val pixels = realWidth * realHeight
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                continue
            }
            val isCandidatePortrait = realWidth < realHeight
            val maybeFlippedWidth = if (isCandidatePortrait) realHeight else realWidth
            val maybeFlippedHeight = if (isCandidatePortrait) realWidth else realHeight
            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                val exactPoint = Point(realWidth, realHeight)
                Log.i(TAG, "Found preview size exactly matching screen size: $exactPoint")
                return exactPoint
            }
            val aspectRatio = maybeFlippedWidth.toFloat() / maybeFlippedHeight.toFloat()
            val newDiff = Math.abs(aspectRatio - screenAspectRatio)
            if (newDiff < diff) {
                bestSize = Point(realWidth, realHeight)
                diff = newDiff
            }
        }

        if (bestSize == null) {
            val defaultSize = parameters.previewSize
            bestSize = Point(defaultSize.width, defaultSize.height)
            Log.i(TAG, "No suitable preview sizes, using default: $bestSize")
        }

        Log.i(TAG, "Found best approximate preview size: $bestSize")
        return bestSize
    }

    companion object {

        private val TAG = "CameraConfiguration"
        // This is bigger than the size of a small screen, which is still supported. The routine
        // below will still select the default (presumably 320x240) size for these. This prevents
        // accidental selection of very low resolution on some devices.
        private val MIN_PREVIEW_PIXELS = 470 * 320 // normal screen
        private val MAX_PREVIEW_PIXELS = 800 * 600 // more than large/HD screen

        private fun initializeTorch(parameters: Camera.Parameters, prefs: SharedPreferences) {
            val currentSetting = prefs.getBoolean(PreferencesActivity.KEY_TOGGLE_LIGHT, false)
            doSetTorch(parameters, currentSetting)
        }

        private fun doSetTorch(parameters: Camera.Parameters, newSetting: Boolean) {
            val flashMode: String?
            if (newSetting) {
                flashMode = findSettableValue(parameters.supportedFlashModes,
                        Camera.Parameters.FLASH_MODE_TORCH,
                        Camera.Parameters.FLASH_MODE_ON)
            } else {
                flashMode = findSettableValue(parameters.supportedFlashModes,
                        Camera.Parameters.FLASH_MODE_OFF)
            }
            if (flashMode != null) {
                parameters.flashMode = flashMode
            }
        }

        private fun findSettableValue(supportedValues: Collection<String>?,
                                      vararg desiredValues: String): String? {
            Log.i(TAG, "Supported values: " + supportedValues!!)
            var result: String? = null
            if (supportedValues != null) {
                for (desiredValue in desiredValues) {
                    if (supportedValues.contains(desiredValue)) {
                        result = desiredValue
                        break
                    }
                }
            }
            Log.i(TAG, "Settable value: " + result!!)
            return result
        }
    }

}
