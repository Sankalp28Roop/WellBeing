package com.wellbeing.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setPin(pin: String) {
        // In a real app, use BCrypt. 
        // For simplicity, we'll store the pin directly in encrypted prefs.
        sharedPreferences.edit().putString("app_pin", pin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val savedPin = sharedPreferences.getString("app_pin", null)
        return savedPin == pin
    }

    fun isPinSet(): Boolean = sharedPreferences.contains("app_pin")
}
