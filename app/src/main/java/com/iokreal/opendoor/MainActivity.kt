package com.iokreal.opendoor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL

import android.content.Context
import java.io.File

object SimpleStorage {
    private const val FILE_NAME = "auth.dat"

    // Формат файла: URL\nTOKEN
    fun save(context: Context, url: String, token: String) {
        val file = File(context.filesDir, FILE_NAME)
        file.writeText("$url\n$token")
    }

    fun load(context: Context): Pair<String, String>? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return null

        return try {
            val lines = file.readLines()
            if (lines.size >= 2) {
                Pair(lines[0], lines[1]) // url, token
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun clear(context: Context) {
        File(context.filesDir, FILE_NAME).delete()
    }
}
class MainActivity : Activity() {
    private var wait = 100
    private var status = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = SimpleStorage.load(this)

        // Если данных нет - идем на логин
        if (data == null) {
            val intent = Intent(this, Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Нужно для старта из NoDisplay
            startActivity(intent)
            finish()
            return
        }

        val (urlStr, token) = data
        val appContext = applicationContext // Сохраняем контекст приложения, так как активити умрет

        // Запускаем поток "в отрыве" от активити
        Thread {
            try {
                val connection = URL(urlStr).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                connection.setRequestProperty("Authorization", token)

                val code = connection.responseCode
                if (code == 200) {
                    status = "Открыто"
                } else if (code == 401) {
                    status = "Ключ устарел, требуется повторный вход"
                    SimpleStorage.clear(appContext) // Чистим файл
                } else {
                    status = "Ошибка: $code"
                }
                connection.disconnect()
            } catch (e: Exception) {
                status = "Ошибка сети"
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        while (status == ""){
            Thread.sleep(100)
            if (wait < 0){
                status = "таймаут"
            }
            wait--
        }
        Toast.makeText(this@MainActivity, status, Toast.LENGTH_SHORT).show()
        finish()
        nukeCacheData()
        System.exit(0)
    }


    private fun nukeCacheData() {
        val dataDir = File(applicationInfo.dataDir)

        val webviewDir = File(dataDir, "app_webview")
        if (webviewDir.exists()) webviewDir.deleteRecursively()

        val cacheDir = File(dataDir, "cache")
        if (cacheDir.exists()) cacheDir.deleteRecursively()

        val codeCacheDir = File(dataDir, "code_cache")
        if (codeCacheDir.exists()) codeCacheDir.deleteRecursively()

        val shardPrefsDir = File(dataDir, "shared_prefs")
        if (shardPrefsDir.exists()) shardPrefsDir.deleteRecursively()
    }
}