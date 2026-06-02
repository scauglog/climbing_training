package com.alma.climbingtraining.ui.randompartner

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.alma.climbingtraining.MainDispatcherRule
import com.alma.climbingtraining.model.Participant
import com.alma.climbingtraining.model.RandomPartnerPhase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RandomPartnerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RandomPartnerViewModel
    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = RandomPartnerViewModel(application)
    }

    // ── Initialization ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is ENTRY phase`() {
        assertEquals(RandomPartnerPhase.ENTRY, viewModel.state.value.phase)
    }

    @Test
    fun `initial participants list is empty`() {
        assertTrue(viewModel.state.value.participants.isEmpty())
    }

    @Test
    fun `initial tolerance is 10 kg`() {
        assertEquals(10f, viewModel.state.value.toleranceKg)
    }

    @Test
    fun `initial groups list is empty`() {
        assertTrue(viewModel.state.value.groups.isEmpty())
    }

    // ── Phase transitions ──────────────────────────────────────────────────────

    @Test
    fun `startPrivateInput transitions to PRIVATE_INPUT phase`() {
        viewModel.startPrivateInput()
        assertEquals(RandomPartnerPhase.PRIVATE_INPUT, viewModel.state.value.phase)
    }

    @Test
    fun `cancelPrivateInput transitions back to ENTRY phase`() {
        viewModel.startPrivateInput()
        viewModel.cancelPrivateInput()
        assertEquals(RandomPartnerPhase.ENTRY, viewModel.state.value.phase)
    }

    // ── Participant management ─────────────────────────────────────────────────

    @Test
    fun `confirmParticipant adds trimmed name and weight to participants`() {
        viewModel.confirmParticipant("  Alice  ", 65.5f)
        
        assertEquals(1, viewModel.state.value.participants.size)
        val participant = viewModel.state.value.participants[0]
        assertEquals("Alice", participant.name)
        assertEquals(65.5f, participant.weightKg)
    }

    @Test
    fun `confirmParticipant transitions back to ENTRY phase`() {
        viewModel.startPrivateInput()
        viewModel.confirmParticipant("Alice", 65f)
        assertEquals(RandomPartnerPhase.ENTRY, viewModel.state.value.phase)
    }

    @Test
    fun `confirmParticipant ignores blank name`() {
        viewModel.confirmParticipant("   ", 65f)
        assertTrue(viewModel.state.value.participants.isEmpty())
    }

    @Test
    fun `confirmParticipant ignores zero weight`() {
        viewModel.confirmParticipant("Alice", 0f)
        assertTrue(viewModel.state.value.participants.isEmpty())
    }

    @Test
    fun `confirmParticipant ignores negative weight`() {
        viewModel.confirmParticipant("Alice", -10f)
        assertTrue(viewModel.state.value.participants.isEmpty())
    }

    @Test
    fun `confirmParticipant allows multiple participants`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        viewModel.confirmParticipant("Charlie", 68f)
        
        assertEquals(3, viewModel.state.value.participants.size)
    }

    @Test
    fun `confirmParticipant assigns unique ID to each participant`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        
        val ids = viewModel.state.value.participants.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `removeParticipant removes participant by ID`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        
        val alice = viewModel.state.value.participants[0]
        viewModel.removeParticipant(alice)
        
        assertEquals(1, viewModel.state.value.participants.size)
        assertEquals("Bob", viewModel.state.value.participants[0].name)
    }

    @Test
    fun `removeParticipant with unknown ID does nothing`() {
        viewModel.confirmParticipant("Alice", 65f)
        val unknownParticipant = Participant(name = "Unknown", weightKg = 60f)
        
        viewModel.removeParticipant(unknownParticipant)
        
        assertEquals(1, viewModel.state.value.participants.size)
    }

    // ── Tolerance management ───────────────────────────────────────────────────

    @Test
    fun `setTolerance updates tolerance value`() {
        viewModel.setTolerance(15f)
        assertEquals(15f, viewModel.state.value.toleranceKg)
    }

    @Test
    fun `setTolerance ignores zero tolerance`() {
        viewModel.setTolerance(0f)
        assertEquals(10f, viewModel.state.value.toleranceKg)
    }

    @Test
    fun `setTolerance ignores negative tolerance`() {
        viewModel.setTolerance(-5f)
        assertEquals(10f, viewModel.state.value.toleranceKg)
    }

    @Test
    fun `setTolerance allows very large values`() {
        viewModel.setTolerance(100f)
        assertEquals(100f, viewModel.state.value.toleranceKg)
    }

    @Test
    fun `setTolerance accepts fractional values`() {
        viewModel.setTolerance(7.5f)
        assertEquals(7.5f, viewModel.state.value.toleranceKg)
    }

    // ── Finding partners (pairing algorithm) ────────────────────────────────────

    @Test
    fun `findPartners does nothing with fewer than 2 participants`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.findPartners()
        
        assertNotEquals(RandomPartnerPhase.RESULT, viewModel.state.value.phase)
        assertTrue(viewModel.state.value.groups.isEmpty())
    }

    @Test
    fun `findPartners transitions to RESULT phase with 2+ participants`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        viewModel.findPartners()
        
        assertEquals(RandomPartnerPhase.RESULT, viewModel.state.value.phase)
    }

    @Test
    fun `findPartners creates pairs with even number of participants`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        viewModel.confirmParticipant("Charlie", 68f)
        viewModel.confirmParticipant("Diana", 66f)
        
        viewModel.findPartners()
        
        val state = viewModel.state.value
        assertEquals(2, state.groups.size)
        assertTrue(state.groups.all { it.members.size == 2 })
    }

    @Test
    fun `findPartners creates one trio and pairs with odd number of participants`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        viewModel.confirmParticipant("Charlie", 68f)
        viewModel.confirmParticipant("Diana", 66f)
        viewModel.confirmParticipant("Eve", 72f)
        
        viewModel.findPartners()
        
        val state = viewModel.state.value
        assertEquals(2, state.groups.size)
        
        val groupSizes = state.groups.map { it.members.size }.sorted()
        assertTrue(groupSizes.contains(2))
        assertTrue(groupSizes.contains(3))
    }

    @Test
    fun `findPartners respects tolerance for pairs`() {
        viewModel.setTolerance(5f)
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 68f)
        
        viewModel.findPartners()
        
        val group = viewModel.state.value.groups[0]
        assertTrue(group.weightDeltaKg <= 5f)
    }

    @Test
    fun `findPartners marks pair as toleranceExceeded if needed`() {
        viewModel.setTolerance(2f)
        viewModel.confirmParticipant("Alice", 60f)
        viewModel.confirmParticipant("Bob", 70f)
        
        viewModel.findPartners()
        
        val group = viewModel.state.value.groups[0]
        assertTrue(group.toleranceExceeded)
        assertEquals(10f, group.weightDeltaKg)
    }

    @Test
    fun `findPartners does not mark pair as toleranceExceeded if within tolerance`() {
        viewModel.setTolerance(10f)
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        
        viewModel.findPartners()
        
        val group = viewModel.state.value.groups[0]
        assertFalse(group.toleranceExceeded)
    }

    @Test
    fun `findPartners with identical weights produces zero weight delta`() {
        viewModel.confirmParticipant("Alice", 70f)
        viewModel.confirmParticipant("Bob", 70f)
        
        viewModel.findPartners()
        
        val group = viewModel.state.value.groups[0]
        assertEquals(0f, group.weightDeltaKg)
    }

    // ── Try Again (re-pairing) ─────────────────────────────────────────────────

    @Test
    fun `tryAgain re-runs pairing algorithm`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        viewModel.confirmParticipant("Charlie", 68f)
        viewModel.confirmParticipant("Diana", 66f)
        
        viewModel.findPartners()
        val firstResult = viewModel.state.value.groups.toList()
        
        viewModel.tryAgain()
        val secondResult = viewModel.state.value.groups.toList()
        
        assertEquals(RandomPartnerPhase.RESULT, viewModel.state.value.phase)
        assertEquals(firstResult.size, secondResult.size)
    }

    @Test
    fun `tryAgain does not modify participant list`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        viewModel.findPartners()
        
        val originalParticipants = viewModel.state.value.participants.map { it.name }
        
        viewModel.tryAgain()
        
        val newParticipants = viewModel.state.value.participants.map { it.name }
        assertEquals(originalParticipants, newParticipants)
    }

    // ── Restart ────────────────────────────────────────────────────────────────

    @Test
    fun `restart clears all participants and returns to ENTRY`() {
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.confirmParticipant("Bob", 70f)
        viewModel.findPartners()
        
        viewModel.restart()
        
        assertEquals(RandomPartnerPhase.ENTRY, viewModel.state.value.phase)
        assertTrue(viewModel.state.value.participants.isEmpty())
        assertTrue(viewModel.state.value.groups.isEmpty())
    }

    @Test
    fun `restart preserves tolerance setting`() {
        viewModel.setTolerance(15f)
        viewModel.confirmParticipant("Alice", 65f)
        viewModel.findPartners()
        
        viewModel.restart()
        
        assertEquals(15f, viewModel.state.value.toleranceKg)
    }

    // ── Edge cases ─────────────────────────────────────────────────────────────

    @Test
    fun `many participants are properly paired`() {
        val participants = listOf(
            "Alice" to 65f, "Bob" to 70f, "Charlie" to 68f,
            "Diana" to 66f, "Eve" to 72f, "Frank" to 67f,
            "Grace" to 69f, "Henry" to 71f
        )
        participants.forEach { (name, weight) ->
            viewModel.confirmParticipant(name, weight)
        }
        
        viewModel.findPartners()
        
        val state = viewModel.state.value
        assertEquals(4, state.groups.size)
        val totalMembers = state.groups.sumOf { it.members.size }
        assertEquals(8, totalMembers)
    }

    @Test
    fun `very similar weights produce good pairings`() {
        viewModel.confirmParticipant("Alice", 70f)
        viewModel.confirmParticipant("Bob", 70.5f)
        viewModel.confirmParticipant("Charlie", 71f)
        viewModel.confirmParticipant("Diana", 70.8f)
        
        viewModel.findPartners()
        
        viewModel.state.value.groups.forEach { group ->
            assertTrue(group.weightDeltaKg <= 1f)
        }
    }

    @Test
    fun `wide weight range creates reasonable groups`() {
        viewModel.setTolerance(20f)
        viewModel.confirmParticipant("Child", 40f)
        viewModel.confirmParticipant("Teen", 55f)
        viewModel.confirmParticipant("Adult1", 70f)
        viewModel.confirmParticipant("Adult2", 75f)
        
        viewModel.findPartners()
        
        assertEquals(2, viewModel.state.value.groups.size)
    }
}
