package com.alma.climbingtraining.ui.flyingloto

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.alma.climbingtraining.MainActivity
import com.alma.climbingtraining.ui.home.TAG_TOOL_CARD_FLYING_LOTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FlyingLotoFlowInstrumentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearSavedNames() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context
            .getSharedPreferences("flying_loto_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun fullFlow_enterPlayers_assignments_startGame_andShowMatchedPlayerName() {
        val playerNames = listOf("Alice", "Bob", "Charlie", "Diana")

        composeRule.onNodeWithTag(TAG_TOOL_CARD_FLYING_LOTO).performClick()

        playerNames.forEach { name ->
            composeRule.onNodeWithTag(TAG_PLAYER_NAME_INPUT).performTextInput(name)
            composeRule.onNodeWithTag(TAG_ADD_PLAYER_BUTTON).performClick()
        }

        composeRule.onNodeWithTag(TAG_VALIDATE_BUTTON).performClick()

        playerNames.forEach { name ->
            composeRule.onNodeWithText(name).assertIsDisplayed()
        }

        val assignmentByNumber = mutableMapOf<Int, String>()
        playerNames.indices.forEach { index ->
            val assignedName = composeRule.readNodeText("$TAG_ASSIGNMENT_NAME_PREFIX$index")
            val assignedNumberText = composeRule.readNodeText("$TAG_ASSIGNMENT_NUMBER_PREFIX$index")
            val assignedNumber = assignedNumberText.removePrefix("#").toInt()
            assignmentByNumber[assignedNumber] = assignedName
        }

        assertEquals("Each player should have a unique assigned number", 4, assignmentByNumber.size)
        assertTrue(
            "Assigned numbers must stay in the expected 1..30 range",
            assignmentByNumber.keys.all { it in 1..30 }
        )

        composeRule.onNodeWithTag(TAG_START_GAME_BUTTON).performClick()
        composeRule.onNodeWithTag(TAG_NEXT_NUMBER_BUTTON).assertIsDisplayed()

        var foundMatchingDraw = false

        repeat(120) {
            composeRule.onNodeWithTag(TAG_NEXT_NUMBER_BUTTON).performClick()
            composeRule.waitForIdle()

            val currentNumber = composeRule.readNodeText(TAG_CURRENT_NUMBER_TEXT).toIntOrNull()
            val expectedMatchedName = currentNumber?.let { assignmentByNumber[it] }

            if (expectedMatchedName != null) {
                val displayedName = composeRule.readNodeText(TAG_CURRENT_PLAYER_NAME_TEXT)
                assertEquals(
                    "When a drawn number is assigned to a player, that player's name should be shown",
                    expectedMatchedName,
                    displayedName
                )
                foundMatchingDraw = true
                return@repeat
            }
        }

        assertTrue(
            "Expected at least one drawn number matching a player assignment within 120 draws",
            foundMatchingDraw
        )
    }
}

private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.readNodeText(tag: String): String {
    val semanticsNode = onNodeWithTag(tag).fetchSemanticsNode()
    val text = semanticsNode.config[SemanticsProperties.Text]
    return text.joinToString(separator = "") { it.text }
}
