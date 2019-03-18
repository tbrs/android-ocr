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
package tbrs.ocr.language

import tbrs.ocr.CaptureActivity
import tbrs.ocr.R

import android.graphics.Typeface
import android.os.AsyncTask
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView

/**
 * Class to perform translations in the background.
 */
class TranslateAsyncTask(private val activity: CaptureActivity, private val sourceLanguageCode: String, private val targetLanguageCode: String,
                         private val sourceText: String) : AsyncTask<String, String, Boolean>() {
    private val textView: TextView
    private val progressView: View?
    private val targetLanguageTextView: TextView?
    private var translatedText = ""

    init {
        textView = activity.findViewById<View>(R.id.translation_text_view) as TextView
        progressView = activity.findViewById(R.id.indeterminate_progress_indicator_view) as View
        targetLanguageTextView = activity.findViewById<View>(R.id.translation_language_text_view) as TextView
    }

    override fun doInBackground(vararg arg0: String): Boolean {
        translatedText = Translator.translate(activity, sourceLanguageCode, targetLanguageCode, sourceText)

        // Check for failed translations.
        return if (translatedText == Translator.BAD_TRANSLATION_MSG) {
            false
        } else true

    }

    @Synchronized
    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)

        if (result!!) {
            //Log.i(TAG, "SUCCESS");
            targetLanguageTextView?.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL)
            textView.text = translatedText
            textView.visibility = View.VISIBLE
            textView.setTextColor(activity.resources.getColor(R.color.translation_text))

            // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
            val scaledSize = Math.max(22, 32 - translatedText.length / 4)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize.toFloat())

        } else {
            Log.e(TAG, "FAILURE")
            targetLanguageTextView!!.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC)
            targetLanguageTextView.text = "Unavailable"

        }

        // Turn off the indeterminate progress indicator
        if (progressView != null) {
            progressView.visibility = View.GONE
        }
    }

    companion object {

        private val TAG = TranslateAsyncTask::class.java.simpleName
    }
}
