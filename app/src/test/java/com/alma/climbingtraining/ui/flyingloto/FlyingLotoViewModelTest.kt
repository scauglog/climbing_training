package com.alma.climbingtraining.ui.flyingloto

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.alma.climbingtraining.MainDispatcherRule
import com.alma.climbingtraining.data.PlayerNamesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * In-memory fake of PlayerNamesRepository — no Android I/O required.
 */
class FakePlayerNamesRepository : PlayerNamesRepository {
    val savedNames = mutableListOf<String>()
    var loadResult: List<String> = emptyList()

    override fun savePlayerNames(names: List<String>) {
        savedNames.clear()
        savedNames.addAll(names)
    }

    override fun loadPlayerNames(): List<String> = loadResult
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FlyingLotoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakePlayerNamesRepository
    private lateinit var viewModel: FlyingLotoViewModel

    @Before
    fun setUp() {
        fakeRepo = FakePlayerNamesRepository()
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = FlyingLotoViewModel(
            application = application,
            repository = fakeRepo,
            ioDispatcher = mainDispatcherRule.testDispatcher
        )
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Test
    fun `initial state starts in PLAYER_ENTRY phase`() {
        assertEquals(GamePhase.PLAYER_ENTRY, viewModel.state.value.phase)
    }

    @Test
    fun `init loads saved names from repository`() = runTest {
        val repo = FakePlayerNamesRepository().apply { loadResult = listOf("Alice", "Bob") }
        val application = ApplicationProvider.getApplicationContext<Application>()
        val vm = FlyingLotoViewModel(application, repo, mainDispatcherRule.testDispatcher)

        assertEquals(listOf("Alice", "Bob"), vm.state.value.playerNames)
    }

    // ── addPlayer ─────────────────────────────────────────────────────────────

    @Test
    fun `addPlayer adds trimmed name to state`() {
        viewModel.addPlayer("  Alice  ")
        assertEquals(listOf("Alice"), viewModel.state.value.playerNames)
    }

    @Test
    fun `addPlayer persists names to repository`() {
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        assertEquals(listOf("Alice", "Bob"), fakeRepo.savedNames)
    }

    @Test
    fun `addPlayer ignores blank input`() {
        viewModel.addPlayer("   ")
        assertTrue(viewModel.state.value.playerNames.isEmpty())
    }

    @Test
    fun `addPlayer ignores duplicate name`() {
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Alice")
        assertEquals(1, viewModel.state.value.playerNames.size)
    }

    @Test
    fun `addPlayer is case-sensitive for duplicates`() {
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("alice")
        assertEquals(2, viewModel.state.value.playerNames.size)
    }

    @Test
    fun `addPlayer enforces max 30 players`() {
        repeat(31) { viewModel.addPlayer("Player$it") }
        assertEquals(30, viewModel.state.value.playerNames.size)
    }

    // ── removePlayer ──────────────────────────────────────────────────────────

    @Test
    fun `removePlayer removes the named player`() {
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.removePlayer("Alice")
        assertEquals(listOf("Bob"), viewModel.state.value.playerNames)
    }

    @Test
    fun `removePlayer persists updated list`() {
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.removePlayer("Alice")
        assertEquals(listOf("Bob"), fakeRepo.savedNames)
    }

    @Test
    fun `removePlayer on unknown name leaves list unchanged`() {
        viewModel.addPlayer("Alice")
        viewModel.removePlayer("Charlie")
        assertEquals(listOf("Alice"), viewModel.state.value.playerNames)
    }

    // ── validate ──────────────────────────────────────────────────────────────

    @Test
    fun `validate transitions to CONFIGURATION phase`() {
        viewModel.addPlayer("Alice")
        viewModel.validate()
        assertEquals(GamePhase.CONFIGURATION, viewModel.state.value.phase)
    }

    @Test
    fun `validate does nothing when no players`() {
        viewModel.validate()
        assertEquals(GamePhase.PLAYER_ENTRY, viewModel.state.value.phase)
    }

    @Test
    fun `validate assigns unique numbers between 1 and 30`() {
        listOf("Alice", "Bob", "Charlie").forEach { viewModel.addPlayer(it) }
        viewModel.validate()

        val assignedNumbers = viewModel.state.value.players.map { it.assignedNumber!! }
        assertEquals(3, assignedNumbers.distinct().size)
        assertTrue(assignedNumbers.all { it in 1..30 })
    }

    @Test
    fun `validate assigns exactly one number per player`() {
        repeat(30) { viewModel.addPlayer("Player$it") }
        viewModel.validate()

        val players = viewModel.state.value.players
        assertEquals(30, players.size)
        assertEquals(30, players.map { it.assignedNumber }.distinct().size)
    }

    @Test
    fun `validate preserves player names`() {
        listOf("Alice", "Bob").forEach { viewModel.addPlayer(it) }
        viewModel.validate()

        val names = viewModel.state.value.players.map { it.name }
        assertEquals(listOf("Alice", "Bob"), names)
    }

    // ── startGame ─────────────────────────────────────────────────────────────

    @Test
    fun `startGame transitions to PLAYING phase`() {
        viewModel.addPlayer("Alice")
        viewModel.validate()
        viewModel.startGame()
        assertEquals(GamePhase.PLAYING, viewModel.state.value.phase)
    }

    @Test
    fun `startGame clears currentNumber and currentPlayerName`() {
        viewModel.addPlayer("Alice")
        viewModel.validate()
        viewModel.startGame()
        assertNull(viewModel.state.value.currentNumber)
        assertNull(viewModel.state.value.currentPlayerName)
    }

    // ── nextNumber ────────────────────────────────────────────────────────────

    @Test
    fun `nextNumber sets currentNumber between 1 and 30`() {
        viewModel.addPlayer("Alice")
        viewModel.validate()
        viewModel.startGame()
        repeat(20) {
            viewModel.nextNumber()
            val num = viewModel.state.value.currentNumber!!
            assertTrue("Expected 1..30, got $num", num in 1..30)
        }
    }

    @Test
    fun `nextNumber sets currentPlayerName when number matches a player`() {
        viewModel.addPlayer("Alice")
        viewModel.validate()
        viewModel.startGame()

        val aliceNumber = viewModel.state.value.players.first { it.name == "Alice" }.assignedNumber!!

        // Drive nextNumber until Alice's number appears (max attempts to avoid flakiness)
        var matched = false
        repeat(500) {
            if (!matched) {
                viewModel.nextNumber()
                if (viewModel.state.value.currentNumber == aliceNumber) {
                    assertEquals("Alice", viewModel.state.value.currentPlayerName)
                    matched = true
                }
            }
        }
        assertTrue("Alice's number should have been drawn within 500 attempts", matched)
    }

    @Test
    fun `nextNumber sets null currentPlayerName when number has no player`() {
        // Only one player with a known number; draw all other numbers and verify no match
        viewModel.addPlayer("Alice")
        viewModel.validate()
        viewModel.startGame()

        val aliceNumber = viewModel.state.value.players.first().assignedNumber!!

        // Find a number not assigned to Alice
        val otherNumber = (1..30).first { it != aliceNumber }

        // Manually inject a state where currentNumber is the other number
        // by calling nextNumber until we get it (or verify null case indirectly)
        var foundNull = false
        repeat(300) {
            if (!foundNull) {
                viewModel.nextNumber()
                val state = viewModel.state.value
                if (state.currentNumber != aliceNumber) {
                    assertNull(state.currentPlayerName)
                    foundNull = true
                }
            }
        }
        assertTrue("Should have drawn a non-Alice number within 300 attempts", foundNull)
    }

    // ── stopGame ──────────────────────────────────────────────────────────────

    @Test
    fun `stopGame transitions back to PLAYER_ENTRY`() {
        viewModel.addPlayer("Alice")
        viewModel.validate()
        viewModel.startGame()
        viewModel.stopGame()
        assertEquals(GamePhase.PLAYER_ENTRY, viewModel.state.value.phase)
    }

    @Test
    fun `stopGame preserves player names`() {
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.validate()
        viewModel.startGame()
        viewModel.stopGame()
        assertEquals(listOf("Alice", "Bob"), viewModel.state.value.playerNames)
    }

    @Test
    fun `stopGame clears players, currentNumber and currentPlayerName`() {
        viewModel.addPlayer("Alice")
        viewModel.validate()
        viewModel.startGame()
        viewModel.nextNumber()
        viewModel.stopGame()

        val state = viewModel.state.value
        assertTrue(state.players.isEmpty())
        assertNull(state.currentNumber)
        assertNull(state.currentPlayerName)
    }
}
