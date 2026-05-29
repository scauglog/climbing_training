package com.alma.climbingtraining.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlayerPreferencesTest {

    private lateinit var preferences: PlayerPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        preferences = PlayerPreferences(context)
        // Start from clean state
        preferences.savePlayerNames(emptyList())
    }

    // ── loadPlayerNames ───────────────────────────────────────────────────────

    @Test
    fun `loadPlayerNames returns empty list when nothing saved`() {
        assertEquals(emptyList<String>(), preferences.loadPlayerNames())
    }

    @Test
    fun `loadPlayerNames returns previously saved names`() {
        preferences.savePlayerNames(listOf("Alice", "Bob", "Charlie"))
        assertEquals(listOf("Alice", "Bob", "Charlie"), preferences.loadPlayerNames())
    }

    @Test
    fun `loadPlayerNames preserves insertion order`() {
        val names = listOf("Zara", "Alice", "Mike", "Bob")
        preferences.savePlayerNames(names)
        assertEquals(names, preferences.loadPlayerNames())
    }

    // ── savePlayerNames ───────────────────────────────────────────────────────

    @Test
    fun `savePlayerNames overwrites previous data`() {
        preferences.savePlayerNames(listOf("Alice", "Bob"))
        preferences.savePlayerNames(listOf("Charlie"))
        assertEquals(listOf("Charlie"), preferences.loadPlayerNames())
    }

    @Test
    fun `savePlayerNames with empty list clears stored data`() {
        preferences.savePlayerNames(listOf("Alice", "Bob"))
        preferences.savePlayerNames(emptyList())
        assertEquals(emptyList<String>(), preferences.loadPlayerNames())
    }

    // ── edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `names with special characters are stored correctly`() {
        val names = listOf("Anne-Marie", "José", "O'Brien", "|||special|||", "comma,name")
        preferences.savePlayerNames(names)
        assertEquals(names, preferences.loadPlayerNames())
    }

    @Test
    fun `names with spaces are stored correctly`() {
        val names = listOf("Mary Jane", "  padded  ", "tab\there")
        preferences.savePlayerNames(names)
        assertEquals(names, preferences.loadPlayerNames())
    }

    @Test
    fun `30 players are stored and loaded correctly`() {
        val names = (1..30).map { "Player$it" }
        preferences.savePlayerNames(names)
        assertEquals(names, preferences.loadPlayerNames())
    }

    @Test
    fun `single player is stored and loaded correctly`() {
        preferences.savePlayerNames(listOf("Solo"))
        assertEquals(listOf("Solo"), preferences.loadPlayerNames())
    }

    @Test
    fun `empty string name is stored correctly`() {
        preferences.savePlayerNames(listOf("Alice", "", "Bob"))
        assertEquals(listOf("Alice", "", "Bob"), preferences.loadPlayerNames())
    }
}
