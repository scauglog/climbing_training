package com.alma.climbingtraining.ui.randomexercise

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.alma.climbingtraining.MainDispatcherRule
import com.alma.climbingtraining.data.ExerciseDataSource
import com.alma.climbingtraining.model.Discipline
import com.alma.climbingtraining.model.EnergySystem
import com.alma.climbingtraining.model.Exercise
import com.alma.climbingtraining.model.ExerciseLevel
import com.alma.climbingtraining.model.TargetAudience
import com.alma.climbingtraining.model.TechniqueFocus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// ── Fixtures ──────────────────────────────────────────────────────────────────

private fun makeExercise(
    id: String = "ex_default",
    name: String = "Exercise Default",
    targetAudience: List<TargetAudience> = listOf(TargetAudience.ADULT),
    energySystems: List<EnergySystem> = listOf(EnergySystem.ENDURANCE),
    disciplines: List<Discipline> = listOf(Discipline.BOULDER),
    level: ExerciseLevel = ExerciseLevel.BEGINNER,
    techniqueFocus: List<TechniqueFocus> = listOf(TechniqueFocus.OTHER),
    description: String = "Description for $id"
) = Exercise(id, name, targetAudience, energySystems, disciplines, level, techniqueFocus, description)

private val EXERCISE_ADULT_BOULDER_BEGINNER = makeExercise(
    id = "adult_boulder_beginner",
    targetAudience = listOf(TargetAudience.ADULT),
    disciplines = listOf(Discipline.BOULDER),
    level = ExerciseLevel.BEGINNER,
    energySystems = listOf(EnergySystem.ENDURANCE),
    techniqueFocus = listOf(TechniqueFocus.FOOTWORK)
)
private val EXERCISE_CHILD_LEAD_INTERMEDIATE = makeExercise(
    id = "child_lead_intermediate",
    targetAudience = listOf(TargetAudience.CHILD),
    disciplines = listOf(Discipline.LEAD),
    level = ExerciseLevel.INTERMEDIATE,
    energySystems = listOf(EnergySystem.STRENGTH_ENDURANCE),
    techniqueFocus = listOf(TechniqueFocus.HANDWORK)
)
private val EXERCISE_ADULT_EXPERT_STRENGTH = makeExercise(
    id = "adult_expert_strength",
    targetAudience = listOf(TargetAudience.ADULT),
    disciplines = listOf(Discipline.BOULDER),
    level = ExerciseLevel.EXPERT,
    energySystems = listOf(EnergySystem.PURE_STRENGTH),
    techniqueFocus = listOf(TechniqueFocus.HANDWORK)
)
private val EXERCISE_BOTH_AUDIENCES = makeExercise(
    id = "both_audiences",
    targetAudience = listOf(TargetAudience.CHILD, TargetAudience.ADULT),
    disciplines = listOf(Discipline.TOP_ROPE),
    level = ExerciseLevel.BEGINNER,
    energySystems = listOf(EnergySystem.ENDURANCE),
    techniqueFocus = listOf(TechniqueFocus.BODY_MOVEMENT)
)

private val ALL_EXERCISES = listOf(
    EXERCISE_ADULT_BOULDER_BEGINNER,
    EXERCISE_CHILD_LEAD_INTERMEDIATE,
    EXERCISE_ADULT_EXPERT_STRENGTH,
    EXERCISE_BOTH_AUDIENCES
)

class FakeExerciseDataSource(private val exercises: List<Exercise> = ALL_EXERCISES) : ExerciseDataSource {
    override fun loadExercises(): List<Exercise> = exercises
}

