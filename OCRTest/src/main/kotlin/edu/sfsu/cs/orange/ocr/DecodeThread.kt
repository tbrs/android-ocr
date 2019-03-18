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

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
internal class DecodeThread(private val activity: CaptureActivity) : Thread() {
    private lateinit var handler: Handler
    private val handlerInitLatch: CountDownLatch = CountDownLatch(1)

    fun getHandler(): Handler = try {
        handlerInitLatch.await()
        handler
    } catch (ie: InterruptedException) {
        // continue?
        handler
    }

    override fun run() {
        Looper.prepare()
        handler = DecodeHandler(activity)
        handlerInitLatch.countDown()
        Looper.loop()
    }
}
