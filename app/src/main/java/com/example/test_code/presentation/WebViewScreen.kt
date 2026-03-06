package com.example.test_code.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen() {
    val context = LocalContext.current
    var webView: WebView? = remember {null}

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                addJavascriptInterface(
                    AndroidBridge(context),
                    "AndroidBridge"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // 페이지 로드 완료 후 JS 호출
                        view?.evaluateJavascript(
                            "javascript:onAndroidReady('Android에서 보낸 메시지')",
                            null
                        )
                    }
                }
                loadUrl("file:///android_asset/index.html")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// Android → JS 로 받는 브릿지
class AndroidBridge(private val context: Context) {
    @JavascriptInterface
    fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun onButtonClick(data: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "JS에서 받은 데이터: $data", Toast.LENGTH_SHORT).show()
        }
    }
}
