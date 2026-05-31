package com.alma.climbingtraining.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.alma.climbingtraining.model.Discipline
import com.alma.climbingtraining.model.EnergySystem
import com.alma.climbingtraining.model.ExerciseLevel
import com.alma.climbingtraining.model.TargetAudience
import com.alma.climbingtraining.model.TechniqueFocus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class ExerciseRepositoryTest {

    private lateinit var repository: ExerciseRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        repository = ExerciseRepository(context)
    }

    // ── languagePrefix helper ─────────────────────────────────────────────────

    @Test
    fun `languagePrefix returns lowercase language code with trailing underscore`() {
        assertEquals("en_", ExerciseRepository.languagePrefix(Locale.ENGLISH))
        assertEquals("fr_", ExerciseRepository.languagePrefix(Locale.FRENCH))
        assertEquals("de_", ExerciseRepository.languagePrefix(Locale.GERMAN))
    }

    @Test
    fun `languagePrefix is case-insensitive for locale input`() {
        assertEquals("fr_", ExerciseRepository.languagePrefix(Locale("FR")))
        assertEquals("en_", ExerciseRepository.languagePrefix(Locale("EN")))
    }

    // ── loadExercisesForLocale – English ──────────────────────────────────────

    @Test
    fun `loadExercisesForLocale returns non-empty list for English locale`() {
        val exercises = repository.loadExercisesForLocale(Locale.ENGLISH)
        assertTrue("Expected at least one English exercise", exercises.isNotEmpty())
    }

    @Test
    fun `loadExercisesForLocale returns English exercises for English locale`() {
        val enExercises = repository.loadExercisesForLocale(Locale.ENGLISH)
        val frExercises = repository.loadExercisesForLocale(Locale.FRENCH)
        // IDs should differ between locales (each locale has its own content)
        val enIds = enExercises.map { it.id }.toSet()
        val frIds = frExercises.map { it.id }.toSet()
        assertNotEquals(enIds, frIds)
    }

    // ── loadExercisesForLocale – French ───────────────────────────────────────

    @Test
    fun `loadExercisesForLocale returns non-empty list for French locale`() {
        val exercises = repository.loadExercisesForLocale(Locale.FRENCH)
        assertTrue("Expected at least one French exercise", exercises.isNotEmpty())
    }

    // ── loadExercisesForLocale – Fallback ─────────────────────────────────────

    @Test
    fun `loadExercisesForLocale falls back to English for unknown locale`() {
        val enExercises = repository.loadExercisesForLocale(Locale.ENGLISH)
        val unknownLocaleExercises = repository.loadExercisesForLocale(Locale("zz"))
        // Fallback must return the same content as English
        assertEquals(enExercises.map { it.id }.toSet(), unknownLocaleExercises.map { it.id }.toSet())
    }

    @Test
    fun `loadExercisesForLocale fallback is non-empty`() {
        val exercises = repository.loadExercisesForLocale(Locale("xx"))
        assertTrue("Fallback to English must produce at least one exercise", exercises.isNotEmpty())
    }

    // ── loadExercises uses default locale ─────────────────────────────────────

    @Test
    fun `loadExercises returns non-empty list`() {
        val exercises = repository.loadExercises()
        assertTrue("loadExercises must return at least one exercise", exercises.isNotEmpty())
    }

    @Test
    fun `loadExercises can be called multiple times and returns the same result`() {
        val first = repository.loadExercises()
        val second = repository.loadExercises()
        assertEquals(first.size, second.size)
        assertEquals(first.map { it.id }.toSet(), second.map { it.id }.toSet())
    }

    // ── Data integrity (runs against English locale) ──────────────────────────

    @Test
    fun `exercises have non-blank ids and names`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            assertTrue("id must not be blank", exercise.id.isNotBlank())
            assertTrue("name must not be blank", exercise.name.isNotBlank())
        }
    }

    @Test
    fun `exercises have non-empty targetAudience`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            assertTrue("'${exercise.id}' must have at least one target audience",
                exercise.targetAudience.isNotEmpty())
        }
    }

    @Test
    fun `exercises have non-empty energySystems`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            assertTrue("'${exercise.id}' must have at least one energy system",
                exercise.energySystems.isNotEmpty())
        }
    }

    @Test
    fun `exercises have non-empty disciplines`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            assertTrue("'${exercise.id}' must have at least one discipline",
                exercise.disciplines.isNotEmpty())
        }
    }

    @Test
    fun `exercises have non-blank descriptions`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            assertTrue("'${exercise.id}' must have a non-blank description",
                exercise.description.isNotBlank())
        }
    }

    @Test
    fun `exercises have unique ids within a locale`() {
        val ids = repository.loadExercisesForLocale(Locale.ENGLISH).map { it.id }
        assertEquals("All ids must be unique", ids.distinct().size, ids.size)
    }

    @Test
    fun `exercises use valid TargetAudience values`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            exercise.targetAudience.forEach { assertTrue(TargetAudience.entries.contains(it)) }
        }
    }

    @Test
    fun `exercises use valid EnergySystem values`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            exercise.energySystems.forEach { assertTrue(EnergySystem.entries.contains(it)) }
        }
    }

    @Test
    fun `exercises use valid Discipline values`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            exercise.disciplines.forEach { assertTrue(Discipline.entries.contains(it)) }
        }
    }

    @Test
    fun `exercises use valid ExerciseLevel values`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            assertTrue(ExerciseLevel.entries.contains(exercise.level))
        }
    }

    @Test
    fun `exercises use valid TechniqueFocus values`() {
        repository.loadExercisesForLocale(Locale.ENGLISH).forEach { exercise ->
            exercise.techniqueFocus.forEach { assertTrue(TechniqueFocus.entries.contains(it)) }
        }
    }

    @Test
    fun `English exercises cover both adults and children`() {
        val exercises = repository.loadExercisesForLocale(Locale.ENGLISH)
        assertTrue(exercises.any { TargetAudience.ADULT in it.targetAudience })
        assertTrue(exercises.any { TargetAudience.CHILD in it.targetAudience })
    }

    @Test
    fun `English exercises cover multiple difficulty levels`() {
        val levels = repository.loadExercisesForLocale(Locale.ENGLISH).map { it.level }.toSet()
        assertTrue("Expected multiple levels in the English library", levels.size > 1)
    }
}
