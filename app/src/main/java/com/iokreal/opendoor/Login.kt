package com.iokreal.opendoor

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView

class JsBridge(private val activity: Login) {
    @android.webkit.JavascriptInterface
    fun send(token: String, url: String){
        // Сохраняем в наш легкий файл
        SimpleStorage.save(activity, url, token)
        activity.runOnUiThread{
            activity.onLoginSuccess()
        }
    }
}

class Login : Activity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        webView = findViewById(R.id.webView)
        // Минимально необходимые настройки
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.addJavascriptInterface(JsBridge(this), "AndroidBridge")

        val js = try { assets.open("fun.js").bufferedReader().readText() } catch (e: Exception) { "" }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (js.isNotEmpty()) view?.evaluateJavascript(js, null)
            }
        }
        webView.loadUrl("https://key.rt.ru/main/pwa/dashboard")
        var but = findViewById<TextView>(R.id.button)
        Thread{
            var i = 10
            while (i > 0){
                Thread.sleep(1000)
                runOnUiThread {
                    but.text = "Подождите $i с"
                }
                i--
            }
            runOnUiThread {
                but.isEnabled = true
                but.text = "ОК"
            }
        }.start()
    }

    fun onLoginSuccess(){
        findViewById<TextView>(R.id.textView5).apply {
            text = "Ключ получен"
            visibility = View.VISIBLE
        }
        findViewById<View>(R.id.button2).visibility = View.VISIBLE
        findViewById<View>(R.id.button).visibility = View.GONE

        // 1. Убиваем WebView, чтобы отпустил файлы
        webView.loadUrl("about:blank")
        webView.destroy()
    }

    fun exit(view: View) {
        finishAffinity()
        System.exit(0) // Раз ты хочешь жесткий выход
    }

    fun close(view: View) {
        findViewById<TextView>(R.id.textView5).visibility = View.GONE
        findViewById<TextView>(R.id.button).visibility = View.GONE
    }
}