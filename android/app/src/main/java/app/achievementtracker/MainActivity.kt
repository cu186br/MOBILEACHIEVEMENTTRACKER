package app.achievementtracker

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.InputType
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout

/**
 * Opens your Achievement Tracker web page full screen, with no address bar.
 * The link is asked for once and kept on the phone; you can change it from the
 * "could not load" dialog.
 */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private val prefs by lazy { getSharedPreferences("tracker", MODE_PRIVATE) }
    private var asking = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.setBackgroundColor(Color.parseColor("#0f1419"))
        web.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(web)

        val s = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.setSupportMultipleWindows(true)
        s.javaScriptCanOpenWindowsAutomatically = true
        // Google refuses sign-in from a plain WebView. Dropping the "; wv" marker makes it look like Chrome.
        s.userAgentString = s.userAgentString.replace("; wv", "")
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                return if (staysInApp(r.url)) false else { openOutside(r.url); true }
            }
            override fun onReceivedError(v: WebView, r: WebResourceRequest, e: WebResourceError) {
                if (r.isForMainFrame) askForLink("Could not load the page. Check your connection or change the link.")
            }
        }
        // Links that open a new window (store pages, for example) go to the browser.
        web.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(v: WebView, dialog: Boolean, user: Boolean, msg: Message): Boolean {
                val t = WebView(this@MainActivity)
                t.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v2: WebView, r: WebResourceRequest): Boolean { openOutside(r.url); return true }
                }
                (msg.obj as WebView.WebViewTransport).webView = t
                msg.sendToTarget()
                return true
            }
        }

        val url = prefs.getString("url", null)
        if (url.isNullOrBlank()) askForLink("Paste your Achievement Tracker link (the Web app URL).") else web.loadUrl(url)
    }

    // Pages of the tracker and Google's sign-in stay inside the app; everything else opens in the browser.
    private fun staysInApp(u: Uri): Boolean {
        val h = u.host ?: return true
        return h.endsWith("google.com") || h.endsWith("googleusercontent.com") || h.endsWith("gstatic.com")
    }

    private fun openOutside(u: Uri) {
        try { startActivity(Intent(Intent.ACTION_VIEW, u)) } catch (e: Exception) { /* no browser */ }
    }

    private fun askForLink(message: String) {
        if (asking || isFinishing) return
        asking = true
        val box = EditText(this)
        box.inputType = InputType.TYPE_TEXT_VARIATION_URI
        box.setText(prefs.getString("url", "https://script.google.com/macros/s/"))
        AlertDialog.Builder(this)
            .setTitle("Link")
            .setMessage(message)
            .setView(box)
            .setCancelable(false)
            .setPositiveButton("Open") { _, _ ->
                asking = false
                val link = box.text.toString().trim()
                if (link.startsWith("https://")) { prefs.edit().putString("url", link).apply(); web.loadUrl(link) }
                else askForLink("The link must start with https://")
            }
            .setNegativeButton("Close app") { _, _ -> finish() }
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onPause() { CookieManager.getInstance().flush(); super.onPause() }
}
