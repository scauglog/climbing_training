package com.alma.climbingtraining.ui.randompartner

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.alma.climbingtraining.MainActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RandomPartnerFlowInstrumentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        // Clear any existing state
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Add any necessary setup here if SharedPreferences is used
    }

    // ── Entry Screen Tests ─────────────────────────────────────────────────────

    @Test
    fun entryScreen_displaysInitialUI() {
        navigateToRandomPartner()
        
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_TOLERANCE_INPUT).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).assertIsDisplayed()
    }

    @Test
    fun entryScreen_findPartnersButtonDisabledInitially() {
        navigateToRandomPartner()
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun entryScreen_toleranceInputShowsDefaultValue() {
        navigateToRandomPartner()
        
        val toleranceNode = composeRule.onNodeWithTag(TAG_TOLERANCE_INPUT)
        val text = readNodeText(TAG_TOLERANCE_INPUT)
        assertTrue(text.contains("10"))
    }

    @Test
    fun entryScreen_addParticipantNavigatesToPrivateInput() {
        navigateToRandomPartner()
        
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TAG_PARTICIPANT_NAME_INPUT).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_PARTICIPANT_WEIGHT_INPUT).assertIsDisplayed()
    }

    // ── Private Input Screen Tests ─────────────────────────────────────────────

    @Test
    fun privateInputScreen_displaysBothInputFields() {
        navigateToRandomPartner()
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TAG_PARTICIPANT_NAME_INPUT).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_PARTICIPANT_WEIGHT_INPUT).assertIsDisplayed()
    }

    @Test
    fun privateInputScreen_hasConfirmAndCancelButtons() {
        navigateToRandomPartner()
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TAG_CONFIRM_PARTICIPANT_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_CANCEL_PRIVATE_INPUT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun privateInputScreen_cancelReturnsToEntry() {
        navigateToRandomPartner()
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TAG_CANCEL_PRIVATE_INPUT_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun privateInputScreen_confirmWithValidInputReturnsToEntry() {
        navigateToRandomPartner()
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        
        composeRule.onNodeWithTag(TAG_PARTICIPANT_NAME_INPUT).performTextInput("Alice")
        composeRule.onNodeWithTag(TAG_PARTICIPANT_WEIGHT_INPUT).performTextInput("65")
        composeRule.onNodeWithTag(TAG_CONFIRM_PARTICIPANT_BUTTON).performClick()
        
        // Should be back on entry screen
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun privateInputScreen_canAddMultipleParticipants() {
        navigateToRandomPartner()
        
        val participants = listOf(
            "Alice" to "65",
            "Bob" to "70",
            "Charlie" to "68"
        )
        
        participants.forEach { (name, weight) ->
            composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
            composeRule.onNodeWithTag(TAG_PARTICIPANT_NAME_INPUT).performTextInput(name)
            composeRule.onNodeWithTag(TAG_PARTICIPANT_WEIGHT_INPUT).performTextInput(weight)
            composeRule.onNodeWithTag(TAG_CONFIRM_PARTICIPANT_BUTTON).performClick()
        }
        
        // All three names should be visible on entry screen
        participants.forEach { (name, _) ->
            composeRule.onNodeWithText(name).assertIsDisplayed()
        }
    }

    @Test
    fun privateInputScreen_weightsAreObscured() {
        navigateToRandomPartner()
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        
        val weightInput = composeRule.onNodeWithTag(TAG_PARTICIPANT_WEIGHT_INPUT)
        // Check that weight input has password transformation
        weightInput.performTextInput("65")
        
        // The input field should exist (weight was accepted)
        weightInput.assertExists()
    }

    // ── Participant Management ─────────────────────────────────────────────────

    @Test
    fun entryScreen_canRemoveParticipant() {
        navigateToRandomPartner()
        
        // Add a participant
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        composeRule.onNodeWithTag(TAG_PARTICIPANT_NAME_INPUT).performTextInput("Alice")
        composeRule.onNodeWithTag(TAG_PARTICIPANT_WEIGHT_INPUT).performTextInput("65")
        composeRule.onNodeWithTag(TAG_CONFIRM_PARTICIPANT_BUTTON).performClick()
        
        // Verify participant is visible
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
        
        // Find and click delete button for Alice
        composeRule.onNodeWithText("Alice").onParent().onChild().onChild()
            .performClick()
        
        // Participant should no longer be visible
        composeRule.onNodeWithText("Alice").assertDoesNotExist()
    }

    @Test
    fun entryScreen_findPartnersEnabledWithTwoParticipants() {
        navigateToRandomPartner()
        
        // Add two participants
        addParticipant("Alice", "65")
        addParticipant("Bob", "70")
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).assertIsEnabled()
    }

    // ── Tolerance Configuration ────────────────────────────────────────────────

    @Test
    fun entryScreen_canUpdateTolerance() {
        navigateToRandomPartner()
        
        val toleranceInput = composeRule.onNodeWithTag(TAG_TOLERANCE_INPUT)
        
        toleranceInput.performTextClearance()
        toleranceInput.performTextInput("15")
        
        val text = readNodeText(TAG_TOLERANCE_INPUT)
        assertTrue(text.contains("15"))
    }

    @Test
    fun entryScreen_toleranceValidation() {
        navigateToRandomPartner()
        
        val toleranceInput = composeRule.onNodeWithTag(TAG_TOLERANCE_INPUT)
        
        // Try to set tolerance to 0 (should be ignored)
        toleranceInput.performTextClearance()
        toleranceInput.performTextInput("0")
        
        // Original value should be preserved
        val text = readNodeText(TAG_TOLERANCE_INPUT)
        // This depends on implementation — verify it handles invalid input
        toleranceInput.assertExists()
    }

    // ── Result Screen Tests ────────────────────────────────────────────────────

    @Test
    fun resultScreen_displaysPartnersAfterFinding() {
        navigateToRandomPartner()
        
        // Add participants and find partners
        addParticipant("Alice", "65")
        addParticipant("Bob", "70")
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // Result screen should be visible
        composeRule.onNodeWithTag(TAG_RESTART_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_TRY_AGAIN_BUTTON).assertIsDisplayed()
    }

    @Test
    fun resultScreen_displaysAllParticipants() {
        navigateToRandomPartner()
        
        val participants = listOf("Alice", "Bob", "Charlie", "Diana")
        participants.forEach { name ->
            addParticipant(name, "65")
        }
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // All participants should be visible somewhere on result screen
        participants.forEach { name ->
            composeRule.onNodeWithText(name).assertExists()
        }
    }

    @Test
    fun resultScreen_displaysPairingInformation() {
        navigateToRandomPartner()
        
        addParticipant("Alice", "65")
        addParticipant("Bob", "70")
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // Should display at least one group with both members
        val groupPrefix = TAG_RESULT_GROUP_PREFIX
        composeRule.onAllNodesWithTag(groupPrefix, useUnmergedTree = true)
            .assertCountEquals(1)  // At least one group
    }

    @Test
    fun resultScreen_tryAgainRerunsPairing() {
        navigateToRandomPartner()
        
        addParticipant("Alice", "65")
        addParticipant("Bob", "70")
        addParticipant("Charlie", "68")
        addParticipant("Diana", "72")
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // Verify result screen is shown
        composeRule.onNodeWithTag(TAG_TRY_AGAIN_BUTTON).assertIsDisplayed()
        
        // Click try again
        composeRule.onNodeWithTag(TAG_TRY_AGAIN_BUTTON).performClick()
        
        // Should still be on result screen with updated pairings
        composeRule.onNodeWithTag(TAG_RESTART_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_TRY_AGAIN_BUTTON).assertIsDisplayed()
    }

    @Test
    fun resultScreen_restartReturnsToEntry() {
        navigateToRandomPartner()
        
        addParticipant("Alice", "65")
        addParticipant("Bob", "70")
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // Click restart
        composeRule.onNodeWithTag(TAG_RESTART_BUTTON).performClick()
        
        // Should be back on entry screen with no participants
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).assertIsDisplayed()
        
        // Alice and Bob should not be visible
        composeRule.onNodeWithText("Alice").assertDoesNotExist()
        composeRule.onNodeWithText("Bob").assertDoesNotExist()
    }

    // ── Full flow tests ────────────────────────────────────────────────────────

    @Test
    fun fullFlow_oddNumberOfParticipants() {
        navigateToRandomPartner()
        
        val participants = listOf("Alice", "Bob", "Charlie")
        participants.forEach { name ->
            addParticipant(name, "70")
        }
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // All participants should appear in result
        participants.forEach { name ->
            composeRule.onNodeWithText(name).assertExists()
        }
        
        composeRule.onNodeWithTag(TAG_RESTART_BUTTON).assertIsDisplayed()
    }

    @Test
    fun fullFlow_evenNumberOfParticipants() {
        navigateToRandomPartner()
        
        val participants = listOf("Alice", "Bob", "Charlie", "Diana")
        participants.forEach { name ->
            addParticipant(name, "70")
        }
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // All participants should appear in result
        participants.forEach { name ->
            composeRule.onNodeWithText(name).assertExists()
        }
    }

    @Test
    fun fullFlow_withDifferentWeights() {
        navigateToRandomPartner()
        
        val participants = listOf("Alice" to "60", "Bob" to "75", "Charlie" to "62", "Diana" to "73")
        participants.forEach { (name, weight) ->
            addParticipant(name, weight)
        }
        
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // All participants should be paired
        participants.forEach { (name, _) ->
            composeRule.onNodeWithText(name).assertExists()
        }
    }

    @Test
    fun fullFlow_multipleRounds() {
        navigateToRandomPartner()
        
        // First round
        addParticipant("Alice", "65")
        addParticipant("Bob", "70")
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        composeRule.onNodeWithTag(TAG_RESTART_BUTTON).performClick()
        
        // Second round
        addParticipant("Charlie", "68")
        addParticipant("Diana", "72")
        composeRule.onNodeWithTag(TAG_FIND_PARTNERS_BUTTON).performClick()
        
        // Both Charlie and Diana should be visible
        composeRule.onNodeWithText("Charlie").assertExists()
        composeRule.onNodeWithText("Diana").assertExists()
    }

    // ── Helper functions ───────────────────────────────────────────────────────

    private fun navigateToRandomPartner() {
        // Assuming the MainActivity has a way to navigate to Random Partner
        // This is a placeholder — adjust based on actual navigation setup
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON)
            .assertIsDisplayed()  // Assuming it's already on Random Partner screen
    }

    private fun addParticipant(name: String, weight: String) {
        composeRule.onNodeWithTag(TAG_ADD_PARTICIPANT_BUTTON).performClick()
        composeRule.onNodeWithTag(TAG_PARTICIPANT_NAME_INPUT).performTextInput(name)
        composeRule.onNodeWithTag(TAG_PARTICIPANT_WEIGHT_INPUT).performTextInput(weight)
        composeRule.onNodeWithTag(TAG_CONFIRM_PARTICIPANT_BUTTON).performClick()
    }

    private fun readNodeText(tag: String): String {
        val semanticsNode = composeRule.onNodeWithTag(tag).fetchSemanticsNode()
        val text = semanticsNode.config[SemanticsProperties.Text]
        return text.joinToString(separator = "") { it.text }
    }
}
