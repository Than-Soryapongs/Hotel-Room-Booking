package com.madproject.roombookingapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.madproject.roombookingapp.data.model.UserResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.dataStore

    companion object {
        private val USER_DATA_KEY = stringPreferencesKey("user_data")
    }

    suspend fun saveUserData(user: UserResponse) {
        dataStore.edit { preferences ->
            preferences[USER_DATA_KEY] = gson.toJson(user)
        }
    }

    suspend fun getUserData(): UserResponse? {
        val preferences = dataStore.data.first()
        val userJson = preferences[USER_DATA_KEY]
        return userJson?.let { gson.fromJson(it, UserResponse::class.java) }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}