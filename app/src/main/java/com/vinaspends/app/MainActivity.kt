package com.vinaspends.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.app.Activity
import java.io.File

class MainActivity : Activity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure cache directories exist to prevent Chromium errors
        try {
            val baseCacheDir = cacheDir
            File(baseCacheDir, "WebView/Default/HTTP Cache/Code Cache/js").mkdirs()
            File(baseCacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm").mkdirs()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val webView = WebView(this)
        webView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
        setContentView(webView)
        
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        // Disable cache to see if it suppresses the HTTP cache errors
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        
        webView.webViewClient = WebViewClient()
        // 10.0.2.2 is the special alias to your host loopback interface (i.e., localhost on the development machine)
        webView.loadUrl("http://10.0.2.2:3000")
    }
}
