package com.alma.climbingtraining.ui.randompartner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alma.climbingtraining.model.Participant
import com.alma.climbingtraining.model.PartnerGroup
import com.alma.climbingtraining.model.RandomPartnerPhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RandomPartnerState(
    val participants: List<Participant> = emptyList(),
    val toleranceKg: Float = 10f,
    val phase: RandomPartnerPhase = RandomPartnerPhase.ENTRY,
    val groups: List<PartnerGroup> = emptyList()
)

class RandomPartnerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(RandomPartnerState())
    val state: StateFlow<RandomPartnerState> = _state.asStateFlow()

    // ── Navigation ──────────────────────────────────────────────────────────

    fun startPrivateInput() {
        _state.update { it.copy(phase = RandomPartnerPhase.PRIVATE_INPUT) }
    }

    fun cancelPrivateInput() {
        _state.update { it.copy(phase = RandomPartnerPhase.ENTRY) }
    }

    // ── Participant management ───────────────────────────────────────────────

    fun confirmParticipant(name: String, weightKg: Float) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || weightKg <= 0f) return
        _state.update { s ->
            s.copy(
                participants = s.participants + Participant(name = trimmed, weightKg = weightKg),
                phase = RandomPartnerPhase.ENTRY
            )
        }
    }

    fun removeParticipant(participant: Participant) {
        _state.update { s ->
            s.copy(participants = s.participants.filter { it.id != participant.id })
        }
    }

    fun setTolerance(kg: Float) {
        if (kg >= 1f) _state.update { it.copy(toleranceKg = kg) }
    }

    // ── Pairing ──────────────────────────────────────────────────────────────

    fun findPartners() {
        val participants = _state.value.participants
        if (participants.size < 2) return
        val tolerance = _state.value.toleranceKg
        val groups = buildGroups(participants, tolerance)
        _state.update { it.copy(groups = groups, phase = RandomPartnerPhase.RESULT) }
    }

    fun tryAgain() {
        findPartners()
    }

    fun restart() {
        _state.update {
            RandomPartnerState(toleranceKg = it.toleranceKg)
        }
    }

    // ── Algorithm ────────────────────────────────────────────────────────────

    private fun buildGroups(participants: List<Participant>, toleranceKg: Float): List<PartnerGroup> {
        val sorted = participants.sortedBy { it.weightKg }.toMutableList()
        val groups = mutableListOf<PartnerGroup>()

        // Step 1: if odd count, pick the trio proactively from a window of 3 sorted participants
        if (sorted.size % 2 != 0) {
            val trioIndices = pickTrioWindow(sorted, toleranceKg)
            val trio = trioIndices.map { sorted[it] }
            val delta = trio.maxOf { it.weightKg } - trio.minOf { it.weightKg }
            val exceeded = delta > toleranceKg
            groups.add(PartnerGroup(members = trio, weightDeltaKg = delta, toleranceExceeded = exceeded))
            // Remove in reverse order to keep indices valid
            trioIndices.sortedDescending().forEach { sorted.removeAt(it) }
        }

        // Step 2: greedy pairing on the (now even) remaining pool
        // Randomly choose start direction
        if (sorted.isNotEmpty() && (0..1).random() == 1) sorted.reverse()

        val unmatched = sorted.toMutableList()
        while (unmatched.size >= 2) {
            val a = unmatched.removeAt(0)
            // Candidates within [a.weight, a.weight + tolerance] (list is still sorted asc or desc)
            val candidates = unmatched.filter { kotlin.math.abs(it.weightKg - a.weightKg) <= toleranceKg }
            val partner = if (candidates.isNotEmpty()) {
                candidates.random()
            } else {
                // best-effort: nearest by absolute weight difference
                unmatched.minByOrNull { kotlin.math.abs(it.weightKg - a.weightKg) }!!
            }
            unmatched.remove(partner)
            val delta = kotlin.math.abs(a.weightKg - partner.weightKg)
            groups.add(
                PartnerGroup(
                    members = listOf(a, partner),
                    weightDeltaKg = delta,
                    toleranceExceeded = delta > toleranceKg
                )
            )
        }

        return groups.shuffled()
    }

    /**
     * Returns the indices (in [sorted]) of the best trio window to form.
     * Picks randomly among windows whose spread ≤ toleranceKg, or the minimum-spread window.
     */
    private fun pickTrioWindow(sorted: List<Participant>, toleranceKg: Float): List<Int> {
        data class Window(val indices: List<Int>, val spread: Float)

        val windows = (0..sorted.size - 3).map { i ->
            Window(
                indices = listOf(i, i + 1, i + 2),
                spread = sorted[i + 2].weightKg - sorted[i].weightKg
            )
        }

        val qualifying = windows.filter { it.spread <= toleranceKg }
        val chosen = if (qualifying.isNotEmpty()) qualifying.random() else windows.minByOrNull { it.spread }!!
        return chosen.indices
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RandomPartnerViewModel(application) as T
    }
}
