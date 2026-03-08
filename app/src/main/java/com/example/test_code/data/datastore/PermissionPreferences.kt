package com.example.test_code.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "permissions")

@Singleton
class PermissionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationRequestedKey = booleanPreferencesKey("notification_permission_requested")

    // 알림 권한 팝업을 띄운 적 있는지
    val notificationPermissionRequested: Flow<Boolean> = context.dataStore.data
        .map { it[notificationRequestedKey] ?: false }

    suspend fun setNotificationPermissionRequested() {
        context.dataStore.edit { it[notificationRequestedKey] = true }
    }
}
