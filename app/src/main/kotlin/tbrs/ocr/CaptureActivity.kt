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

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.text.ClipboardManager
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.capture.*
import tbrs.ocr.camera.CameraManager
import tbrs.ocr.camera.ShutterButton
import tbrs.ocr.kotlin.setSpanBetweenTokens
import tbrs.ocr.language.LanguageCodeHelper
import tbrs.ocr.language.TranslateAsyncTask
import java.io.File
import java.io.IOException

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the text correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
class CaptureActivity : Activity(), SurfaceHolder.Callback, ShutterButton.OnShutterButtonListener {
    internal var cameraManager: CameraManager? = null
        private set
    private var handler: CaptureActivityHandler? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var lastResult: OcrResult? = null
    private var lastBitmap: Bitmap? = null
    private var hasSurface: Boolean = false
    private var beepManager: BeepManager? = null
    // Interface for the Tesseract OCR engine.
    internal var baseApi: TessBaseAPI? = null
        private set
    // ISO 639-3 language code.
    private var sourceLanguageCodeOcr: String = ""
    // Language name, for example, "English".
    private var sourceLanguageReadable: String? = null
    // ISO 639-1 language code.
    private var sourceLanguageCodeTranslation: String = ""
    // ISO 639-1 language code.
    private var targetLanguageCodeTranslation: String = ""
    // Language name, for example, "English".
    private var targetLanguageReadable: String? = null
    private var pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD
    private var ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY
    private var characterBlacklist: String? = null
    private var characterWhitelist: String? = null
    // Whether we want to show translations.
    private var isTranslationActive: Boolean = false
    // Whether we are doing OCR in continuous mode.
    private var isContinuousModeActive: Boolean = false
    // For initOcr - language download & unzip.
    private var dialog: ProgressDialog? = null
    // Also for initOcr - init OCR engine.
    internal var progressDialog: ProgressDialog? = null
        private set
    private var isEngineReady: Boolean = false
    private var isPaused: Boolean = false

    private val errorStatusSpan by lazy {
        ForegroundColorSpan(ContextCompat.getColor(this, R.color.red))
    }