// ── Tests ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RandomExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RandomExerciseViewModel

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = RandomExerciseViewModel(application, FakeExerciseDataSource())
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial phase is FILTER`() {
        assertEquals(RandomExercisePhase.FILTER, viewModel.state.value.phase)
    }

    @Test
    fun `initial filter is empty`() {
        val filter = viewModel.state.value.filter
        assertTrue(filter.targetAudience.isEmpty())
        assertTrue(filter.energySystems.isEmpty())
        assertTrue(filter.disciplines.isEmpty())
        assertTrue(filter.levels.isEmpty())
        assertTrue(filter.techniqueFocus.isEmpty())
    }

    @Test
    fun `initial currentExercise is null`() {
        assertNull(viewModel.state.value.currentExercise)
    }

    @Test
    fun `initial noMatchFound is false`() {
        assertFalse(viewModel.state.value.noMatchFound)
    }

    // ── Filter toggles ────────────────────────────────────────────────────────

    @Test
    fun `toggleTargetAudience adds value to filter`() {
        viewModel.toggleTargetAudience(TargetAudience.ADULT)
        assertTrue(TargetAudience.ADULT in viewModel.state.value.filter.targetAudience)
    }

    @Test
    fun `toggleTargetAudience removes already-selected value`() {
        viewModel.toggleTargetAudience(TargetAudience.ADULT)
        viewModel.toggleTargetAudience(TargetAudience.ADULT)
        assertFalse(TargetAudience.ADULT in viewModel.state.value.filter.targetAudience)
    }

    @Test
    fun `toggleEnergySystem adds value to filter`() {
        viewModel.toggleEnergySystem(EnergySystem.PURE_STRENGTH)
        assertTrue(EnergySystem.PURE_STRENGTH in viewModel.state.value.filter.energySystems)
    }

    @Test
    fun `toggleEnergySystem removes already-selected value`() {
        viewModel.toggleEnergySystem(EnergySystem.PURE_STRENGTH)
        viewModel.toggleEnergySystem(EnergySystem.PURE_STRENGTH)
        assertFalse(EnergySystem.PURE_STRENGTH in viewModel.state.value.filter.energySystems)
    }

    @Test
    fun `toggleDiscipline adds value to filter`() {
        viewModel.toggleDiscipline(Discipline.LEAD)
        assertTrue(Discipline.LEAD in viewModel.state.value.filter.disciplines)
    }

    @Test
    fun `toggleDiscipline removes already-selected value`() {
        viewModel.toggleDiscipline(Discipline.LEAD)
        viewModel.toggleDiscipline(Discipline.LEAD)
        assertFalse(Discipline.LEAD in viewModel.state.value.filter.disciplines)
    }

    @Test
    fun `toggleLevel adds value to filter`() {
        viewModel.toggleLevel(ExerciseLevel.ADVANCED)
        assertTrue(ExerciseLevel.ADVANCED in viewModel.state.value.filter.levels)
    }

    @Test
    fun `toggleLevel removes already-selected value`() {
        viewModel.toggleLevel(ExerciseLevel.ADVANCED)
        viewModel.toggleLevel(ExerciseLevel.ADVANCED)
        assertFalse(ExerciseLevel.ADVANCED in viewModel.state.value.filter.levels)
    }

    @Test
    fun `toggleTechniqueFocus adds value to filter`() {
        viewModel.toggleTechniqueFocus(TechniqueFocus.FOOTWORK)
        assertTrue(TechniqueFocus.FOOTWORK in viewModel.state.value.filter.techniqueFocus)
    }

    @Test
    fun `toggleTechniqueFocus removes already-selected value`() {
        viewModel.toggleTechniqueFocus(TechniqueFocus.FOOTWORK)
        viewModel.toggleTechniqueFocus(TechniqueFocus.FOOTWORK)
        assertFalse(TechniqueFocus.FOOTWORK in viewModel.state.value.filter.techniqueFocus)
    }

    @Test
    fun `multiple filters can be active simultaneously`() {
        viewModel.toggleTargetAudience(TargetAudience.ADULT)
        viewModel.toggleDiscipline(Discipline.BOULDER)
        viewModel.toggleLevel(ExerciseLevel.BEGINNER)

        val filter = viewModel.state.value.filter
        assertTrue(TargetAudience.ADULT in filter.targetAudience)
        assertTrue(Discipline.BOULDER in filter.disciplines)
        assertTrue(ExerciseLevel.BEGINNER in filter.levels)
    }

    // ── drawExercise ──────────────────────────────────────────────────────────

    @Test
    fun `drawExercise with no filters transitions to RESULT phase`() = runTest {
        viewModel.drawExercise()
        assertEquals(RandomExercisePhase.RESULT, viewModel.state.value.phase)
    }

    @Test
    fun `drawExercise with no filters sets currentExercise from the library`() = runTest {
        viewModel.drawExercise()
        val exercise = viewModel.state.value.currentExercise
        assertNotNull(exercise)
        assertTrue(ALL_EXERCISES.contains(exercise))
    }

    @Test
    fun `drawExercise respects targetAudience filter - only adult exercises`() = runTest {
        viewModel.toggleTargetAudience(TargetAudience.ADULT)
        viewModel.drawExercise()
        val exercise = viewModel.state.value.currentExercise!!
        assertTrue(TargetAudience.ADULT in exercise.targetAudience)
    }

    @Test
    fun `drawExercise respects discipline filter`() = runTest {
        viewModel.toggleDiscipline(Discipline.LEAD)
        viewModel.drawExercise()
        val exercise = viewModel.state.value.currentExercise!!
        assertTrue(Discipline.LEAD in exercise.disciplines)
    }

    @Test
    fun `drawExercise respects level filter`() = runTest {
        viewModel.toggleLevel(ExerciseLevel.EXPERT)
        viewModel.drawExercise()
        val exercise = viewModel.state.value.currentExercise!!
        assertEquals(ExerciseLevel.EXPERT, exercise.level)
    }

    @Test
    fun `drawExercise respects energySystem filter`() = runTest {
        viewModel.toggleEnergySystem(EnergySystem.PURE_STRENGTH)
        viewModel.drawExercise()
        val exercise = viewModel.state.value.currentExercise!!
        assertTrue(EnergySystem.PURE_STRENGTH in exercise.energySystems)
    }

    @Test
    fun `drawExercise respects techniqueFocus filter`() = runTest {
        viewModel.toggleTechniqueFocus(TechniqueFocus.BODY_MOVEMENT)
        viewModel.drawExercise()
        val exercise = viewModel.state.value.currentExercise!!
        assertTrue(TechniqueFocus.BODY_MOVEMENT in exercise.techniqueFocus)
    }

    @Test
    fun `drawExercise applies AND logic across different filter categories`() = runTest {
        // Only ADULT + BOULDER => matches EXERCISE_ADULT_BOULDER_BEGINNER and EXERCISE_ADULT_EXPERT_STRENGTH
        viewModel.toggleTargetAudience(TargetAudience.ADULT)
        viewModel.toggleDiscipline(Discipline.BOULDER)
        viewModel.drawExercise()

        val exercise = viewModel.state.value.currentExercise!!
        assertTrue(TargetAudience.ADULT in exercise.targetAudience)
        assertTrue(Discipline.BOULDER in exercise.disciplines)
    }

    @Test
    fun `drawExercise applies OR logic within same filter category`() = runTest {
        // Both BEGINNER and EXPERT => matches both audience exercises
        viewModel.toggleLevel(ExerciseLevel.BEGINNER)
        viewModel.toggleLevel(ExerciseLevel.EXPERT)

        // Run several draws to confirm both levels can be returned
        val drawnLevels = mutableSetOf<ExerciseLevel>()
        repeat(50) {
            viewModel.drawExercise()
            viewModel.state.value.currentExercise?.let { drawnLevels.add(it.level) }
        }
        assertTrue(ExerciseLevel.BEGINNER in drawnLevels)
        assertTrue(ExerciseLevel.EXPERT in drawnLevels)
    }

    @Test
    fun `drawExercise sets noMatchFound when no exercises match the filter`() = runTest {
        // CHILD + EXPERT combination has no match in the fake library
        viewModel.toggleTargetAudience(TargetAudience.CHILD)
        viewModel.toggleLevel(ExerciseLevel.EXPERT)
        viewModel.drawExercise()

        assertTrue(viewModel.state.value.noMatchFound)
        assertEquals(RandomExercisePhase.FILTER, viewModel.state.value.phase)
    }

    @Test
    fun `drawExercise does not change phase when no match found`() = runTest {
        viewModel.toggleLevel(ExerciseLevel.ADVANCED) // no ADVANCED exercise in fake library
        viewModel.drawExercise()
        assertEquals(RandomExercisePhase.FILTER, viewModel.state.value.phase)
    }

    // ── drawAgain ─────────────────────────────────────────────────────────────

    @Test
    fun `drawAgain stays in RESULT phase`() = runTest {
        viewModel.drawExercise()
        viewModel.drawAgain()
        assertEquals(RandomExercisePhase.RESULT, viewModel.state.value.phase)
    }

    @Test
    fun `drawAgain replaces currentExercise with one matching the same filter`() = runTest {
        viewModel.toggleDiscipline(Discipline.BOULDER)
        viewModel.drawExercise()
        viewModel.drawAgain()

        val exercise = viewModel.state.value.currentExercise!!
        assertTrue(Discipline.BOULDER in exercise.disciplines)
    }

    @Test
    fun `drawAgain transitions back to FILTER when filter no longer matches any exercise`() = runTest {
        // Start with no filter (draw succeeds), then toggle a filter with no match and call drawAgain
        viewModel.drawExercise()
        viewModel.toggleLevel(ExerciseLevel.ADVANCED) // no ADVANCED in fake library
        viewModel.drawAgain()

        assertEquals(RandomExercisePhase.FILTER, viewModel.state.value.phase)
        assertTrue(viewModel.state.value.noMatchFound)
    }

    // ── clearNoMatchFound ─────────────────────────────────────────────────────

    @Test
    fun `clearNoMatchFound resets the flag to false`() = runTest {
        viewModel.toggleLevel(ExerciseLevel.ADVANCED)
        viewModel.drawExercise()
        assertTrue(viewModel.state.value.noMatchFound)

        viewModel.clearNoMatchFound()
        assertFalse(viewModel.state.value.noMatchFound)
    }

    // ── changeFilters ─────────────────────────────────────────────────────────

    @Test
    fun `changeFilters transitions back to FILTER phase`() = runTest {
        viewModel.drawExercise()
        assertEquals(RandomExercisePhase.RESULT, viewModel.state.value.phase)

        viewModel.changeFilters()
        assertEquals(RandomExercisePhase.FILTER, viewModel.state.value.phase)
    }

    @Test
    fun `changeFilters clears currentExercise`() = runTest {
        viewModel.drawExercise()
        assertNotNull(viewModel.state.value.currentExercise)

        viewModel.changeFilters()
        assertNull(viewModel.state.value.currentExercise)
    }

    @Test
    fun `changeFilters preserves the active filter`() = runTest {
        viewModel.toggleTargetAudience(TargetAudience.ADULT)
        viewModel.toggleDiscipline(Discipline.BOULDER)
        viewModel.drawExercise()
        viewModel.changeFilters()

        val filter = viewModel.state.value.filter
        assertTrue(TargetAudience.ADULT in filter.targetAudience)
        assertTrue(Discipline.BOULDER in filter.disciplines)
    }
}
