package iem.bdia.polyhome.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.tokenStorage by preferencesDataStore(name = "token")

class TokenStorage(private val context: Context) {

    private val tokenKey = stringPreferencesKey("token")

    suspend fun saveToken(token: String) {
        context.tokenStorage.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    suspend fun getToken(): String? {
        val prefs = context.tokenStorage.data.first()
        return prefs[tokenKey]
    }

    suspend fun clearToken() {
        context.tokenStorage.edit { prefs ->
            prefs.remove(tokenKey)
        }
    }

}