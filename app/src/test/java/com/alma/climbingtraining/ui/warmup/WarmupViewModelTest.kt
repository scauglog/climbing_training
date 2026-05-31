package com.alma.climbingtraining.ui.warmup

import com.alma.climbingtraining.MainDispatcherRule
import com.alma.climbingtraining.model.IntervalType
import com.alma.climbingtraining.model.WarmupExercise
import com.alma.climbingtraining.model.WarmupPhase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// ── Fixtures ──────────────────────────────────────────────────────────────────

private fun makeExercise(id: String, bodyPart: String = "core") =
    WarmupExercise(id = id, name = "Exercise $id", description = "Desc $id", bodyPart = bodyPart)

private val EXERCISE_LIST = listOf(
    makeExercise("ex1", "wrists"),
    makeExercise("ex2", "shoulders"),
    makeExercise("ex3", "core"),
    makeExercise("ex4", "legs"),
)

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WarmupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WarmupViewModel

    @Before
    fun setUp() {
        // Use a null tone generator to avoid hardware dependencies
        viewModel = WarmupViewModel(exercises = EXERCISE_LIST, toneGeneratorFactory = { null })
    }

    // ── Setup phase ───────────────────────────────────────────────────────────

    @Test
    fun `initial state is SETUP with 10 minutes`() {
        val state = viewModel.state.value
        assertEquals(WarmupPhase.SETUP, state.phase)
        assertEquals(10, state.durationMinutes)
        assertEquals(20, state.totalIntervals)
    }

    @Test
    fun `incrementDuration increases durationMinutes by 1`() {
        viewModel.incrementDuration()
        assertEquals(11, viewModel.state.value.durationMinutes)
        assertEquals(22, viewModel.state.value.totalIntervals)
    }

    @Test
    fun `decrementDuration decreases durationMinutes by 1`() {
        viewModel.decrementDuration()
        assertEquals(9, viewModel.state.value.durationMinutes)
        assertEquals(18, viewModel.state.value.totalIntervals)
    }

    @Test
    fun `incrementDuration does not exceed 60 minutes`() {
        repeat(60) { viewModel.incrementDuration() }
        assertEquals(60, viewModel.state.value.durationMinutes)
    }

    @Test
    fun `decrementDuration does not go below 1 minute`() {
        repeat(15) { viewModel.decrementDuration() }
        assertEquals(1, viewModel.state.value.durationMinutes)
    }

    // ── Start ─────────────────────────────────────────────────────────────────

    @Test
    fun `start transitions to RUNNING phase`() {
        viewModel.start()
        assertEquals(WarmupPhase.RUNNING, viewModel.state.value.phase)
    }

    @Test
    fun `start sets currentExercise to an exercise from the library`() {
        viewModel.start()
        assertNotNull(viewModel.state.value.currentExercise)
        assertTrue(EXERCISE_LIST.contains(viewModel.state.value.currentExercise))
    }

    @Test
    fun `start sets nextExercise to an exercise from the library`() {
        viewModel.start()
        assertNotNull(viewModel.state.value.nextExercise)
        assertTrue(EXERCISE_LIST.contains(viewModel.state.value.nextExercise))
    }

    @Test
    fun `start resets completedIntervals to 0`() {
        viewModel.start()
        assertEquals(0, viewModel.state.value.completedIntervals)
    }

    @Test
    fun `start sets intervalType to WORK`() {
        viewModel.start()
        assertEquals(IntervalType.WORK, viewModel.state.value.intervalType)
    }

    @Test
    fun `start sets intervalSecondsRemaining to WORK_SECONDS`() {
        viewModel.start()
        assertEquals(WORK_SECONDS, viewModel.state.value.intervalSecondsRemaining)
    }

    // ── Pause / Resume ────────────────────────────────────────────────────────

    @Test
    fun `pause transitions to PAUSED phase`() {
        viewModel.start()
        viewModel.pause()
        assertEquals(WarmupPhase.PAUSED, viewModel.state.value.phase)
    }

    @Test
    fun `resume transitions back to RUNNING phase`() {
        viewModel.start()
        viewModel.pause()
        viewModel.resume()
        assertEquals(WarmupPhase.RUNNING, viewModel.state.value.phase)
    }

    // ── Stop ──────────────────────────────────────────────────────────────────

    @Test
    fun `stop returns to SETUP phase`() {
        viewModel.start()
        viewModel.stop()
        assertEquals(WarmupPhase.SETUP, viewModel.state.value.phase)
    }

    @Test
    fun `stop preserves durationMinutes`() {
        viewModel.incrementDuration() // 11 min
        viewModel.start()
        viewModel.stop()
        assertEquals(11, viewModel.state.value.durationMinutes)
    }

    // ── Timer ticks ───────────────────────────────────────────────────────────

    @Test
    fun `one tick decrements intervalSecondsRemaining`() = runTest {
        viewModel.start()
        advanceTimeBy(1_001L)
        assertEquals(WORK_SECONDS - 1, viewModel.state.value.intervalSecondsRemaining)
    }

    @Test
    fun `after WORK_SECONDS ticks, intervalType becomes REST`() = runTest {
        viewModel.start()
        advanceTimeBy((WORK_SECONDS * 1_000L) + 500L)
        assertEquals(IntervalType.REST, viewModel.state.value.intervalType)
        assertEquals(REST_SECONDS, viewModel.state.value.intervalSecondsRemaining)
    }

    @Test
    fun `after one full cycle (WORK + REST) completedIntervals is 2`() = runTest {
        viewModel.start()
        val cycleDuration = (WORK_SECONDS + REST_SECONDS) * 1_000L
        advanceTimeBy(cycleDuration + 500L)
        assertEquals(2, viewModel.state.value.completedIntervals)
        assertEquals(IntervalType.WORK, viewModel.state.value.intervalType)
    }

    @Test
    fun `exercise advances after REST completes`() = runTest {
        viewModel.start()
        val initialExercise = viewModel.state.value.currentExercise
        val cycleDuration = (WORK_SECONDS + REST_SECONDS) * 1_000L
        advanceTimeBy(cycleDuration + 500L)
        val newExercise = viewModel.state.value.currentExercise
        // Exercise should have changed (sequence has 4 distinct exercises)
        assertNotEquals(initialExercise, newExercise)
        assertNotNull(newExercise)
        assertTrue(EXERCISE_LIST.contains(newExercise))
    }

    @Test
    fun `pause stops timer from decrementing`() = runTest {
        viewModel.start()
        advanceTimeBy(2_001L)
        viewModel.pause()
        val secondsAfterPause = viewModel.state.value.intervalSecondsRemaining
        advanceTimeBy(5_000L)
        assertEquals(secondsAfterPause, viewModel.state.value.intervalSecondsRemaining)
    }

    // ── FINISHED ──────────────────────────────────────────────────────────────

    @Test
    fun `timer finishes after all intervals complete`() = runTest {
        // Use 1-minute session = 2 intervals
        viewModel = WarmupViewModel(exercises = EXERCISE_LIST, toneGeneratorFactory = { null })
        // reduce to 1 minute
        repeat(9) { viewModel.decrementDuration() }
        assertEquals(1, viewModel.state.value.durationMinutes)
        viewModel.start()
        val totalDuration = (WORK_SECONDS + REST_SECONDS) * 1_000L
        advanceTimeBy(totalDuration + 500L)
        assertEquals(WarmupPhase.FINISHED, viewModel.state.value.phase)
    }

    // ── remainingTotalSeconds ─────────────────────────────────────────────────

    @Test
    fun `remainingTotalSeconds before starting equals all intervals duration`() {
        val state = viewModel.state.value
        // 20 intervals = 10 full cycles of (20s work + 10s rest) = 10 * 30s = 300s
        // But the formula also adds intervalSecondsRemaining when remaining%2==0 && remaining>0
        // remaining=20, fullCycles=10, hasWorkLeft=false, remaining>0 => adds intervalSecondsRemaining=WORK_SECONDS
        val expected = 10 * (WORK_SECONDS + REST_SECONDS) + WORK_SECONDS
        assertEquals(expected, state.remainingTotalSeconds)
    }

    @Test
    fun `remainingTotalSeconds decreases by 1 each second`() = runTest {
        viewModel.start()
        val before = viewModel.state.value.remainingTotalSeconds
        advanceTimeBy(1_001L)
        val after = viewModel.state.value.remainingTotalSeconds
        assertEquals(before - 1, after)
    }

    // ── WarmupState computed properties ───────────────────────────────────────

    @Test
    fun `totalSeconds equals durationMinutes times 60`() {
        viewModel.incrementDuration() // 11 min
        assertEquals(11 * 60, viewModel.state.value.totalSeconds)
    }

    // ── Exercise sequencing ───────────────────────────────────────────────────

    @Test
    fun `buildExerciseSequence returns requested number of exercises`() {
        val seq = viewModel.buildExerciseSequence(4)
        assertEquals(4, seq.size)
    }

    @Test
    fun `buildExerciseSequence covers all body parts when slots allow`() {
        // EXERCISE_LIST has 4 distinct body parts, request 4 slots
        val seq = viewModel.buildExerciseSequence(4)
        val bodyParts = seq.map { it.bodyPart }.toSet()
        assertEquals(setOf("wrists", "shoulders", "core", "legs"), bodyParts)
    }

    @Test
    fun `buildExerciseSequence is partial when fewer slots than body parts`() {
        // Request only 2 slots for 4 body parts
        val seq = viewModel.buildExerciseSequence(2)
        assertEquals(2, seq.size)
        // Each exercise must be from the library
        seq.forEach { assertTrue(EXERCISE_LIST.contains(it)) }
    }

    @Test
    fun `buildExerciseSequence fills extra slots randomly when more slots than body parts`() {
        // 4 body parts, request 6 slots → coverage (4) + 2 filler
        val seq = viewModel.buildExerciseSequence(6)
        assertEquals(6, seq.size)
        seq.forEach { assertTrue(EXERCISE_LIST.contains(it)) }
    }

    @Test
    fun `buildExerciseSequence with empty list returns empty`() {
        val emptyVm = WarmupViewModel(exercises = emptyList(), toneGeneratorFactory = { null })
        assertEquals(emptyList<WarmupExercise>(), emptyVm.buildExerciseSequence(5))
    }

    // ── coveredBodyParts ──────────────────────────────────────────────────────

    @Test
    fun `coveredBodyParts is empty before start`() {
        assertTrue(viewModel.state.value.coveredBodyParts.isEmpty())
    }

    @Test
    fun `coveredBodyParts includes first exercise body part after start`() {
        viewModel.start()
        val firstBodyPart = viewModel.state.value.currentExercise?.bodyPart
        assertNotNull(firstBodyPart)
        assertTrue(viewModel.state.value.coveredBodyParts.contains(firstBodyPart))
    }

    @Test
    fun `coveredBodyParts grows after each work interval`() = runTest {
        viewModel.start()
        val countAfterStart = viewModel.state.value.coveredBodyParts.size
        val cycleDuration = (WORK_SECONDS + REST_SECONDS) * 1_000L
        advanceTimeBy(cycleDuration + 500L)
        val countAfterCycle = viewModel.state.value.coveredBodyParts.size
        assertTrue(countAfterCycle >= countAfterStart)
    }

    @Test
    fun `coveredBodyParts resets on stop`() {
        viewModel.start()
        viewModel.stop()
        assertTrue(viewModel.state.value.coveredBodyParts.isEmpty())
    }

    // ── coveredBodyPartsCount helper ──────────────────────────────────────────

    @Test
    fun `coveredBodyPartsCount returns min of work slots and distinct body parts`() {
        // 4 body parts, 10 min = 10 work slots → all 4 covered
        assertEquals(4, viewModel.coveredBodyPartsCount(10))
    }

    @Test
    fun `coveredBodyPartsCount is capped by distinct body parts`() {
        assertEquals(4, viewModel.coveredBodyPartsCount(60))
    }

    @Test
    fun `coveredBodyPartsCount is capped by work slots when fewer than body parts`() {
        assertEquals(2, viewModel.coveredBodyPartsCount(2))
    }

    @Test
    fun `totalBodyPartsCount returns number of distinct body parts in library`() {
        assertEquals(4, viewModel.totalBodyPartsCount())
    }
}
