package com.example.skillswaps

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class FirstLaunchManager(private val context: Context) {
    companion object {
        private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
    }

    // Flow that emits true if it's first launch (or the key is absent)
    val isFirstLaunch = context.dataStore.data
        .map { prefs ->
            prefs[FIRST_LAUNCH_KEY] ?: true
        }

    // Call this once you’ve shown the screen
    suspend fun setLaunched() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH_KEY] = false
        }
    }
}
