package com.alma.climbingtraining.ui.warmup

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alma.climbingtraining.data.WarmupExerciseDataSource
import com.alma.climbingtraining.model.IntervalType
import com.alma.climbingtraining.model.WarmupExercise
import com.alma.climbingtraining.model.WarmupPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val WORK_SECONDS = 20
const val REST_SECONDS = 10

data class WarmupState(
    val phase: WarmupPhase = WarmupPhase.SETUP,
    val durationMinutes: Int = 10,
    val totalIntervals: Int = 20,
    val completedIntervals: Int = 0,
    val intervalType: IntervalType = IntervalType.WORK,
    val intervalSecondsRemaining: Int = WORK_SECONDS,
    val currentExercise: WarmupExercise? = null,
    val nextExercise: WarmupExercise? = null,
    val coveredBodyParts: Set<String> = emptySet()
) {
    val totalSeconds: Int get() = durationMinutes * 60
    val elapsedSeconds: Int get() {
        return (completedIntervals / 2) * (WORK_SECONDS + REST_SECONDS) +
                if (completedIntervals % 2 == 1) WORK_SECONDS else 0
    }
    val remainingTotalSeconds: Int get() = (totalIntervals - completedIntervals).let { remaining ->
        val fullCycles = remaining / 2
        val hasWorkLeft = remaining % 2 == 1
        fullCycles * (WORK_SECONDS + REST_SECONDS) +
                (if (hasWorkLeft) intervalSecondsRemaining else 0) +
                (if (!hasWorkLeft && remaining > 0) intervalSecondsRemaining else 0)
    }
}

