package com.example.test_code.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.test_code.BuildConfig

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen() {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    // 🔙 뒤로가기 처리
    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    // ========================================
                    // 1️⃣ 기본 설정
                    // ========================================
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true

                        // ========================================
                        // 2️⃣ 보안 설정
                        // ========================================
                        // Mixed Content 허용 (HTTPS 페이지에서 HTTP 리소스 로드)
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        // File Access 제한 (보안)
                        allowFileAccess = true  // asset 폴더 접근 필요
                        allowFileAccessFromFileURLs = false  // 보안 강화
                        allowUniversalAccessFromFileURLs = false  // 보안 강화

                        // ========================================
                        // 3️⃣ 성능 최적화
                        // ========================================
                        // 캐시 모드
                        cacheMode = WebSettings.LOAD_DEFAULT  // 기본 캐시 정책

                        // Zoom 설정
                        setSupportZoom(false)
                        builtInZoomControls = false

                        // 뷰포트 설정
                        useWideViewPort = true
                        loadWithOverviewMode = true

                        // ========================================
                        // 4️⃣ 디버깅 (개발 환경에만)
                        // ========================================
                        if (BuildConfig.DEBUG) {
                            WebView.setWebContentsDebuggingEnabled(true)
                            // Chrome DevTools: chrome://inspect 접속
                        }
                    }

                    // Hardware Acceleration
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    // ========================================
                    // 5️⃣ JavascriptInterface (보안 주의)
                    // interface object name 설정
                    // ========================================
                    addJavascriptInterface(
                        AndroidBridge(context),
                        "AndroidBridge"
                    )

                    // ========================================
                    // 6️⃣ WebViewClient - 페이지 로딩 및 에러 처리
                    // ========================================
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            loadError = null
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            canGoBack = view?.canGoBack() == true

                            // JS 호출
                            view?.evaluateJavascript(
                                "javascript:onAndroidReady('Android에서 보낸 메시지')",
                                null
                            )
                        }

                        // 에러 처리 (Android 6.0+)
                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                isLoading = false
                                loadError = "페이지를 불러올 수 없습니다 (${error?.description})"
                            }
                        }

                        // HTTP 에러 처리 (404, 500 등)
                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            errorResponse: WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            if (request?.isForMainFrame == true) {
                                loadError = "HTTP 오류: ${errorResponse?.statusCode}"
                            }
                        }

                        // SSL 인증서 에러 처리
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            // 프로덕션: handler.cancel()
                            handler?.cancel()
                            loadError = "SSL 인증서 오류"
                        }

                        // URL 로딩 제어
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url.toString()

                            // 외부 앱 실행 (전화, 문자 등)
                            if (url.startsWith("tel:") || url.startsWith("sms:")) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                                return true
                            }

                            return false  // WebView에서 로딩
                        }
                    }

                    // ========================================
                    // 7️⃣ WebChromeClient - 진행률, 알림 등
                    // ========================================
                    webChromeClient = object : WebChromeClient() {
                        // JavaScript alert() 처리
                        override fun onJsAlert(
                            view: WebView?,
                            url: String?,
                            message: String?,
                            result: JsResult?
                        ): Boolean {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            result?.confirm()
                            return true
                        }

                        // JavaScript console.log() 처리
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            android.util.Log.d(
                                "WebView",
                                "${consoleMessage?.message()} -- From line " +
                                "${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}"
                            )
                            return true
                        }
                    }

                    // ========================================
                    // 8️⃣ 다운로드 리스너
                    // ========================================
                    setDownloadListener { url, _, _, _, _ ->
                        Toast.makeText(context, "다운로드: $url", Toast.LENGTH_SHORT).show()
                    }

                    // URL 로드
                    loadUrl("file:///android_asset/index.html")
                }.also { webView = it }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                webView = view
            }
        )

        // ========================================
        // 🔟 로딩 상태 UI
        // ========================================
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // ========================================
        // 1️⃣1️⃣ 에러 상태 UI
        // ========================================
        loadError?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { webView?.reload() }) {
                        Text("다시 시도")
                    }
                }
            }
        }
    }

    // ========================================
    // 1️⃣2️⃣ 생명주기 관리 (DisposableEffect)
    // ========================================
    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                clearCache(true)
                destroy()
            }
        }
    }
}

// Android ↔ JS 양방향 브릿지
class AndroidBridge(private val context: Context) {

    // 1️⃣ 단순 문자열 받기
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

    // 2️⃣ JSON 받기 (JavaScript → Android)
    @JavascriptInterface
    fun receiveJsonFromJS(jsonString: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                // JSON 파싱 예시 (간단한 Map으로)
                val data = parseSimpleJson(jsonString)
                Toast.makeText(
                    context,
                    "받은 JSON: $data",
                    Toast.LENGTH_LONG
                ).show()

                // 또는 Gson으로 파싱:
                // val gson = Gson()
                // val movie = gson.fromJson(jsonString, Movie::class.java)

            } catch (e: Exception) {
                Toast.makeText(context, "JSON 파싱 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 간단한 JSON 파싱 (예시용 - 실제로는 Gson 사용 권장)
    private fun parseSimpleJson(json: String): Map<String, Any> {
        // 매우 간단한 파싱 (실제로는 Gson 사용!)
        return mapOf("raw" to json)
    }
}
