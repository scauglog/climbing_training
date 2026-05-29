package com.alma.climbingtraining.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class PlayerPreferences(context: Context) : PlayerNamesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun savePlayerNames(names: List<String>) {
        val jsonArray = JSONArray(names)
        prefs.edit()
            .putString(KEY_PLAYER_NAMES_JSON, jsonArray.toString())
            .apply()
    }

    override fun loadPlayerNames(): List<String> {
        val json = prefs.getString(KEY_PLAYER_NAMES_JSON, null)
        if (json.isNullOrBlank()) return emptyList()

        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS_NAME = "flying_loto_prefs"
        private const val KEY_PLAYER_NAMES_JSON = "player_names_json"
    }
}
