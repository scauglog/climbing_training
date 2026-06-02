package com.alma.climbingtraining.data

import android.content.Context
import com.alma.climbingtraining.model.WarmupExercise
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

class WarmupExerciseDataSource(private val context: Context) {

    companion object {
        private const val WARMUP_DIR = "warmup"
        private const val FALLBACK_PREFIX = "en_"
        private const val CUSTOM_FILE = "custom_warmup.json"

        fun languagePrefix(locale: Locale = Locale.getDefault()): String =
            "${locale.language.lowercase()}_"
    }

    private val customFile: File get() = File(context.filesDir, CUSTOM_FILE)

    // ── Public API ────────────────────────────────────────────────────────────

    fun loadExercises(locale: Locale = Locale.getDefault()): List<WarmupExercise> {
        if (customFile.exists()) {
            return try {
                parseJsonArray(customFile.readText())
            } catch (e: Exception) {
                // Corrupt custom file — delete and fall back to built-in assets.
                customFile.delete()
                loadFromAssets(locale)
            }
        }
        return loadFromAssets(locale)
    }

    fun hasCustomLibrary(): Boolean = customFile.exists()

    /**
     * Replaces the custom warmup library with the provided JSON string.
     * Returns null on success, or an error message if the JSON is invalid.
     */
    fun importCustomLibrary(json: String): String? {
        return try {
            val parsed = parseJsonArray(json)
            if (parsed.isEmpty()) return "No valid warmup exercises found in the file."
            customFile.writeText(json)
            null
        } catch (e: Exception) {
            "Invalid JSON: ${e.message}"
        }
    }

    fun clearCustomLibrary() {
        customFile.delete()
    }

    fun exportCurrentLibraryAsJson(): String {
        val exercises = loadExercises()
        return warmupExercisesToJson(exercises)
    }

    // ── Internal loading ──────────────────────────────────────────────────────

    private fun loadFromAssets(locale: Locale): List<WarmupExercise> {
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
                parseJsonArray(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    private fun parseJsonArray(json: String): List<WarmupExercise> {
        val array = JSONArray(json)
        return (0 until array.length()).mapNotNull { i ->
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
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    private fun warmupExercisesToJson(exercises: List<WarmupExercise>): String {
        val array = JSONArray()
        exercises.forEach { ex ->
            val obj = JSONObject()
            obj.put("id", ex.id)
            obj.put("name", ex.name)
            obj.put("description", ex.description)
            obj.put("bodyPart", ex.bodyPart)
            array.put(obj)
        }
        return array.toString(2)
    }
}
