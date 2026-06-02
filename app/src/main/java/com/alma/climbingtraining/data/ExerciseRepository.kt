package com.alma.climbingtraining.data

import android.content.Context
import com.alma.climbingtraining.model.Discipline
import com.alma.climbingtraining.model.EnergySystem
import com.alma.climbingtraining.model.Exercise
import com.alma.climbingtraining.model.ExerciseLevel
import com.alma.climbingtraining.model.TargetAudience
import com.alma.climbingtraining.model.TechniqueFocus
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

class ExerciseRepository(private val context: Context) : ExerciseDataSource {

    companion object {
        private const val EXERCISES_DIR = "exercises"
        private const val FALLBACK_PREFIX = "en_"
        private const val CUSTOM_FILE = "custom_exercises.json"

        /** Returns the two-letter language prefix for the current locale, e.g. "fr_". */
        fun languagePrefix(locale: Locale = Locale.getDefault()): String =
            "${locale.language.lowercase()}_"
    }

    private val customFile: File get() = File(context.filesDir, CUSTOM_FILE)

    // ── Public API ────────────────────────────────────────────────────────────

    override fun loadExercises(): List<Exercise> = loadExercisesForLocale(Locale.getDefault())

    fun hasCustomLibrary(): Boolean = customFile.exists()

    /**
     * Replaces the custom library with the provided JSON string.
     * Returns null on success, or an error message if the JSON is invalid.
     */
    fun importCustomLibrary(json: String): String? {
        return try {
            // Validate by parsing
            val parsed = parseJsonArray(json)
            if (parsed.isEmpty()) return "No valid exercises found in the file."
            customFile.writeText(json)
            null
        } catch (e: Exception) {
            "Invalid JSON: ${e.message}"
        }
    }

    fun clearCustomLibrary() {
        customFile.delete()
    }

    /**
     * Exports the currently active library (custom if present, else built-in for the current
     * locale) as a pretty-printed JSON string ready for sharing or saving.
     */
    fun exportCurrentLibraryAsJson(): String {
        val exercises = loadExercises()
        return exercisesToJson(exercises)
    }

    // ── Internal loading ──────────────────────────────────────────────────────

    fun loadExercisesForLocale(locale: Locale): List<Exercise> {
        // Custom library takes full precedence when healthy; fall back to built-in if corrupt.
        if (customFile.exists()) {
            return try {
                parseJsonArray(customFile.readText())
            } catch (e: Exception) {
                // Corrupt custom file — delete it and fall back to built-in assets transparently.
                customFile.delete()
                loadFromAssets(locale)
            }
        }
        return loadFromAssets(locale)
    }

    private fun loadFromAssets(locale: Locale): List<Exercise> {
        val assetManager = context.assets
        val allFiles = try {
            assetManager.list(EXERCISES_DIR) ?: emptyArray()
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
                val json = assetManager.open("$EXERCISES_DIR/$file").bufferedReader().readText()
                parseJsonArray(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    private fun parseJsonArray(json: String): List<Exercise> {
        val array = JSONArray(json)
        return (0 until array.length()).mapNotNull { i -> parseExercise(array.getJSONObject(i)) }
    }

    private fun parseExercise(obj: JSONObject): Exercise? = try {
        Exercise(
            id = obj.getString("id"),
            name = obj.getString("name"),
            targetAudience = obj.getJSONArray("targetAudience").toStringList()
                .mapNotNull { parseEnum<TargetAudience>(it) },
            energySystems = obj.getJSONArray("energySystems").toStringList()
                .mapNotNull { parseEnum<EnergySystem>(it) },
            disciplines = obj.getJSONArray("disciplines").toStringList()
                .mapNotNull { parseEnum<Discipline>(it) },
            level = parseEnum<ExerciseLevel>(obj.getString("level")) ?: ExerciseLevel.BEGINNER,
            techniqueFocus = obj.getJSONArray("techniqueFocus").toStringList()
                .mapNotNull { parseEnum<TechniqueFocus>(it) },
            description = obj.getString("description"),
            imageAsset = if (obj.has("imageAsset")) obj.optString("imageAsset").takeIf { it.isNotBlank() } else null
        )
    } catch (e: Exception) {
        null
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    private fun exercisesToJson(exercises: List<Exercise>): String {
        val array = JSONArray()
        exercises.forEach { ex ->
            val obj = JSONObject()
            obj.put("id", ex.id)
            obj.put("name", ex.name)
            obj.put("targetAudience", JSONArray(ex.targetAudience.map { it.name.lowercase() }))
            obj.put("energySystems", JSONArray(ex.energySystems.map { it.name.lowercase() }))
            obj.put("disciplines", JSONArray(ex.disciplines.map { it.name.lowercase() }))
            obj.put("level", ex.level.name.lowercase())
            obj.put("techniqueFocus", JSONArray(ex.techniqueFocus.map { it.name.lowercase() }))
            obj.put("description", ex.description)
            obj.put("imageAsset", ex.imageAsset)
            array.put(obj)
        }
        // Pretty-print with 2-space indent
        return array.toString(2)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { getString(it) }

    private inline fun <reified T : Enum<T>> parseEnum(value: String): T? =
        enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) }
}
