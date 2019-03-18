/*
 * Copyright (C) 2012 ZXing authors
 * Copyright 2012 Robert Theis
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
import android.content.SharedPreferences
import android.hardware.Camera
import android.preference.PreferenceManager
import android.util.Log
import edu.sfsu.cs.orange.ocr.PreferencesActivity

import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask

class AutoFocusManager internal constructor(context: Context, private val camera: Camera) : Camera.AutoFocusCallback {

    private var active: Boolean = false
    private var manual: Boolean = false
    private val useAutoFocus: Boolean
    private val timer: Timer
    private var outstandingTask: TimerTask? = null

    init {
        timer = Timer(true)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val currentFocusMode = camera.parameters.focusMode
        useAutoFocus = sharedPrefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true) && FOCUS_MODES_CALLING_AF.contains(currentFocusMode)
        Log.i(TAG, "Current focus mode '$currentFocusMode'; use auto focus? $useAutoFocus")
        manual = false
        checkAndStart()
    }

    @Synchronized
    override fun onAutoFocus(success: Boolean, theCamera: Camera) {
        if (active && !manual) {
            outstandingTask = object : TimerTask() {
                override fun run() {
                    checkAndStart()
                }
            }
            timer.schedule(outstandingTask, AUTO_FOCUS_INTERVAL_MS)
        }
        manual = false
    }

    internal fun checkAndStart() {
        if (useAutoFocus) {
            active = true
            start()
        }
    }

    @Synchronized
    internal fun start() {
        try {
            camera.autoFocus(this)
        } catch (re: RuntimeException) {
            // Have heard RuntimeException reported in Android 4.0.x+; continue?
            Log.w(TAG, "Unexpected exception while focusing", re)
        }

    }

    /**
     * Performs a manual auto-focus after the given delay.
     * @param delay Time to wait before auto-focusing, in milliseconds
     */
    @Synchronized
    internal fun start(delay: Long) {
        outstandingTask = object : TimerTask() {
            override fun run() {
                manual = true
                start()
            }
        }
        timer.schedule(outstandingTask, delay)
    }

    @Synchronized
    internal fun stop() {
        if (useAutoFocus) {
            camera.cancelAutoFocus()
        }
        if (outstandingTask != null) {
            outstandingTask!!.cancel()
            outstandingTask = null
        }
        active = false
        manual = false
    }

    companion object {

        private val TAG = AutoFocusManager::class.java.simpleName

        private val AUTO_FOCUS_INTERVAL_MS = 3500L
        private val FOCUS_MODES_CALLING_AF: MutableCollection<String>

        init {
            FOCUS_MODES_CALLING_AF = ArrayList(2)
            FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO)
            FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO)
        }
    }

}
