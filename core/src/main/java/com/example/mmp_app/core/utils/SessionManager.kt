package com.example.mmp_app.core.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            Log.e("SessionManager", "Failed to initialize EncryptedSharedPreferences, clearing and retrying", e)
            // Clear corrupted preferences and retry once
            context.getSharedPreferences("mmp_secure_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            try {
                createEncryptedPrefs()
            } catch (e2: Exception) {
                Log.e("SessionManager", "Still failed after clearing, falling back to plain SharedPreferences", e2)
                try {
                    context.getSharedPreferences("mmp_plain_prefs", Context.MODE_PRIVATE)
                } catch (e3: Exception) {
                    Log.e("SessionManager", "Failed to initialize plain SharedPreferences, using in-memory fallback", e3)
                    // Fallback: Use in-memory SharedPreferences implementation
                    class InMemorySharedPreferences : SharedPreferences {
                        private val map = mutableMapOf<String, Any?>()
                        private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

                        override fun getAll(): MutableMap<String, *> = map
                        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
                        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = map[key] as? MutableSet<String> ?: defValues
                        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
                        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
                        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
                        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
                        override fun contains(key: String?): Boolean = map.containsKey(key)

                        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
                            private val tempMap = mutableMapOf<String, Any?>()
                            private var clear = false
                            override fun putString(key: String?, value: String?): SharedPreferences.Editor { if (key != null) tempMap[key] = value; return this }
                            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { if (key != null) tempMap[key] = values; return this }
                            override fun putInt(key: String?, value: Int): SharedPreferences.Editor { if (key != null) tempMap[key] = value; return this }
                            override fun putLong(key: String?, value: Long): SharedPreferences.Editor { if (key != null) tempMap[key] = value; return this }
                            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { if (key != null) tempMap[key] = value; return this }
                            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { if (key != null) tempMap[key] = value; return this }
                            override fun remove(key: String?): SharedPreferences.Editor { if (key != null) tempMap.remove(key); return this }
                            override fun clear(): SharedPreferences.Editor { clear = true; return this }
                            override fun commit(): Boolean { apply(); return true }
                            override fun apply() {
                                synchronized(map) {
                                    if (clear) map.clear()
                                    map.putAll(tempMap)
                                }
                                synchronized(listeners) {
                                    listeners.forEach { it.onSharedPreferenceChanged(this@InMemorySharedPreferences, null) }
                                }
                            }
                        }

                        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
                            if (listener != null) synchronized(listeners) { listeners.add(listener) }
                        }

                        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
                            if (listener != null) synchronized(listeners) { listeners.remove(listener) }
                        }
                    }
                    InMemorySharedPreferences()
                }
            }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "mmp_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
