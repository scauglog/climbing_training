package com.alma.climbingtraining.model

import java.util.UUID

data class Participant(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val weightKg: Float
)

/**
 * A pair or trio of participants matched by weight.
 * [weightDeltaKg] is the max pairwise weight difference within the group.
 * [toleranceExceeded] is true when the delta is above the configured tolerance (best-effort match).
 */
data class PartnerGroup(
    val members: List<Participant>,
    val weightDeltaKg: Float,
    val toleranceExceeded: Boolean = false
)

enum class RandomPartnerPhase {
    ENTRY,
    PRIVATE_INPUT,
    RESULT
}
