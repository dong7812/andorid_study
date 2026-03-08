package com.example.test_code.presentation.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NotificationPermissionHandler(
    viewModel: NotificationPermissionViewModel = hiltViewModel()
) {
    // Android 13 미만은 알림 권한 불필요
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val alreadyRequested by viewModel.notificationPermissionRequested.collectAsState(initial = false)
    var showDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            val activity = context as? Activity
            val permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                )
            if (permanentlyDenied) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(alreadyRequested) {
        if (alreadyRequested) return@LaunchedEffect

        val isGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            showDialog = true
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("알림 권한 요청") },
            text = { Text("앱의 중요한 소식을 받으려면 알림 권한이 필요합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.markPermissionRequested()
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("허용")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    (context as? Activity)?.finish()
                }) {
                    Text("취소")
                }
            }
        )
    }
}
