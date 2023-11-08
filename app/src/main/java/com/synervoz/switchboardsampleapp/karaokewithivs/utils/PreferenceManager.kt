package com.synervoz.switchboardsampleapp.karaokewithivs.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceConstants {
    const val PREFERENCES_INITIALIZED = "initialized"
    const val INGEST_SERVER = "INGEST_SERVER"
    const val STREAM_KEY = "STREAM_KEY"
    const val TOKEN = "TOKEN"
}

class PreferenceManager() {

    companion object {

        private lateinit var globalSettings: SharedPreferences

        init {
            loadGlobalSettings()
            if (!getGlobalBooleanPreference(PreferenceConstants.PREFERENCES_INITIALIZED)) {
                initializePreferences()
            }
        }

        fun loadGlobalSettings() {
            globalSettings = ContextHolder.activity.getSharedPreferences("IVSSampleApp", Context.MODE_PRIVATE)
        }

        private fun initializePreferences() {
            setGlobalBooleanPreference(PreferenceConstants.PREFERENCES_INITIALIZED, true)
            setGlobalStringPreference(PreferenceConstants.INGEST_SERVER, null)
            setGlobalStringPreference(PreferenceConstants.STREAM_KEY, null)
            setGlobalStringPreference(PreferenceConstants.TOKEN, null)
        }

        fun getGlobalStringPreference(key: String?): String? {
            return globalSettings.getString(key, null)
        }

        fun getGlobalBooleanPreference(key: String?): Boolean {
            return globalSettings.getBoolean(key, false)
        }

        fun doesGlobalBooleanPreferenceExist(key: String?): Boolean {
            return globalSettings.contains(key)
        }

        fun getGlobalIntPreference(key: String?): Int {
            return globalSettings.getInt(key, 0)
        }

        fun setGlobalStringPreference(key: String?, value: String?) {
            globalSettings.edit().putString(key, value).commit()
        }

        fun setGlobalIntPreference(key: String?, value: Int) {
            globalSettings.edit().putInt(key, value).commit()
        }

        fun setGlobalBooleanPreference(key: String?, value: Boolean) {
            globalSettings.edit().putBoolean(key, value).commit()
        }
    }
}
