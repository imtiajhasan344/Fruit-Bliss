package com.example

import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.data.FruitBlissDatabase
import com.example.game.FruitMatchEngine
import com.example.game.GridPos
import com.example.model.AppScreen
import com.example.model.BoostType
import com.example.model.StoreCatalog
import com.example.viewmodel.GameStatus
import com.example.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class PauseMenuAndBugFixTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(object : ExternalResource() {
            override fun before() {
                FruitBlissDatabase.resetInstanceForTest(
                    context = ApplicationProvider.getApplicationContext(),
                    useInMemory = true
                )
            }

            override fun after() {
                FruitBlissDatabase.resetInstanceForTest(
                    context = ApplicationProvider.getApplicationContext(),
                    useInMemory = false
                )
            }
        })
        .around(composeTestRule)

    private fun viewModel(): MainViewModel {
        return ViewModelProvider(composeTestRule.activity)[MainViewModel::class.java]
    }

    private fun pumpMainLooper(millis: Long = 100L) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))
    }

    @Test
    fun pauseMenuShowsExactThreeOptionsBlocksGameplayAndResumesExactState() {
        val vm = viewModel()
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.startLevel(1)
        }
        composeTestRule.waitForIdle()

        // Make one valid move so score > 0 and movesLeft < maxMoves before pausing
        val initialMoves = vm.gameState.value.movesLeft
        val validMove = FruitMatchEngine.findBestValidMove(vm.gameState.value.board)!!
        composeTestRule.runOnUiThread {
            vm.onTileTapped(validMove.first)
            vm.onTileTapped(validMove.second)
        }
        composeTestRule.waitUntil(5_000) {
            pumpMainLooper(150L)
            vm.gameState.value.score > 0 && vm.gameState.value.status == GameStatus.PLAYING
        }

        val scoreBeforePause = vm.gameState.value.score
        val movesBeforePause = vm.gameState.value.movesLeft
        val boardBeforePause = vm.gameState.value.board.map { row -> row.map { it.id to it.fruit } }
        assertTrue(scoreBeforePause > 0)
        assertTrue(movesBeforePause < initialMoves)

        // Tap top-left Back button to open Pause Menu
        composeTestRule.onNodeWithTag("game_back_button").performClick()
        composeTestRule.waitForIdle()

        // Pause Menu & exact 3 buttons displayed
        composeTestRule.onNodeWithTag("game_pause_menu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_resume_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_restart_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_home_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Resume").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restart").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back to Home").assertIsDisplayed()
        assertTrue(vm.gameState.value.isPaused)

        // Attempt gameplay actions while paused — must be ignored
        val nextMove = FruitMatchEngine.findBestValidMove(vm.gameState.value.board)
        if (nextMove != null) {
            composeTestRule.runOnUiThread {
                vm.onTileTapped(nextMove.first)
                vm.onTileSwiped(nextMove.first, 0, 1)
            }
        }
        pumpMainLooper(300L)
        assertNull(vm.gameState.value.selectedCell)
        assertEquals(scoreBeforePause, vm.gameState.value.score)
        assertEquals(movesBeforePause, vm.gameState.value.movesLeft)

        // Resume preserves exact score, moves, and board
        composeTestRule.onNodeWithTag("pause_resume_button").performClick()
        composeTestRule.waitForIdle()

        assertFalse(vm.gameState.value.isPaused)
        assertEquals(0, composeTestRule.onAllNodesWithTag("game_pause_menu").fetchSemanticsNodes().size)
        assertEquals(scoreBeforePause, vm.gameState.value.score)
        assertEquals(movesBeforePause, vm.gameState.value.movesLeft)
        assertEquals(
            boardBeforePause,
            vm.gameState.value.board.map { row -> row.map { it.id to it.fruit } }
        )
    }

    @Test
    fun pauseMenuFreezesTimedLevelTimerAndRestartConfirmationResetsCleanly() {
        val vm = viewModel()
        composeTestRule.waitForIdle()
        val expectedTimeLimit = StoreCatalog.getLevelConfig(4).timeLimitSeconds!!
        composeTestRule.runOnUiThread {
            vm.startLevel(4)
        }
        composeTestRule.waitForIdle()

        assertEquals(expectedTimeLimit, vm.gameState.value.timeLeftSeconds)

        // Pause game and advance looper by 3 full seconds -> timer must stay frozen
        composeTestRule.onNodeWithTag("game_back_button").performClick()
        composeTestRule.waitForIdle()
        val frozenTime = vm.gameState.value.timeLeftSeconds!!

        repeat(3) {
            pumpMainLooper(1_050L)
        }
        composeTestRule.waitForIdle()
        assertEquals(frozenTime, vm.gameState.value.timeLeftSeconds)

        // Tap Restart -> opens confirmation popup without resetting yet
        composeTestRule.onNodeWithTag("pause_restart_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pause_restart_confirm_dialog").assertIsDisplayed()
        assertTrue(vm.gameState.value.showRestartConfirmation)

        // Cancel returns to Pause Menu without restarting
        composeTestRule.onNodeWithTag("restart_confirm_cancel_button").performClick()
        composeTestRule.waitForIdle()
        assertFalse(vm.gameState.value.showRestartConfirmation)
        assertTrue(vm.gameState.value.isPaused)
        composeTestRule.onNodeWithTag("game_pause_menu").assertIsDisplayed()

        // Confirm Restart -> resets score, moves, timer, and board
        val oldBoardIds = vm.gameState.value.board.flatten().map { it.id }
        composeTestRule.onNodeWithTag("pause_restart_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("restart_confirm_yes_button").performClick()
        composeTestRule.waitForIdle()

        assertFalse(vm.gameState.value.isPaused)
        assertFalse(vm.gameState.value.showRestartConfirmation)
        assertEquals(0, vm.gameState.value.score)
        assertEquals(StoreCatalog.getLevelConfig(4).maxMoves, vm.gameState.value.movesLeft)
        assertEquals(expectedTimeLimit, vm.gameState.value.timeLeftSeconds)
        assertEquals(GameStatus.PLAYING, vm.gameState.value.status)
        val newBoardIds = vm.gameState.value.board.flatten().map { it.id }
        assertNotEquals(oldBoardIds, newBoardIds)

        // Verify timer runs again now that game is unpaused
        pumpMainLooper(1_100L)
        composeTestRule.waitForIdle()
        assertTrue((vm.gameState.value.timeLeftSeconds ?: expectedTimeLimit) < expectedTimeLimit)
    }

    @Test
    fun pauseBackToHomeStopsSessionAndNeverAwardsCoinsOrWins() {
        val vm = viewModel()
        composeTestRule.waitUntil(5_000) {
            pumpMainLooper(50L)
            vm.uiState.value.profile.id == 1
        }
        val initialCoins = vm.uiState.value.profile.coins
        val initialStars = vm.uiState.value.profile.stars
        val initialWins = vm.uiState.value.profile.gamesWon

        composeTestRule.runOnUiThread {
            vm.startLevel(1)
        }
        composeTestRule.waitForIdle()

        // Score some points first
        val validMove = FruitMatchEngine.findBestValidMove(vm.gameState.value.board)!!
        composeTestRule.runOnUiThread {
            vm.onTileTapped(validMove.first)
            vm.onTileTapped(validMove.second)
        }
        composeTestRule.waitUntil(5_000) {
            pumpMainLooper(150L)
            vm.gameState.value.score > 0 && vm.gameState.value.status == GameStatus.PLAYING
        }

        // Open Pause Menu and tap Back to Home
        composeTestRule.onNodeWithTag("game_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pause_home_button").performClick()
        composeTestRule.waitForIdle()
        pumpMainLooper(200L)

        assertEquals(AppScreen.HOME, vm.uiState.value.currentScreen)
        assertFalse(vm.gameState.value.isPaused)
        assertEquals(initialCoins, vm.uiState.value.profile.coins)
        assertEquals(initialStars, vm.uiState.value.profile.stars)
        assertEquals(initialWins, vm.uiState.value.profile.gamesWon)
    }

    @Test
    fun androidSystemBackHandlesGameplayPauseAndRestartConfirmCorrectly() {
        val vm = viewModel()
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.startLevel(1)
        }
        composeTestRule.waitForIdle()

        // Back during active gameplay -> Pause Menu
        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
        assertTrue(vm.gameState.value.isPaused)
        composeTestRule.onNodeWithTag("game_pause_menu").assertIsDisplayed()

        // Open Restart Confirmation, then system Back -> returns to Pause Menu
        composeTestRule.onNodeWithTag("pause_restart_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(vm.gameState.value.showRestartConfirmation)

        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
        assertFalse(vm.gameState.value.showRestartConfirmation)
        assertTrue(vm.gameState.value.isPaused)
        composeTestRule.onNodeWithTag("game_pause_menu").assertIsDisplayed()

        // System Back from Pause Menu -> Resumes game
        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
        assertFalse(vm.gameState.value.isPaused)
        assertEquals(AppScreen.GAME, vm.uiState.value.currentScreen)
    }

    @Test
    fun initialBoardNeverContainsPreExistingMatchesAcrossSeedsAndVarieties() {
        for (variety in 4..6) {
            for (seed in 0 until 50) {
                val board = FruitMatchEngine.createInitialBoard(varietyCount = variety, random = Random(seed))
                val matches = FruitMatchEngine.findMatches(board)
                assertTrue(
                    "Found pre-existing match for variety=$variety seed=$seed",
                    matches.matchedPositions.isEmpty()
                )
            }
        }
    }

    @Test
    fun shuffleBoostClearsStaleSelectionAndHintHighlights() {
        val vm = viewModel()
        composeTestRule.waitUntil(5_000) {
            pumpMainLooper(50L)
            (vm.uiState.value.boostInventory[BoostType.SHUFFLE] ?: 0) >= 1
        }

        composeTestRule.runOnUiThread {
            vm.startLevel(1)
        }
        composeTestRule.waitForIdle()

        // Select a cell first
        composeTestRule.runOnUiThread {
            vm.onTileTapped(GridPos(0, 0))
        }
        composeTestRule.waitForIdle()
        assertEquals(GridPos(0, 0), vm.gameState.value.selectedCell)

        // Activate Shuffle -> stale selectedCell & hintedPair must be cleared
        composeTestRule.runOnUiThread {
            vm.activateBoost(BoostType.SHUFFLE)
        }
        composeTestRule.waitUntil(5_000) {
            pumpMainLooper(100L)
            vm.gameState.value.selectedCell == null && vm.gameState.value.hintedPair == null
        }

        assertNull(vm.gameState.value.selectedCell)
        assertNull(vm.gameState.value.hintedPair)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-mdpi")
    fun compactScreenRendersHomeModalsGameAndPauseMenuCleanly() {
        val vm = viewModel()
        composeTestRule.waitForIdle()

        // Home screen primary actions visible on compact 320x568 screen
        composeTestRule.onNodeWithTag("play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_store_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_theme_shop_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_profile_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_settings_button").assertIsDisplayed()

        // Profile modal renders & closes
        composeTestRule.onNodeWithTag("home_profile_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_close_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_close_button").performClick()
        composeTestRule.waitForIdle()

        // Settings modal renders & closes
        composeTestRule.onNodeWithTag("home_settings_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_close_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_close_button").performClick()
        composeTestRule.waitForIdle()

        // Level Select modal renders & closes
        composeTestRule.onNodeWithTag("home_levels_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("level_select_grid").assertIsDisplayed()
        composeTestRule.onNodeWithTag("level_select_close_button").performClick()
        composeTestRule.waitForIdle()

        // Start game & verify board + Pause Menu on compact screen
        composeTestRule.runOnUiThread {
            vm.startLevel(1)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("fruit_match_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("game_back_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("game_pause_menu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_resume_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_restart_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_home_button").assertIsDisplayed()
    }
}
