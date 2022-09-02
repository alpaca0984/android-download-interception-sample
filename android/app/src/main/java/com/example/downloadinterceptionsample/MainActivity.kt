package com.example.downloadinterceptionsample

import android.os.Bundle
import android.webkit.WebView
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DownloadInterceptionSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = "↓ is a WebView",
                            style = MaterialTheme.typography.h2,
                        )

                        Spacer(Modifier.height(12.dp))
                        AndroidView(factory = { context ->
                            WebView(context).apply {
                                loadUrl("http://10.0.2.2:3000")
                            }
                        })
                    }
                }
            }
        }
    }
}