class WarmupViewModel(
    private val exercises: List<WarmupExercise> = emptyList(),
    private val toneGeneratorFactory: () -> ToneGenerator? = {
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 80) } catch (e: Exception) { null }
    }
) : ViewModel() {

    private val _state = MutableStateFlow(WarmupState())
    val state: StateFlow<WarmupState> = _state.asStateFlow()

    /** Pre-built sequence of exercises for the current session. */
    private var exerciseSequence: List<WarmupExercise> = emptyList()
    private var sequenceIndex = 0

    // ── Setup ─────────────────────────────────────────────────────────────────

    fun incrementDuration() {
        _state.update { s ->
            if (s.durationMinutes < 60) s.copy(
                durationMinutes = s.durationMinutes + 1,
                totalIntervals = (s.durationMinutes + 1) * 2
            ) else s
        }
    }

    fun decrementDuration() {
        _state.update { s ->
            if (s.durationMinutes > 1) s.copy(
                durationMinutes = s.durationMinutes - 1,
                totalIntervals = (s.durationMinutes - 1) * 2
            ) else s
        }
    }

    /**
     * Returns the number of distinct body parts that will be covered
     * for [durationMinutes], given the current exercise library.
     */
    fun coveredBodyPartsCount(durationMinutes: Int): Int {
        val workSlots = durationMinutes          // N min → N work intervals (half of 2N total)
        val distinctBodyParts = exercises.map { it.bodyPart }.distinct()
        return minOf(workSlots, distinctBodyParts.size)
    }

    fun totalBodyPartsCount(): Int = exercises.map { it.bodyPart }.distinct().size

    // ── Timer lifecycle ───────────────────────────────────────────────────────

    fun start() {
        val durationMinutes = _state.value.durationMinutes
        val workSlots = durationMinutes
        exerciseSequence = buildExerciseSequence(workSlots)
        sequenceIndex = 0

        val firstExercise = exerciseSequence.getOrNull(0)
        val nextExercise = exerciseSequence.getOrNull(1)

        _state.update { s ->
            s.copy(
                phase = WarmupPhase.RUNNING,
                completedIntervals = 0,
                intervalType = IntervalType.WORK,
                intervalSecondsRemaining = WORK_SECONDS,
                currentExercise = firstExercise,
                nextExercise = nextExercise,
                totalIntervals = s.durationMinutes * 2,
                coveredBodyParts = if (firstExercise != null) setOf(firstExercise.bodyPart) else emptySet()
            )
        }
        playTone(ToneGenerator.TONE_PROP_BEEP)
        startTick()
    }

    fun pause() {
        timerJob?.cancel()
        _state.update { it.copy(phase = WarmupPhase.PAUSED) }
    }

    fun resume() {
        _state.update { it.copy(phase = WarmupPhase.RUNNING) }
        startTick()
    }

    fun stop() {
        timerJob?.cancel()
        sequenceIndex = 0
        exerciseSequence = emptyList()
        _state.update { s ->
            WarmupState(durationMinutes = s.durationMinutes, totalIntervals = s.durationMinutes * 2)
        }
    }

    // ── Sequence building ─────────────────────────────────────────────────────

    /**
     * Builds an ordered list of [workSlots] exercises guaranteeing full-body coverage.
     *
     * Algorithm:
     * 1. From each body part group pick one exercise randomly → coverage round (shuffled).
     * 2. Fill remaining slots by sampling uniformly at random from all exercises.
     */
    internal fun buildExerciseSequence(workSlots: Int): List<WarmupExercise> {
        if (exercises.isEmpty()) return emptyList()

        // Step 1: coverage round — one exercise per body part, shuffled
        val coverageRound = exercises
            .groupBy { it.bodyPart }
            .values
            .map { group -> group.random() }
            .shuffled()
            .take(workSlots)    // if fewer work slots than body parts, partial coverage

        if (coverageRound.size >= workSlots) return coverageRound

        // Step 2: fill remaining slots randomly
        val remaining = workSlots - coverageRound.size
        val filler = (0 until remaining).map { exercises.random() }

        return coverageRound + filler
    }

    // ── Internal timer ────────────────────────────────────────────────────────

    private var timerJob: Job? = null

    private fun startTick() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val s = _state.value
                if (s.intervalSecondsRemaining > 1) {
                    _state.update { it.copy(intervalSecondsRemaining = it.intervalSecondsRemaining - 1) }
                } else {
                    advanceInterval()
                    if (_state.value.phase == WarmupPhase.FINISHED) break
                }
            }
        }
    }

    private fun advanceInterval() {
        val s = _state.value
        val newCompleted = s.completedIntervals + 1

        if (newCompleted >= s.totalIntervals) {
            timerJob?.cancel()
            playCompletion()
            _state.update { it.copy(phase = WarmupPhase.FINISHED, intervalSecondsRemaining = 0) }
            return
        }

        if (s.intervalType == IntervalType.WORK) {
            // WORK finished → go to REST
            playTone(ToneGenerator.TONE_PROP_BEEP2)
            _state.update { it.copy(
                completedIntervals = newCompleted,
                intervalType = IntervalType.REST,
                intervalSecondsRemaining = REST_SECONDS
            ) }
        } else {
            // REST finished → go to next WORK
            sequenceIndex++
            val nextIdx = sequenceIndex + 1
            val nextExercise = exerciseSequence.getOrNull(nextIdx)
            val currentExercise = exerciseSequence.getOrNull(sequenceIndex)
            playTone(ToneGenerator.TONE_PROP_BEEP)
            _state.update { it.copy(
                completedIntervals = newCompleted,
                intervalType = IntervalType.WORK,
                intervalSecondsRemaining = WORK_SECONDS,
                currentExercise = currentExercise,
                nextExercise = nextExercise,
                coveredBodyParts = it.coveredBodyParts +
                        listOfNotNull(currentExercise?.bodyPart)
            ) }
        }
    }

    // ── Sound ─────────────────────────────────────────────────────────────────

    private fun playTone(toneType: Int) {
        try {
            toneGeneratorFactory()?.startTone(toneType, 200)
        } catch (_: Exception) { }
    }

    private fun playCompletion() {
        try {
            val tg = toneGeneratorFactory() ?: return
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            viewModelScope.launch {
                delay(300)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                delay(300)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            }
        } catch (_: Exception) { }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val exercises = WarmupExerciseDataSource(application).loadExercises()
            return WarmupViewModel(exercises = exercises) as T
        }
    }
}
