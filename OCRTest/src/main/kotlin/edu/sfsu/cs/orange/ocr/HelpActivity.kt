/*
 * Copyright 2008 ZXing authors
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

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Activity to display informational pages to the user in a WebView.
 *
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
class HelpActivity : Activity() {

    private var webView: WebView? = null

    private val doneListener = View.OnClickListener { finish() }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.help)

        webView = findViewById<View>(R.id.help_contents) as WebView
        webView!!.webViewClient = HelpClient(this)

        val intent = intent
        val page = intent!!.getStringExtra(REQUESTED_PAGE_KEY)

        // Show an OK button.
        val doneButton = findViewById<View>(R.id.done_button)
        doneButton.setOnClickListener(doneListener)

        if (page == DEFAULT_PAGE) {
            doneButton.visibility = View.VISIBLE
        } else {
            doneButton.visibility = View.GONE
        }

        // Froyo has a bug with calling onCreate() twice in a row, which causes the What's New page
        // that's auto-loaded on first run to appear blank. As a workaround we only call restoreState()
        // if a valid URL was loaded at the time the previous activity was torn down.
        if (icicle != null && icicle.getBoolean(WEBVIEW_STATE_PRESENT, false)) {
            webView!!.restoreState(icicle)
        } else if (intent != null && page != null && page.length > 0) {
            webView!!.loadUrl(BASE_URL + page)
        } else {
            webView!!.loadUrl(BASE_URL + DEFAULT_PAGE)
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        val url = webView!!.url
        if (url != null && url.length > 0) {
            webView!!.saveState(state)
            state.putBoolean(WEBVIEW_STATE_PRESENT, true)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private inner class HelpClient(internal var context: Activity) : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            title = view.title
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("file")) {
                // Keep local assets in this WebView.
                return false
            } else if (url.startsWith("mailto:")) {
                try {
                    val mt = MailTo.parse(url)
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "message/rfc822"
                    i.putExtra(Intent.EXTRA_EMAIL, arrayOf(mt.to))
                    i.putExtra(Intent.EXTRA_SUBJECT, mt.subject)
                    context.startActivity(i)
                    view.reload()
                } catch (e: ActivityNotFoundException) {
                    Log.w(TAG, "Problem with Intent.ACTION_SEND", e)
                    AlertDialog.Builder(context)
                            .setTitle("Contact Info")
                            .setMessage("Please send your feedback to: app.ocr@gmail.com")
                            .setPositiveButton("Done") { dialog, which -> Log.d("AlertDialog", "Positive") }
                            .show()
                }

                return true
            } else {
                // Open external URLs in Browser.
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }
        }
    }

    companion object {

        private val TAG = HelpActivity::class.java.simpleName

        // Use this key and one of the values below when launching this activity via intent. If not
        // present, the default page will be loaded.
        val REQUESTED_PAGE_KEY = "requested_page_key"
        val DEFAULT_PAGE = "whatsnew.html"
        val ABOUT_PAGE = "about.html"
        val TERMS_PAGE = "terms.html"
        val WHATS_NEW_PAGE = "whatsnew.html"

        private val BASE_URL = "file:///android_asset/html/"
        private val WEBVIEW_STATE_PRESENT = "webview_state_present"
    }
}