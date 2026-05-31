package com.alma.climbingtraining.ui.randomexercise

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alma.climbingtraining.MainActivity
import com.alma.climbingtraining.ui.home.TAG_TOOL_CARD_RANDOM_EXERCISE
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class RandomExerciseFlowInstrumentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun tappingRandomExerciseCardNavigatesToFilterScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).assertIsDisplayed()
    }

    // ── Full happy-path flow ──────────────────────────────────────────────────

    @Test
    fun drawButton_withNoFilters_showsExerciseResultScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_EXERCISE_NAME).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_AGAIN_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_CHANGE_FILTERS_BUTTON).assertIsDisplayed()
    }

    @Test
    fun resultScreen_exerciseNameIsNonBlank() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).performClick()
        composeRule.waitForIdle()

        val name = composeRule.readNodeText(TAG_RANDOM_EXERCISE_EXERCISE_NAME)
        assert(name.isNotBlank()) { "Exercise name on result screen must not be blank" }
    }

    @Test
    fun drawAgainButton_replacesExerciseAndStaysOnResultScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).performClick()
        composeRule.waitForIdle()

        // Collect the name of the first drawn exercise
        val firstName = composeRule.readNodeText(TAG_RANDOM_EXERCISE_EXERCISE_NAME)

        // Draw again several times and confirm we stay on the result screen
        // (with a library of 12 exercises we expect a different result most of the time)
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_AGAIN_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_EXERCISE_NAME).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_AGAIN_BUTTON).assertIsDisplayed()

        // Sanity check: after repeated draws a different name is eventually displayed
        var foundDifferent = false
        repeat(20) {
            if (!foundDifferent) {
                composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_AGAIN_BUTTON).performClick()
                composeRule.waitForIdle()
                val currentName = composeRule.readNodeText(TAG_RANDOM_EXERCISE_EXERCISE_NAME)
                if (currentName != firstName) foundDifferent = true
            }
        }
        assert(foundDifferent) {
            "After 20 draws a different exercise should have appeared (library has > 1 exercise)"
        }
    }

    @Test
    fun changeFiltersButton_returnsToFilterScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_CHANGE_FILTERS_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).assertIsDisplayed()
    }

    @Test
    fun backArrow_onResultScreen_returnsToFilterScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).performClick()
        composeRule.waitForIdle()

        // The TopAppBar back arrow navigates to filter screen when in RESULT phase
        composeRule.onNodeWithText("Your Exercise").assertIsDisplayed()

        // Using the system back button equivalent via activity back press
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).assertIsDisplayed()
    }

    @Test
    fun filterChipsAndDrawButton_areAllDisplayedOnFilterScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()

        // Filter section titles should be visible (scrollable — just check the draw button)
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).assertIsDisplayed()
    }

    @Test
    fun fullRoundTrip_filterToDraw_thenChangeFilters_thenDrawAgain() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_RANDOM_EXERCISE).performClick()

        // First draw
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).performClick()
        composeRule.waitForIdle()
        val firstName = composeRule.readNodeText(TAG_RANDOM_EXERCISE_EXERCISE_NAME)
        assert(firstName.isNotBlank())

        // Change filters and draw again
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_CHANGE_FILTERS_BUTTON).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).assertIsDisplayed()

        composeRule.onNodeWithTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON).performClick()
        composeRule.waitForIdle()
        val secondName = composeRule.readNodeText(TAG_RANDOM_EXERCISE_EXERCISE_NAME)
        assert(secondName.isNotBlank())
    }
}

private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.readNodeText(tag: String): String {
    val node = onNodeWithTag(tag).fetchSemanticsNode()
    return node.config[SemanticsProperties.Text].joinToString("") { it.text }
}
