package com.example.test_code.presentation.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_code.data.datastore.PermissionPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    private val permissionPreferences: PermissionPreferences
) : ViewModel() {

    val notificationPermissionRequested = permissionPreferences.notificationPermissionRequested

    fun markPermissionRequested() {
        viewModelScope.launch {
            permissionPreferences.setNotificationPermissionRequested()
        }
    }
}
