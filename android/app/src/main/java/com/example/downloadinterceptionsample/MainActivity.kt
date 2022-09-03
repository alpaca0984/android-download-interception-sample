package com.example.downloadinterceptionsample

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import com.example.downloadinterceptionsample.ui.theme.DownloadInterceptionSampleTheme
import java.io.File

private const val JS_INTERFACE_NAME = "android"

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
                                            mimetype = mimetype
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

class JavaScriptInterfaceImpl(
    private val context: Context
) {

    @JavascriptInterface
    fun receiveBase64(base64: String, url: String, mimetype: String) {
        val content = Base64.decode(base64, Base64.DEFAULT)
        val fileName = URLUtil.guessFileName(url, "", mimetype)
        val file = File(context.filesDir, fileName)
        file.writeBytes(content)

//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(Uri.fromFile(file), mimetype)
//        }
//        context.startActivity(intent)
    }

    companion object {

        fun fetchBlobScript(blobUrl: String, mimetype: String): String {
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
                    ${JS_INTERFACE_NAME}.receiveBase64(base64, blobUrl, mimetype);
                  });
                  reader.readAsDataURL(blob); 
                })();
            """.trimIndent()
        }
    }
}