    /** Finds the proper location on the SD card where we can save files.  */
    private val storageDirectory: File?
        get() {
            // Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

            var state: String? = null
            try {
                state = Environment.getExternalStorageState()
            } catch (e: RuntimeException) {
                Log.e(TAG, "Is the SD card visible?", e)
                showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.")
            }

            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                // We can read and write the media
                //    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
                // For Android 2.2 and above.
                try {
                    return getExternalFilesDir(Environment.MEDIA_MOUNTED)
                } catch (e: NullPointerException) {
                    // We get an error here if the SD card is visible, but full.
                    Log.e(TAG, "External storage is unavailable")
                    showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.")
                }
                //        } else {
                //          // For Android 2.1 and below, explicitly give the path as, for example,
                //          // "/mnt/sdcard/Android/data/tbrs.ocr/files/"
                //          return new File(Environment.getExternalStorageDirectory().toString() + File.separator +
                //                  "Android" + File.separator + "data" + File.separator + getPackageName() +
                //                  File.separator + "files" + File.separator);
                //        }
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
                // We can only read the media.
                Log.e(TAG, "External storage is read-only")
                showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.")
            } else {
                // Something else is wrong. It may be one of many other states, but all we need
                // to know is we can neither read nor write
                Log.e(TAG, "External storage is unavailable")
                showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable or corrupted.")
            }
            return null
        }

    /**
     * Returns a string that represents which OCR engine(s) are currently set to be run.
     *
     * @return OCR engine mode
     */
    private val ocrEngineModeName: String
        get() = resources.getStringArray(R.array.ocrenginemodes).getOrElse(ocrEngineMode) { "" }

    internal fun getHandler(): Handler? = handler

    public override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        // Call before everything else.
        checkFirstLaunch()

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.capture)

        registerForContextMenu(status_view_bottom)
        registerForContextMenu(status_view_top)

        handler = null
        lastResult = null
        hasSurface = false
        beepManager = BeepManager(this)

        // Camera shutter button.
        if (DISPLAY_SHUTTER_BUTTON) shutter_button.setOnShutterButtonListener(this)

        registerForContextMenu(ocr_result_text_view)
        registerForContextMenu(translation_text_view)

        with(CameraManager(application)) {
            cameraManager = this
            viewfinder_view.setCameraManager(this)
        }

        // Set listener to change the size of the viewfinder rectangle.
        viewfinder_view.setOnTouchListener(object : View.OnTouchListener {
            internal var lastX = -1
            internal var lastY = -1

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = -1
                        lastY = -1
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val currentX = event.x.toInt()
                        val currentY = event.y.toInt()

                        try {
                            val rect = cameraManager!!.framingRect

                            val BUFFER = 50
                            val BIG_BUFFER = 60
                            if (lastX >= 0) {
                                // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
                                if ((currentX >= rect!!.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER || lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER) && (currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER || lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER)) {
                                    // Top left corner: adjust both top and left sides.
                                    cameraManager!!.adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY))
                                    viewfinder_view.resultText = null
                                } else if ((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER || lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER) && (currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER || lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER)) {
                                    // Top right corner: adjust both top and right sides.
                                    cameraManager!!.adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY))
                                    viewfinder_view.resultText = null
                                } else if ((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER || lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER) && (currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER || lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER)) {
                                    // Bottom left corner: adjust both bottom and left sides.
                                    cameraManager!!.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY))
                                    viewfinder_view.resultText = null
                                } else if ((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER || lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER) && (currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER || lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER)) {
                                    // Bottom right corner: adjust both bottom and right sides.
                                    cameraManager!!.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY))
                                    viewfinder_view.resultText = null
                                } else if ((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER || lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER) && (currentY <= rect.bottom && currentY >= rect.top || lastY <= rect.bottom && lastY >= rect.top)) {
                                    // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits.
                                    cameraManager!!.adjustFramingRect(2 * (lastX - currentX), 0)
                                    viewfinder_view.resultText = null
                                } else if ((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER || lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER) && (currentY <= rect.bottom && currentY >= rect.top || lastY <= rect.bottom && lastY >= rect.top)) {
                                    // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits.
                                    cameraManager!!.adjustFramingRect(2 * (currentX - lastX), 0)
                                    viewfinder_view.resultText = null
                                } else if ((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER || lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER) && (currentX <= rect.right && currentX >= rect.left || lastX <= rect.right && lastX >= rect.left)) {
                                    // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits.
                                    cameraManager!!.adjustFramingRect(0, 2 * (lastY - currentY))
                                    viewfinder_view.resultText = null
                                } else if ((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER || lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER) && (currentX <= rect.right && currentX >= rect.left || lastX <= rect.right && lastX >= rect.left)) {
                                    // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits.
                                    cameraManager!!.adjustFramingRect(0, 2 * (currentY - lastY))
                                    viewfinder_view.resultText = null
                                }
                            }
                        } catch (e: NullPointerException) {
                            Log.e(TAG, "Framing rect not available", e)
                        }

                        v.invalidate()
                        lastX = currentX
                        lastY = currentY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        lastX = -1
                        lastY = -1
                        return true
                    }
                }
                return false
            }
        })

        isEngineReady = false
    }

    override fun onResume() {
        super.onResume()
        resetStatusView()

        val previousSourceLanguageCodeOcr = sourceLanguageCodeOcr
        val previousOcrEngineMode = ocrEngineMode

        readPreferences()

        // Set up the camera preview surface.
        surfaceHolder = preview_view.holder
        if (!hasSurface) {
            surfaceHolder!!.addCallback(this)
            surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        // Comment out the following block to test non-OCR functions without an SD card.

        // Do OCR engine initialization, if necessary.
        val doNewInit = baseApi == null || sourceLanguageCodeOcr != previousSourceLanguageCodeOcr ||
                ocrEngineMode != previousOcrEngineMode
        if (doNewInit) {
            // Initialize the OCR engine
            val storageDirectory = storageDirectory
            if (storageDirectory != null) {
                initOcrEngine(storageDirectory, sourceLanguageCodeOcr, sourceLanguageReadable)
            }
        } else {
            // We already have the engine initialized, so just start the camera.
            resumeOCR()
        }
    }

    /**
     * Method to start or restart recognition after the OCR engine has been initialized,
     * or after the app regains focus. Sets state related settings and OCR engine parameters,
     * and requests camera initialization.
     */
    internal fun resumeOCR() {
        Log.d(TAG, "resumeOCR()")

        // This method is called when Tesseract has already been successfully initialized, so set
        // isEngineReady = true here.
        isEngineReady = true

        isPaused = false

        handler?.resetState()
        if (baseApi != null) {
            baseApi!!.pageSegMode = pageSegmentationMode
            baseApi!!.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist)
            baseApi!!.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist)
        }

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            tryInitCamera()
        }
    }

    /** Called when the shutter button is pressed in continuous mode.  */
    internal fun onShutterButtonPressContinuous() {
        isPaused = true
        handler?.stop()
        beepManager!!.playBeepSoundAndVibrate()
        if (lastResult != null) {
            handleOcrDecode(lastResult!!)
        } else {
            val toast = Toast.makeText(this, "OCR failed. Please try again.", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
            resumeContinuousDecoding()
        }
    }

    /** Called to resume recognition after translation in continuous mode.  */
    internal fun resumeContinuousDecoding() {
        isPaused = false
        resetStatusView()
        setStatusViewForContinuous()
        DecodeHandler.resetDecodeState()
        handler?.resetState()
        if (DISPLAY_SHUTTER_BUTTON) shutter_button.visibility = View.VISIBLE
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d(TAG, "surfaceCreated()")

        if (holder == null) {
            Log.e(TAG, "surfaceCreated gave us a null surface")
        }

        // Only initialize the camera if the OCR engine is ready to go.
        if (!hasSurface && isEngineReady) {
            Log.d(TAG, "surfaceCreated(): calling tryInitCamera()...")
            tryInitCamera()
        }
        hasSurface = true
    }

    /** Initializes the camera and starts the handler to begin previewing.  */
    private fun tryInitCamera() {
        if (ContextCompat.checkSelfPermission(application, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera()
            return
        }

        // Request camera permission.
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_permissions_title)
                .setMessage(R.string.dialog_permissions_message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_permissions_button_positive) { dialog, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
                    dialog.dismiss()
                }.setNegativeButton(R.string.dialog_permissions_button_negative) { dialog, _ ->
                    exitOnCameraInaccessible()
                    dialog.dismiss()
                }.create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = super.onRequestPermissionsResult(requestCode, permissions, grantResults).also {
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera()
        } else {
            exitOnCameraInaccessible()
        }
    }

    private fun initCamera() {
        Log.d(TAG, "initCamera()")
        if (surfaceHolder == null) throw IllegalStateException("No SurfaceHolder provided")

        try {
            // Open and initialize the camera.
            cameraManager!!.openDriver(surfaceHolder!!)

            // Creating the handler starts the preview, which can also throw a RuntimeException.
            handler = CaptureActivityHandler(this, cameraManager!!, isContinuousModeActive)
        } catch (ioe: IOException) {
            exitOnCameraError()
        } catch (e: RuntimeException) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service.
            exitOnCameraError()
        }
    }

    private fun exitOnCameraError() =
            showErrorMessage(R.string.dialog_error_generic_title, R.string.dialog_error_camera_message)

    private fun exitOnCameraInaccessible() = showErrorMessage(R.string.dialog_no_camera_permission_title,
            R.string.dialog_no_camera_permission_message)

    override fun onPause() {
        handler?.quitSynchronously()

        // Stop using the camera, to avoid conflicting with other camera-based apps.
        cameraManager!!.closeDriver()

        if (!hasSurface) preview_view.holder.removeCallback(this)
        super.onPause()
    }

    internal fun stopHandler() = handler?.stop()

    override fun onDestroy() {
        if (baseApi != null) {
            baseApi!!.end()
        }
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent) = when (keyCode) {
        KeyEvent.KEYCODE_BACK -> {
            when {
                isPaused -> {
                    // We're paused in continuous mode. Resume.
                    resumeContinuousDecoding()
                }
                lastResult == null -> {
                    // We're not viewing an OCR result. Exit.
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                else -> {
                    // Go back to previewing in regular OCR mode.
                    resetStatusView()
                    handler?.sendEmptyMessage(R.id.restart_preview)
                }
            }
            true
        }
        KeyEvent.KEYCODE_CAMERA -> if (isContinuousModeActive) {
            onShutterButtonPressContinuous()
        } else {
            handler?.hardwareShutterButtonClick()
        }.let { true }
        KeyEvent.KEYCODE_FOCUS -> {
            // Only perform autofocus if user is not holding down the button.
            if (event.repeatCount == 0) cameraManager!!.requestAutoFocus(500L)
            true
        }
        else -> super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).let {
        menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences)
        menu.add(0, ABOUT_ID, 0, "About").setIcon(android.R.drawable.ic_menu_info_details)
        true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            SETTINGS_ID -> {
                intent = Intent().setClass(this, PreferencesActivity::class.java)
                startActivity(intent)
            }
            ABOUT_ID -> {
                intent = Intent(this, HelpActivity::class.java)
                intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, HelpActivity.ABOUT_PAGE)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    /** Sets the necessary language code values for the given OCR language.  */
    private fun setSourceLanguage(languageCode: String): Boolean {
        sourceLanguageCodeOcr = languageCode
        sourceLanguageCodeTranslation = LanguageCodeHelper.mapLanguageCode(languageCode)
        sourceLanguageReadable = LanguageCodeHelper.getOcrLanguageName(this, languageCode)
        return true
    }

    /** Sets the necessary language code values for the translation target language.  */
    private fun setTargetLanguage(languageCode: String): Boolean {
        targetLanguageCodeTranslation = languageCode
        targetLanguageReadable = LanguageCodeHelper.getTranslationLanguageName(this, languageCode)
        return true
    }

    /**
     * Requests initialization of the OCR engine with the given parameters.
     *
     * @param storageRoot Path to location of the tessdata directory to use.
     * @param languageCode Three-letter ISO 639-3 language code for OCR.
     * @param languageName Name of the language for OCR, for example, "English".
     */
    private fun initOcrEngine(storageRoot: File, languageCode: String, languageName: String?) {
        isEngineReady = false

        // Set up the dialog box for the thermometer-style download progress indicator
        if (dialog != null) {
            dialog!!.dismiss()
        }
        dialog = ProgressDialog(this)

        // If we have a language that only runs using Cube, then set the ocrEngineMode to Cube.
        if (ocrEngineMode != TessBaseAPI.OEM_CUBE_ONLY) {
            for (s in CUBE_REQUIRED_LANGUAGES) {
                if (s == languageCode) {
                    ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY
                    PreferenceManager.getDefaultSharedPreferences(this).edit {
                        putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, ocrEngineModeName)
                    }
                }
            }
        }

        // If our language doesn't support Cube, then set the ocrEngineMode to Tesseract.
        if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
            var cubeOk = false
            for (s in CUBE_SUPPORTED_LANGUAGES) {
                if (s == languageCode) {
                    cubeOk = true
                }
            }
            if (!cubeOk) {
                ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY
                PreferenceManager.getDefaultSharedPreferences(this).edit {
                    putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, ocrEngineModeName)
                }
            }
        }

        // Display the name of the OCR engine we're initializing in the indeterminate progress dialog box.
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        val ocrEngineModeName = ocrEngineModeName
        if (ocrEngineModeName == "Both") {
            progressDialog!!.setMessage("Initializing Cube and Tesseract OCR engines for $languageName...")
        } else {
            progressDialog!!.setMessage("Initializing $ocrEngineModeName OCR engine for $languageName...")
        }
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()

        handler?.quitSynchronously()

        // Disable continuous mode if we're using Cube. This will prevent bad states for devices
        // with low memory that crash when running OCR with Cube, and prevent unwanted delays.
        if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY || ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            Log.d(TAG, "Disabling continuous preview")
            isContinuousModeActive = false
            PreferenceManager.getDefaultSharedPreferences(this).edit {
                putBoolean(PreferencesActivity.KEY_CONTINUOUS_PREVIEW, false)
            }
        }

        // Start AsyncTask to install language data and init OCR.
        baseApi = TessBaseAPI()
        OcrInitAsyncTask(this, baseApi!!, dialog!!, progressDialog!!, languageCode, languageName, ocrEngineMode)
                .execute(storageRoot.toString())
    }

    /**
     * Displays information relating to the result of OCR, and requests a translation if necessary.
     *
     * @param ocrResult Object representing successful OCR results.
     * @return True if a non-null result was received for OCR.
     */
    internal fun handleOcrDecode(ocrResult: OcrResult): Boolean {
        lastResult = ocrResult

        // Test whether the result is null.
        val ocrResultText = ocrResult.text
        if (ocrResultText.isNullOrBlank()) {
            val toast = Toast.makeText(this, "OCR failed. Please try again.", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
            return false
        }

        // Turn off capture-related UI elements.
        shutter_button.visibility = View.GONE
        status_view_bottom.visibility = View.GONE
        status_view_top.visibility = View.GONE
        camera_button_view.visibility = View.GONE
        viewfinder_view.visibility = View.GONE
        result_view.visibility = View.VISIBLE

        lastBitmap = ocrResult.getBitmap()
        if (lastBitmap == null) {
            image_view.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher))
        } else {
            image_view.setImageBitmap(lastBitmap)
        }

        // Display the recognized text.
        source_language_text_view.text = sourceLanguageReadable
        ocr_result_text_view.text = ocrResultText
        // Crudely scale betweeen 22 and 32 -- bigger font for shorter text.
        val scaledSize = Math.max(22, 32 - ocrResultText.length / 4)
        ocr_result_text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize.toFloat())

        if (isTranslationActive) {
            // Handle translation text fields.
            translation_language_label_text_view.visibility = View.VISIBLE
            with(translation_language_text_view) {
                text = targetLanguageReadable
                setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL)
                visibility = View.VISIBLE
            }

            // Activate/re-activate the indeterminate progress indicator.
            translation_text_view.visibility = View.GONE
            indeterminate_progress_indicator_view.visibility = View.VISIBLE
            setProgressBarVisibility(true)

            // Get the translation asynchronously.
            TranslateAsyncTask(this, sourceLanguageCodeTranslation, targetLanguageCodeTranslation,
                    ocrResultText).execute()
        } else {
            translation_language_label_text_view.visibility = View.GONE
            translation_language_text_view.visibility = View.GONE
            translation_text_view.visibility = View.GONE
            indeterminate_progress_indicator_view.visibility = View.GONE
            setProgressBarVisibility(false)
        }
        return true
    }

    /**
     * Displays information relating to the results of a successful real-time OCR request.
     *
     * @param ocrResult Object representing successful OCR results.
     */
    internal fun handleOcrContinuousDecode(ocrResult: OcrResult) {
        lastResult = ocrResult

        // Send an OcrResultText object to the ViewfinderView for text rendering.
        // TODO(tbrs): nullability stubbed with .orEmpty. Do something about it.
        val ocrResultText = ocrResult.text.orEmpty()
        viewfinder_view.resultText = OcrResultText(ocrResultText,
                ocrResult.wordConfidences?.asList().orEmpty(),
                ocrResult.bitmapDimensions,
                ocrResult.regionBoundingBoxes.orEmpty(),
                ocrResult.textlineBoundingBoxes.orEmpty(),
                ocrResult.stripBoundingBoxes.orEmpty(),
                ocrResult.wordBoundingBoxes.orEmpty())

        val meanConfidence = ocrResult.meanConfidence

        if (CONTINUOUS_DISPLAY_RECOGNIZED_TEXT) {
            // Display the recognized text on the screen.
            with(status_view_top) {
                text = ocrResultText
                val scaledSize = Math.max(22, 32 - ocrResultText.length / 4)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize.toFloat())
                setTextColor(Color.BLACK)
                setBackgroundResource(R.color.status_top_text_background)
                background.alpha = meanConfidence * (255 / 100)
            }
        }

        if (CONTINUOUS_DISPLAY_METADATA) {
            // Display recognition-related metadata at the bottom of the screen.
            val recognitionTimeRequired = ocrResult.recognitionTimeRequired
            status_view_bottom.textSize = 14f
            status_view_bottom.text = "OCR: " + sourceLanguageReadable + " - Mean confidence: " +
                    meanConfidence.toString() + " - Time required: " + recognitionTimeRequired + " ms"
        }
    }

    /**
     * Version of handleOcrContinuousDecode for failed OCR requests. Displays a failure message.
     *
     * @param obj Metadata for the failed OCR request.
     */
    internal fun handleOcrContinuousDecode(obj: OcrResultFailure) {
        lastResult = null
        viewfinder_view.resultText = null

        // Reset the text in the recognized text box.
        status_view_top.text = ""

        if (CONTINUOUS_DISPLAY_METADATA) {
            status_view_bottom.textSize = 14f
            status_view_bottom.text = getString(R.string.continuous_decode_status, sourceLanguageReadable, obj.timeRequired)
                    .setSpanBetweenTokens("-", errorStatusSpan)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v == ocr_result_text_view) {
            menu.add(Menu.NONE, OPTIONS_COPY_RECOGNIZED_TEXT_ID, Menu.NONE, "Copy recognized text")
            menu.add(Menu.NONE, OPTIONS_SHARE_RECOGNIZED_TEXT_ID, Menu.NONE, "Share recognized text")
        } else if (v == translation_text_view) {
            menu.add(Menu.NONE, OPTIONS_COPY_TRANSLATED_TEXT_ID, Menu.NONE, "Copy translated text")
            menu.add(Menu.NONE, OPTIONS_SHARE_TRANSLATED_TEXT_ID, Menu.NONE, "Share translated text")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        when (item.itemId) {

            OPTIONS_COPY_RECOGNIZED_TEXT_ID -> {
                clipboardManager.text = ocr_result_text_view.text
                if (clipboardManager.hasText()) {
                    val toast = Toast.makeText(this, "Text copied.", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.BOTTOM, 0, 0)
                    toast.show()
                }
                return true
            }
            OPTIONS_SHARE_RECOGNIZED_TEXT_ID -> {
                val shareRecognizedTextIntent = Intent(android.content.Intent.ACTION_SEND)
                shareRecognizedTextIntent.type = "text/plain"
                shareRecognizedTextIntent.putExtra(android.content.Intent.EXTRA_TEXT, ocr_result_text_view.text)
                startActivity(Intent.createChooser(shareRecognizedTextIntent, "Share via"))
                return true
            }
            OPTIONS_COPY_TRANSLATED_TEXT_ID -> {
                clipboardManager.text = translation_text_view.text
                if (clipboardManager.hasText()) {
                    val toast = Toast.makeText(this, "Text copied.", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.BOTTOM, 0, 0)
                    toast.show()
                }
                return true
            }
            OPTIONS_SHARE_TRANSLATED_TEXT_ID -> {
                val shareTranslatedTextIntent = Intent(android.content.Intent.ACTION_SEND)
                shareTranslatedTextIntent.type = "text/plain"
                shareTranslatedTextIntent.putExtra(android.content.Intent.EXTRA_TEXT, translation_text_view.text)
                startActivity(Intent.createChooser(shareTranslatedTextIntent, "Share via"))
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    /**
     * Resets view elements.
     */
    private fun resetStatusView() {
        result_view.visibility = View.GONE
        if (CONTINUOUS_DISPLAY_METADATA) with(status_view_bottom) {
            text = ""
            textSize = 14f
            setTextColor(resources.getColor(R.color.status_text))
            visibility = View.VISIBLE
        }
        if (CONTINUOUS_DISPLAY_RECOGNIZED_TEXT) {
            status_view_top.text = ""
            status_view_top.textSize = 14f
            status_view_top.visibility = View.VISIBLE
        }
        viewfinder_view.visibility = View.VISIBLE
        camera_button_view.visibility = View.VISIBLE
        if (DISPLAY_SHUTTER_BUTTON) shutter_button.visibility = View.VISIBLE
        lastResult = null
        viewfinder_view.resultText = null
    }

    /** Displays a pop-up message showing the name of the current OCR source language.  */
    internal fun showLanguageName() {
        val toast = Toast.makeText(this, "OCR: " + sourceLanguageReadable!!, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
    }

    /**
     * Displays an initial message to the user while waiting for the first OCR request to be
     * completed after starting realtime OCR.
     */
    internal fun setStatusViewForContinuous() {
        viewfinder_view.resultText = null
        if (CONTINUOUS_DISPLAY_METADATA) {
            status_view_bottom.text = "OCR: $sourceLanguageReadable - waiting for OCR..."
        }
    }

    internal fun setButtonVisibility(visible: Boolean) {
        shutter_button.visibility = if (visible && DISPLAY_SHUTTER_BUTTON) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /**
     * Enables/disables the shutter button to prevent double-clicks on the button.
     *
     * @param clickable True if the button should accept a click.
     */
    internal fun setShutterButtonClickable(clickable: Boolean) {
        shutter_button.isClickable = clickable
    }

    /** Request the viewfinder to be invalidated.  */
    internal fun drawViewfinder() = viewfinder_view.drawViewfinder()

    override fun onShutterButtonClick(b: ShutterButton) {
        if (isContinuousModeActive) {
            onShutterButtonPressContinuous()
        } else {
            handler?.shutterButtonClick()
        }
    }

    override fun onShutterButtonFocus(b: ShutterButton, pressed: Boolean) {
        requestDelayedAutoFocus()
    }

    /**
     * Requests autofocus after a 350 ms delay. This delay prevents requesting focus when the user
     * just wants to click the shutter button without focusing. Quick button press/release will
     * trigger onShutterButtonClick() before the focus kicks in.
     */
    private fun requestDelayedAutoFocus() {
        // Wait 350 ms before focusing to avoid interfering with quick button presses when
        // the user just wants to take a picture without focusing.
        cameraManager!!.requestAutoFocus(350L)
    }

    /**
     * We want the help screen to be shown automatically the first time a new version of the app is
     * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
     * it to a value stored as a preference.
     */
    private fun checkFirstLaunch(): Boolean = try {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0)
        val cleanInstall = lastVersion == 0

        if (cleanInstall) writeDefaultPreferences()

        val currentVersion = packageManager.getPackageInfo(packageName, 0).versionCode
        (currentVersion > lastVersion).also { upgraded ->
            if (!upgraded) return@also

            // Record the last version for which we last displayed the What's New (Help) page.
            prefs.edit { putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion) }

            // Show the default page on a clean install, and the what's new page on an upgrade.
            with(Intent(this, HelpActivity::class.java)) {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                putExtra(HelpActivity.REQUESTED_PAGE_KEY, if (cleanInstall) HelpActivity.DEFAULT_PAGE else HelpActivity.WHATS_NEW_PAGE)
                startActivity(this)
            }
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Log.w(TAG, e)
        false
    }

    /**
     * Gets values from shared preferences and sets the corresponding data members in this activity.
     */
    private fun readPreferences() = with(PreferenceManager.getDefaultSharedPreferences(this)) {
        // Retrieve from preferences, and set in this Activity, the language preferences.
        PreferenceManager.setDefaultValues(this@CaptureActivity, R.xml.preferences, false)
        setSourceLanguage(getString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, DEFAULT_SOURCE_LANGUAGE_CODE))
        setTargetLanguage(getString(PreferencesActivity.KEY_TARGET_LANGUAGE_PREFERENCE, DEFAULT_TARGET_LANGUAGE_CODE))
        isTranslationActive = getBoolean(PreferencesActivity.KEY_TOGGLE_TRANSLATION, false)

        // Retrieve from preferences, and set in this Activity, the capture mode preference.
        isContinuousModeActive = getBoolean(PreferencesActivity.KEY_CONTINUOUS_PREVIEW, DEFAULT_TOGGLE_CONTINUOUS)

        // Retrieve from preferences, and set in this Activity, the page segmentation mode preference.
        val pageSegmentationModes = resources.getStringArray(R.array.pagesegmentationmodes)
        pageSegmentationMode = if (pageSegmentationModes.size != 9) {
            Log.e(TAG, "Incorrect resource for page segmentation modes")
            TessBaseAPI.PageSegMode.PSM_AUTO
        } else when (getString(PreferencesActivity.KEY_PAGE_SEGMENTATION_MODE, pageSegmentationModes[0])) {
            pageSegmentationModes[0] -> TessBaseAPI.PageSegMode.PSM_AUTO_OSD
            pageSegmentationModes[1] -> TessBaseAPI.PageSegMode.PSM_AUTO
            pageSegmentationModes[2] -> TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
            pageSegmentationModes[3] -> TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR
            pageSegmentationModes[4] -> TessBaseAPI.PageSegMode.PSM_SINGLE_COLUMN
            pageSegmentationModes[5] -> TessBaseAPI.PageSegMode.PSM_SINGLE_LINE
            pageSegmentationModes[6] -> TessBaseAPI.PageSegMode.PSM_SINGLE_WORD
            pageSegmentationModes[7] -> TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT
            pageSegmentationModes[8] -> TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT
            else -> TessBaseAPI.PageSegMode.PSM_AUTO
        }

        // Retrieve from preferences, and set in this Activity, the OCR engine mode.
        val ocrEngineModes = resources.getStringArray(R.array.ocrenginemodes)
        val ocrEngineModeName = getString(PreferencesActivity.KEY_OCR_ENGINE_MODE, ocrEngineModes[0])
        if (ocrEngineModeName == ocrEngineModes[0]) {
            ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY
        } else if (ocrEngineModeName == ocrEngineModes[1]) {
            ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY
        } else if (ocrEngineModeName == ocrEngineModes[2]) {
            ocrEngineMode = TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED
        }

        // Retrieve from preferences, and set in this Activity, the character blacklist and whitelist.
        characterBlacklist = OcrCharacterHelper.getBlacklist(this, sourceLanguageCodeOcr)
        characterWhitelist = OcrCharacterHelper.getWhitelist(this, sourceLanguageCodeOcr)

        beepManager!!.updatePrefs()
    }

    /**
     * Sets default values for preferences.
     * To be called after fresh install.
     */
    private fun writeDefaultPreferences() = PreferenceManager.getDefaultSharedPreferences(this).edit {
        putBoolean(PreferencesActivity.KEY_CONTINUOUS_PREVIEW, DEFAULT_TOGGLE_CONTINUOUS)
        putString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, DEFAULT_SOURCE_LANGUAGE_CODE)
        putBoolean(PreferencesActivity.KEY_TOGGLE_TRANSLATION, DEFAULT_TOGGLE_TRANSLATION)
        putString(PreferencesActivity.KEY_TARGET_LANGUAGE_PREFERENCE, DEFAULT_TARGET_LANGUAGE_CODE)
        putString(PreferencesActivity.KEY_TRANSLATOR, DEFAULT_TRANSLATOR)
        putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, DEFAULT_OCR_ENGINE_MODE)
        putBoolean(PreferencesActivity.KEY_AUTO_FOCUS, DEFAULT_TOGGLE_AUTO_FOCUS)
        putBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, DEFAULT_DISABLE_CONTINUOUS_FOCUS)
        putBoolean(PreferencesActivity.KEY_PLAY_BEEP, DEFAULT_TOGGLE_BEEP)
        putString(PreferencesActivity.KEY_CHARACTER_BLACKLIST, OcrCharacterHelper.getDefaultBlacklist(DEFAULT_SOURCE_LANGUAGE_CODE))
        putString(PreferencesActivity.KEY_CHARACTER_WHITELIST, OcrCharacterHelper.getDefaultWhitelist(DEFAULT_SOURCE_LANGUAGE_CODE))
        putString(PreferencesActivity.KEY_PAGE_SEGMENTATION_MODE, DEFAULT_PAGE_SEGMENTATION_MODE)
        putBoolean(PreferencesActivity.KEY_REVERSE_IMAGE, DEFAULT_TOGGLE_REVERSED_IMAGE)
        putBoolean(PreferencesActivity.KEY_TOGGLE_LIGHT, DEFAULT_TOGGLE_LIGHT)
    }

    internal fun displayProgressDialog() {
        // Set up the indeterminate progress dialog box.
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        val ocrEngineModeName = ocrEngineModeName
        if (ocrEngineModeName == "Both") {
            progressDialog!!.setMessage("Performing OCR using Cube and Tesseract...")
        } else {
            progressDialog!!.setMessage("Performing OCR using $ocrEngineModeName...")
        }
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    private fun showErrorMessage(@StringRes title: Int, @StringRes message: Int) =
            showErrorMessage(getString(title), getString(message))

    /**
     * Displays an error message dialog box to the user on the UI thread.
     *
     * @param title The title for the dialog box.
     * @param message The error message to be displayed.
     */
    internal fun showErrorMessage(title: String, message: String) =
            AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setOnCancelListener {
                        finish()
                    }
                    .setPositiveButton(R.string.dialog_error_button_positive) { _, _ ->
                        finish()
                    }
                    .show()

    companion object {

        private val TAG = CaptureActivity::class.java.simpleName

        // Note: These constants will be overridden by any default values defined in preferences.xml.

        /** ISO 639-3 language code indicating the default recognition language.  */
        val DEFAULT_SOURCE_LANGUAGE_CODE = "eng"

        /** ISO 639-1 language code indicating the default target language for translation.  */
        val DEFAULT_TARGET_LANGUAGE_CODE = "es"

        /** The default online machine translation service to use.  */
        val DEFAULT_TRANSLATOR = "Google Translate"

        /** The default OCR engine to use.  */
        val DEFAULT_OCR_ENGINE_MODE = "Tesseract"

        /** The default page segmentation mode to use.  */
        val DEFAULT_PAGE_SEGMENTATION_MODE = "Auto"

        /** Whether to use autofocus by default.  */
        val DEFAULT_TOGGLE_AUTO_FOCUS = true

        /** Whether to initially disable continuous-picture and continuous-video focus modes.  */
        val DEFAULT_DISABLE_CONTINUOUS_FOCUS = true

        /** Whether to beep by default when the shutter button is pressed.  */
        val DEFAULT_TOGGLE_BEEP = false

        /** Whether to initially show a looping, real-time OCR display.  */
        val DEFAULT_TOGGLE_CONTINUOUS = false

        /** Whether to initially reverse the image returned by the camera.  */
        val DEFAULT_TOGGLE_REVERSED_IMAGE = false

        /** Whether to enable the use of online translation services be default.  */
        val DEFAULT_TOGGLE_TRANSLATION = true

        /** Whether the light should be initially activated by default.  */
        val DEFAULT_TOGGLE_LIGHT = false


        /** Flag to display the real-time recognition results at the top of the scanning screen.  */
        private val CONTINUOUS_DISPLAY_RECOGNIZED_TEXT = true

        /** Flag to display recognition-related statistics on the scanning screen.  */
        private val CONTINUOUS_DISPLAY_METADATA = true

        /** Flag to enable display of the on-screen shutter button.  */
        private val DISPLAY_SHUTTER_BUTTON = true

        /** Languages for which Cube data is available.  */
        internal val CUBE_SUPPORTED_LANGUAGES = arrayOf(
                "ara", // Arabic
                "eng", // English
                "hin" // Hindi
        )

        /** Languages that require Cube, and cannot run using Tesseract.  */
        private val CUBE_REQUIRED_LANGUAGES = arrayOf(
                "ara" // Arabic
        )

        /** Resource to use for data file downloads.  */
        internal val DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/"

        /** Download filename for orientation and script detection (OSD) data.  */
        internal val OSD_FILENAME = "tesseract-ocr-3.01.osd.tar"

        /** Destination filename for orientation and script detection (OSD) data.  */
        internal val OSD_FILENAME_BASE = "osd.traineddata"

        /** Minimum mean confidence score necessary to not reject single-shot OCR result. Currently unused.  */
        internal val MINIMUM_MEAN_CONFIDENCE = 0 // 0 means don't reject any scored results

        // Context menu.
        private val SETTINGS_ID = Menu.FIRST
        private val ABOUT_ID = Menu.FIRST + 1

        // Options menu, for copy to clipboard.
        private val OPTIONS_COPY_RECOGNIZED_TEXT_ID = Menu.FIRST
        private val OPTIONS_COPY_TRANSLATED_TEXT_ID = Menu.FIRST + 1
        private val OPTIONS_SHARE_RECOGNIZED_TEXT_ID = Menu.FIRST + 2
        private val OPTIONS_SHARE_TRANSLATED_TEXT_ID = Menu.FIRST + 3

        private const val PERMISSION_REQUEST_CAMERA = 42
    }
}
