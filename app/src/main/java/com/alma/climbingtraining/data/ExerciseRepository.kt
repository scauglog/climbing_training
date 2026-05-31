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
import java.util.Locale

class ExerciseRepository(private val context: Context) : ExerciseDataSource {

    companion object {
        private const val EXERCISES_DIR = "exercises"
        private const val FALLBACK_PREFIX = "en_"

        /** Returns the two-letter language prefix for the current locale, e.g. "fr_". */
        fun languagePrefix(locale: Locale = Locale.getDefault()): String =
            "${locale.language.lowercase()}_"
    }

    override fun loadExercises(): List<Exercise> = loadExercisesForLocale(Locale.getDefault())

    /**
     * Loads exercises from all JSON files in `assets/exercises/` whose filename starts with
     * the prefix for [locale] (e.g. `fr_`). Falls back to the English prefix (`en_`) when no
     * file matching the requested locale exists. This selection is transparent to callers.
     */
    fun loadExercisesForLocale(locale: Locale): List<Exercise> {
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
            // fall back to English
            allFiles.filter { it.endsWith(".json") && it.startsWith(FALLBACK_PREFIX) }
        }

        return filesToLoad.flatMap { file ->
            try {
                val json = assetManager.open("$EXERCISES_DIR/$file").bufferedReader().readText()
                val array = JSONArray(json)
                (0 until array.length()).mapNotNull { i -> parseExercise(array.getJSONObject(i)) }
            } catch (e: Exception) {
                emptyList()
            }
        }
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

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { getString(it) }

    private inline fun <reified T : Enum<T>> parseEnum(value: String): T? =
        enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) }
}
