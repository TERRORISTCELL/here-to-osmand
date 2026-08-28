package com.helper.heretoosmand

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class RedirectLogEntry(
    val timestamp: Long,
    val sourceUri: String,
    val destFormatted: String,
    val mode: String,
    val success: Boolean,
    val errorMsg: String? = null
)

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("here_to_osmand_prefs", Context.MODE_PRIVATE)

    var isRedirectEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var preferredOsmAndPackage: String
        get() = prefs.getString(KEY_PREFERRED_PKG, OsmAndIntentBuilder.OPTION_AUTO) ?: OsmAndIntentBuilder.OPTION_AUTO
        set(value) = prefs.edit().putString(KEY_PREFERRED_PKG, value).apply()

    var defaultNavigationMode: String
        get() = prefs.getString(KEY_DEFAULT_MODE, "AUTO") ?: "AUTO"
        set(value) = prefs.edit().putString(KEY_DEFAULT_MODE, value).apply()

    var showToastNotification: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TOAST, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TOAST, value).apply()

    fun getRecentLogs(): List<RedirectLogEntry> {
        val rawJson = prefs.getString(KEY_LOGS, "[]") ?: "[]"
        val list = mutableListOf<RedirectLogEntry>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RedirectLogEntry(
                        timestamp = obj.optLong("timestamp"),
                        sourceUri = obj.optString("sourceUri"),
                        destFormatted = obj.optString("destFormatted"),
                        mode = obj.optString("mode"),
                        success = obj.optBoolean("success", true),
                        errorMsg = if (obj.has("errorMsg")) obj.optString("errorMsg") else null
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun addLogEntry(entry: RedirectLogEntry) {
        val currentLogs = getRecentLogs().toMutableList()
        currentLogs.add(0, entry)
        if (currentLogs.size > 20) {
            currentLogs.subList(20, currentLogs.size).clear()
        }

        val array = JSONArray()
        for (item in currentLogs) {
            val obj = JSONObject()
            obj.put("timestamp", item.timestamp)
            obj.put("sourceUri", item.sourceUri)
            obj.put("destFormatted", item.destFormatted)
            obj.put("mode", item.mode)
            obj.put("success", item.success)
            item.errorMsg?.let { obj.put("errorMsg", it) }
            array.put(obj)
        }

        prefs.edit().putString(KEY_LOGS, array.toString()).apply()
    }

    fun clearLogs() {
        prefs.edit().remove(KEY_LOGS).apply()
    }

    companion object {
        private const val KEY_ENABLED = "key_redirect_enabled"
        private const val KEY_PREFERRED_PKG = "key_preferred_pkg"
        private const val KEY_DEFAULT_MODE = "key_default_mode"
        private const val KEY_SHOW_TOAST = "key_show_toast"
        private const val KEY_LOGS = "key_redirect_logs"
    }
}
