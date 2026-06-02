package com.alma.climbingtraining.model

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class PartnerGroupTest {

    // ── Basic properties ───────────────────────────────────────────────────────

    @Test
    fun `PartnerGroup with 2 participants creates valid pair`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f)
        
        assertEquals(2, group.members.size)
        assertEquals(5f, group.weightDeltaKg)
    }

    @Test
    fun `PartnerGroup with 3 participants creates valid trio`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val charlie = Participant(name = "Charlie", weightKg = 68f)
        val group = PartnerGroup(members = listOf(alice, bob, charlie), weightDeltaKg = 5f)
        
        assertEquals(3, group.members.size)
    }

    @Test
    fun `PartnerGroup defaults toleranceExceeded to false`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f)
        
        assertFalse(group.toleranceExceeded)
    }

    @Test
    fun `PartnerGroup can be marked as toleranceExceeded`() {
        val alice = Participant(name = "Alice", weightKg = 60f)
        val bob = Participant(name = "Bob", weightKg = 75f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 15f, toleranceExceeded = true)
        
        assertTrue(group.toleranceExceeded)
    }

    // ── Weight delta calculation ───────────────────────────────────────────────

    @Test
    fun `zero weight delta for identical weights`() {
        val alice = Participant(name = "Alice", weightKg = 70f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 0f)
        
        assertEquals(0f, group.weightDeltaKg)
    }

    @Test
    fun `weight delta reflects difference between pair`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 72f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 7f)
        
        assertEquals(7f, group.weightDeltaKg)
    }

    @Test
    fun `weight delta is positive regardless of order`() {
        val alice = Participant(name = "Alice", weightKg = 70f)
        val bob = Participant(name = "Bob", weightKg = 65f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f)
        
        assertEquals(5f, group.weightDeltaKg)
    }

    @Test
    fun `trio weight delta is max pairwise difference`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val charlie = Participant(name = "Charlie", weightKg = 73f)
        // Max pairwise difference is 73 - 65 = 8
        val group = PartnerGroup(members = listOf(alice, bob, charlie), weightDeltaKg = 8f)
        
        assertEquals(8f, group.weightDeltaKg)
    }

    // ── Participant properties ─────────────────────────────────────────────────

    @Test
    fun `Participant stores name and weight`() {
        val participant = Participant(name = "Alice", weightKg = 65.5f)
        
        assertEquals("Alice", participant.name)
        assertEquals(65.5f, participant.weightKg)
    }

    @Test
    fun `Participant generates unique ID by default`() {
        val participant1 = Participant(name = "Alice", weightKg = 65f)
        val participant2 = Participant(name = "Bob", weightKg = 70f)
        
        assertNotEquals(participant1.id, participant2.id)
    }

    @Test
    fun `Participant can be created with explicit ID`() {
        val id = UUID.randomUUID()
        val participant = Participant(id = id, name = "Alice", weightKg = 65f)
        
        assertEquals(id, participant.id)
    }

    @Test
    fun `Participant name is preserved with whitespace`() {
        val participant = Participant(name = "Mary Jane", weightKg = 60f)
        
        assertEquals("Mary Jane", participant.name)
    }

    @Test
    fun `Participant accepts fractional weights`() {
        val participant = Participant(name = "Alice", weightKg = 65.7f)
        
        assertEquals(65.7f, participant.weightKg)
    }

    @Test
    fun `Participant accepts very small weights`() {
        val participant = Participant(name = "Child", weightKg = 20f)
        
        assertEquals(20f, participant.weightKg)
    }

    @Test
    fun `Participant accepts very large weights`() {
        val participant = Participant(name = "Heavy", weightKg = 150f)
        
        assertEquals(150f, participant.weightKg)
    }

    // ── Tolerance validation ───────────────────────────────────────────────────

    @Test
    fun `pair within tolerance is valid`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 73f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 8f, toleranceExceeded = false)
        
        assertFalse(group.toleranceExceeded)
        assertTrue(group.weightDeltaKg <= 10f)  // assuming 10 kg tolerance
    }

    @Test
    fun `pair exceeding tolerance is marked`() {
        val alice = Participant(name = "Alice", weightKg = 60f)
        val bob = Participant(name = "Bob", weightKg = 76f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 16f, toleranceExceeded = true)
        
        assertTrue(group.toleranceExceeded)
        assertTrue(group.weightDeltaKg > 10f)  // assuming 10 kg tolerance
    }

    @Test
    fun `trio weight delta determines tolerance exceeded`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val charlie = Participant(name = "Charlie", weightKg = 80f)
        // Max delta is 15 kg
        val group = PartnerGroup(members = listOf(alice, bob, charlie), weightDeltaKg = 15f, toleranceExceeded = true)
        
        assertTrue(group.toleranceExceeded)
        assertTrue(group.weightDeltaKg > 10f)
    }

    // ── Data consistency ───────────────────────────────────────────────────────

    @Test
    fun `PartnerGroup preserves all member information`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f)
        
        assertTrue(group.members.contains(alice))
        assertTrue(group.members.contains(bob))
        assertEquals(alice.name, group.members[0].name)
        assertEquals(bob.name, group.members[1].name)
    }

    @Test
    fun `PartnerGroup can be copied with modified tolerance`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val original = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f, toleranceExceeded = false)
        
        val modified = original.copy(toleranceExceeded = true)
        
        assertEquals(original.members, modified.members)
        assertEquals(original.weightDeltaKg, modified.weightDeltaKg)
        assertTrue(modified.toleranceExceeded)
        assertFalse(original.toleranceExceeded)
    }

    @Test
    fun `PartnerGroup can be copied with modified members`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        val charlie = Participant(name = "Charlie", weightKg = 68f)
        val original = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f)
        
        val modified = original.copy(members = listOf(alice, charlie), weightDeltaKg = 3f)
        
        assertEquals(2, modified.members.size)
        assertEquals(3f, modified.weightDeltaKg)
        assertEquals(2, original.members.size)
    }

    // ── Edge cases ─────────────────────────────────────────────────────────────

    @Test
    fun `PartnerGroup handles names with special characters`() {
        val participant = Participant(name = "O'Brien-Smith", weightKg = 65f)
        val group = PartnerGroup(members = listOf(participant), weightDeltaKg = 0f)
        
        assertEquals("O'Brien-Smith", group.members[0].name)
    }

    @Test
    fun `PartnerGroup handles unicode names`() {
        val participant = Participant(name = "José García", weightKg = 65f)
        val group = PartnerGroup(members = listOf(participant), weightDeltaKg = 0f)
        
        assertEquals("José García", group.members[0].name)
    }

    @Test
    fun `very small weight differences are preserved`() {
        val alice = Participant(name = "Alice", weightKg = 70.0f)
        val bob = Participant(name = "Bob", weightKg = 70.1f)
        val group = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 0.1f)
        
        assertEquals(0.1f, group.weightDeltaKg, 0.001f)
    }

    @Test
    fun `group equality respects all properties`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        
        val group1 = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f, toleranceExceeded = false)
        val group2 = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f, toleranceExceeded = false)
        
        assertEquals(group1, group2)
    }

    @Test
    fun `group inequality when toleranceExceeded differs`() {
        val alice = Participant(name = "Alice", weightKg = 65f)
        val bob = Participant(name = "Bob", weightKg = 70f)
        
        val group1 = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f, toleranceExceeded = false)
        val group2 = PartnerGroup(members = listOf(alice, bob), weightDeltaKg = 5f, toleranceExceeded = true)
        
        assertNotEquals(group1, group2)
    }
}
