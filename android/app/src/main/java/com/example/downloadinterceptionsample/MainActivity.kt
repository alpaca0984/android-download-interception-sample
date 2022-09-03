package com.example.downloadinterceptionsample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.downloadinterceptionsample.ui.theme.DownloadInterceptionSampleTheme
import java.io.File

private const val JS_INTERFACE_NAME = "android"
private const val FILE_AUTHORITY = "com.example.downloadinterceptionsample"

class MainActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val javaScriptInterface = JavaScriptInterfaceImpl(context = this)
        setContent {
            DownloadInterceptionSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "WebView File Download Interception",
                            style = MaterialTheme.typography.h4,
                        )

                        Spacer(Modifier.height(24.dp))
                        AndroidView(factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                webViewClient = WebViewClient()
                                addJavascriptInterface(javaScriptInterface, JS_INTERFACE_NAME)
                                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                                    if (url.startsWith("blob:")) {
                                        val script = JavaScriptInterfaceImpl.fetchBlobScript(
                                            blobUrl = url,
                                            contentDisposition = contentDisposition,
                                            mimetype = mimetype,
                                        )
                                        evaluateJavascript(script, null)
                                    }
                                }
                                loadUrl("http://10.0.2.2:3000")
                            }
                        })
                    }
                }
            }
        }
    }
}

class JavaScriptInterfaceImpl(private val context: Context) {

    @JavascriptInterface
    fun receiveBase64(
        base64: String,
        url: String,
        contentDisposition: String,
        mimetype: String,
    ) {
        val content = Base64.decode(base64, Base64.DEFAULT)
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        file.writeBytes(content)

        val uri = FileProvider.getUriForFile(context, FILE_AUTHORITY, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimetype)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    companion object {

        fun fetchBlobScript(
            blobUrl: String,
            contentDisposition: String,
            mimetype: String,
        ): String {
            return """
                (async () => {
                  const response = await fetch('${blobUrl}', {
                    headers: {
                      'Content-Type': '${mimetype}',
                    }
                  });
                  const blob = await response.blob();

                  const reader = new FileReader();
                  reader.addEventListener('load', () => {
                    const base64 = reader.result.replace(/^data:.+;base64,/, '');
                    ${JS_INTERFACE_NAME}.receiveBase64(
                      base64,
                      '${blobUrl}',
                      '${contentDisposition}',
                      '${mimetype}'
                    );
                  });
                  reader.readAsDataURL(blob); 
                })();
            """.trimIndent()
        }
    }
}
