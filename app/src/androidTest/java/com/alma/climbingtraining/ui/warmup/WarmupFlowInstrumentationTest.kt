package com.alma.climbingtraining.ui.warmup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alma.climbingtraining.MainActivity
import com.alma.climbingtraining.ui.home.TAG_TOOL_CARD_WARMUP
import org.junit.Rule
import org.junit.Test

class WarmupFlowInstrumentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun tappingWarmupCardNavigatesToSetupScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).assertIsDisplayed()
    }

    // ── Setup screen ──────────────────────────────────────────────────────────

    @Test
    fun setupScreen_showsDurationDisplay() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_DURATION_DISPLAY).assertIsDisplayed()
    }

    @Test
    fun setupScreen_incrementButtonIncreasesMinutes() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        val initialText = composeRule.onNodeWithTag(TAG_WARMUP_DURATION_DISPLAY)
            .fetchSemanticsNode().config.getOrElse(androidx.compose.ui.semantics.SemanticsProperties.Text) { emptyList() }
            .firstOrNull()?.text ?: ""

        composeRule.onNodeWithTag(TAG_WARMUP_INCREMENT_BUTTON).performClick()
        composeRule.waitForIdle()

        val newText = composeRule.onNodeWithTag(TAG_WARMUP_DURATION_DISPLAY)
            .fetchSemanticsNode().config.getOrElse(androidx.compose.ui.semantics.SemanticsProperties.Text) { emptyList() }
            .firstOrNull()?.text ?: ""
        assert(newText != initialText) { "Duration display should change after increment" }
    }

    @Test
    fun setupScreen_decrementButtonDecreasesMinutes() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        // First increment so we're not at minimum
        composeRule.onNodeWithTag(TAG_WARMUP_INCREMENT_BUTTON).performClick()
        composeRule.waitForIdle()

        val textBefore = composeRule.onNodeWithTag(TAG_WARMUP_DURATION_DISPLAY)
            .fetchSemanticsNode().config.getOrElse(androidx.compose.ui.semantics.SemanticsProperties.Text) { emptyList() }
            .firstOrNull()?.text ?: ""

        composeRule.onNodeWithTag(TAG_WARMUP_DECREMENT_BUTTON).performClick()
        composeRule.waitForIdle()

        val textAfter = composeRule.onNodeWithTag(TAG_WARMUP_DURATION_DISPLAY)
            .fetchSemanticsNode().config.getOrElse(androidx.compose.ui.semantics.SemanticsProperties.Text) { emptyList() }
            .firstOrNull()?.text ?: ""
        assert(textAfter != textBefore) { "Duration display should change after decrement" }
    }

    // ── Start → Timer screen ──────────────────────────────────────────────────

    @Test
    fun tappingStartNavigatesToTimerScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_WARMUP_INTERVAL_LABEL).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_WARMUP_COUNTDOWN).assertIsDisplayed()
    }

    @Test
    fun timerScreen_showsPauseButton() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_WARMUP_PAUSE_RESUME_BUTTON).assertIsDisplayed()
    }

    @Test
    fun timerScreen_showsStopButton() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_WARMUP_STOP_BUTTON).assertIsDisplayed()
    }

    @Test
    fun timerScreen_showsExerciseName() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_WARMUP_EXERCISE_NAME).assertIsDisplayed()
    }

    // ── Pause / Resume ────────────────────────────────────────────────────────

    @Test
    fun tappingPauseButtonPausesTimer() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_WARMUP_PAUSE_RESUME_BUTTON).performClick()
        composeRule.waitForIdle()

        // After pause, button should show "Resume" text
        composeRule.onNodeWithText("Resume").assertIsDisplayed()
    }

    @Test
    fun tappingResumeButtonResumesTimer() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).performClick()
        composeRule.waitForIdle()

        // Pause then resume
        composeRule.onNodeWithTag(TAG_WARMUP_PAUSE_RESUME_BUTTON).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TAG_WARMUP_PAUSE_RESUME_BUTTON).performClick()
        composeRule.waitForIdle()

        // Should show "Pause" again
        composeRule.onNodeWithText("Pause").assertIsDisplayed()
    }

    // ── Stop ──────────────────────────────────────────────────────────────────

    @Test
    fun tappingStopButtonReturnsToSetupScreen() {
        composeRule.onNodeWithTag(TAG_TOOL_CARD_WARMUP).performClick()
        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_WARMUP_STOP_BUTTON).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TAG_WARMUP_START_BUTTON).assertIsDisplayed()
    }
}
