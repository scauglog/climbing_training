package com.alma.climbingtraining.data

import android.content.Context
import com.alma.climbingtraining.model.WarmupExercise
import org.json.JSONArray
import java.util.Locale

class WarmupExerciseDataSource(private val context: Context) {

    companion object {
        private const val WARMUP_DIR = "warmup"
        private const val FALLBACK_PREFIX = "en_"

        fun languagePrefix(locale: Locale = Locale.getDefault()): String =
            "${locale.language.lowercase()}_"
    }

    fun loadExercises(locale: Locale = Locale.getDefault()): List<WarmupExercise> {
        val assetManager = context.assets
        val allFiles = try {
            assetManager.list(WARMUP_DIR) ?: emptyArray()
        } catch (e: Exception) {
            emptyArray()
        }

        val requestedPrefix = languagePrefix(locale)
        val candidateFiles = allFiles.filter { it.endsWith(".json") && it.startsWith(requestedPrefix) }
        val filesToLoad = if (candidateFiles.isNotEmpty()) {
            candidateFiles
        } else {
            allFiles.filter { it.endsWith(".json") && it.startsWith(FALLBACK_PREFIX) }
        }

        return filesToLoad.flatMap { file ->
            try {
                val json = assetManager.open("$WARMUP_DIR/$file").bufferedReader().readText()
                val array = JSONArray(json)
                (0 until array.length()).mapNotNull { i ->
                    try {
                        val obj = array.getJSONObject(i)
                        WarmupExercise(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.getString("description"),
                            bodyPart = obj.getString("bodyPart")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
