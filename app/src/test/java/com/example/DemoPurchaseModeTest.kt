package com.example

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.billing.BillingGatewayFactory
import com.example.billing.DemoPurchaseOutcome
import com.example.data.FruitBlissDatabase
import com.example.model.BoostType
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class DemoPurchaseModeTest {

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

    private fun prepareVibratorAndSystemSettings(): Vibrator? {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        Settings.System.putInt(context.contentResolver, "vibrate_on", 1)
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null) {
            shadowOf(vibrator).setHasVibrator(true)
            shadowOf(vibrator).setHasAmplitudeControl(true)
        }
        return vibrator
    }

    @Test
    fun debugBuildEnablesDemoPurchaseModeForTreasureVaultWithSoundVibrationAndNonPlayLabels() {
        assertTrue(BuildConfig.DEBUG)
        assertTrue(BuildConfig.DEMO_PURCHASES_ENABLED)
        assertTrue(BillingGatewayFactory.isDemoPurchaseModeAvailable)

        prepareVibratorAndSystemSettings()
        val vm = viewModel()
        val recordedSounds = mutableListOf<String>()
        var vibrationCount = 0

        composeTestRule.waitForIdle()
        assertTrue(vm.uiState.value.isDemoPurchaseMode)

        composeTestRule.runOnUiThread {
            vm.soundManager.soundObserverForTest = { recordedSounds += it }
            vm.purchaseHapticsManager.onVibratedForTest = { vibrationCount++ }
        }

        // Store disclaimer must NOT appear on Home Screen
        composeTestRule.onNodeWithTag("store_virtual_coins_disclaimer").assertDoesNotExist()

        composeTestRule.onNodeWithTag("home_store_button").performClick()
        composeTestRule.waitForIdle()
        recordedSounds.clear()
        vibrationCount = 0

        // Store shows exact virtual coins disclaimer above Store items and TEST MODE label
        composeTestRule.onNodeWithTag("store_virtual_coins_disclaimer").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Disclaimer: All coins are virtual and for in-game use only. They have no real-world value and cannot be withdrawn or exchanged for real money."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_test_mode_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("TEST MODE — NO REAL PAYMENT").assertIsDisplayed()

        // Tap 100 Coins ($1.01) in Treasure Vault
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_100").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("coin_pack_purchase_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_test_mode_banner").assertIsDisplayed()
        composeTestRule.onNode(
            hasText("TEST MODE — NO REAL PAYMENT") and
                hasAnyAncestor(hasTestTag("purchase_test_mode_banner"))
        ).assertIsDisplayed()
        composeTestRule.onAllNodes(
            hasText("100 Coins") and
                hasAnyAncestor(hasTestTag("coin_pack_purchase_screen"))
        ).onFirst().assertIsDisplayed()
        composeTestRule.onNode(
            hasTestTag("coin_purchase_price_text") and hasText("$1.01")
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag("demo_cancel_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("demo_fail_button").assertIsDisplayed()

        // Tap BUY COINS -> simulates demo purchase locally without opening Google Play Billing
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            vm.uiState.value.profile.coins == 100 &&
                composeTestRule.onAllNodes(hasTestTag("purchase_success_dialog"))
                    .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()

        // Purchase Successful popup displays correct details + explicit demo non-Google-Play labels
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_test_mode_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_demo_disclaimer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Demo test reward added locally — not a real Google Play transaction.")
            .assertIsDisplayed()
        composeTestRule.onNode(
            hasText("100 Coins") and hasAnyAncestor(hasTestTag("purchase_success_dialog"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Balance : 100 Virtual Coins").assertIsDisplayed()

        // Chime and vibration played once immediately on demo success
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
        assertEquals(1, vibrationCount)

        // Verify Room history marks transaction honestly as DEMO_GRANTED (never verified Google Play)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val history = runBlocking {
            FruitBlissDatabase.getInstance(context).fruitBlissDao().observePurchaseHistory().first()
        }
        assertEquals(1, history.size)
        val entry = history.first()
        assertEquals("DEMO_GRANTED", entry.status)
        assertFalse(entry.verified)
        assertFalse(entry.acknowledged)
        assertTrue(entry.orderId?.startsWith("DEMO-TEST-") == true)

        // Dismiss popup -> no extra sounds
        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
    }

    @Test
    fun demoBoostForgeSupportsCancelFailAndSuccessWithoutDeductingCoins() {
        prepareVibratorAndSystemSettings()
        val vm = viewModel()
        val recordedSounds = mutableListOf<String>()
        var vibrationCount = 0

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.soundManager.soundObserverForTest = { recordedSounds += it }
            vm.purchaseHapticsManager.onVibratedForTest = { vibrationCount++ }
        }

        // First buy 100 demo coins so balance = 100 (proves Boost Forge never deducts virtual coins)
        composeTestRule.onNodeWithTag("home_store_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_100").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            vm.uiState.value.profile.coins == 100
        }
        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()

        val initialHammerCount = vm.uiState.value.boostInventory[BoostType.MAGIC_HAMMER] ?: 0
        recordedSounds.clear()
        vibrationCount = 0

        // Switch to Boost Forge -> open Magic Hammer ($0.10)
        composeTestRule.onNodeWithTag("tab_boost_forge").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("buy_boost_magic_hammer").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("boost_purchase_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_test_mode_banner").assertIsDisplayed()
        composeTestRule.onNode(
            hasTestTag("boost_purchase_price_text") and hasText("$0.10")
        ).assertIsDisplayed()

        // 1. Test Cancelled outcome -> returns to Boost Forge with cancelled toast, zero boost/coin change, no sound/vibe
        composeTestRule.onNodeWithTag("demo_cancel_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("boost_purchase_screen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("purchase_cancelled_toast").assertIsDisplayed()
        assertEquals(initialHammerCount, vm.uiState.value.boostInventory[BoostType.MAGIC_HAMMER])
        assertEquals(100, vm.uiState.value.profile.coins)
        assertTrue(recordedSounds.isEmpty())
        assertEquals(0, vibrationCount)

        // 2. Test Failed outcome -> shows error banner on purchase screen, zero boost/coin change, no sound/vibe
        composeTestRule.onNodeWithTag("buy_boost_magic_hammer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("demo_fail_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("purchase_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Test Mode: Simulated payment failed — no real payment or reward was processed."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertDoesNotExist()
        assertEquals(initialHammerCount, vm.uiState.value.boostInventory[BoostType.MAGIC_HAMMER])
        assertEquals(100, vm.uiState.value.profile.coins)
        assertTrue(recordedSounds.isEmpty())
        assertEquals(0, vibrationCount)

        // 3. Tap BUY -> grants +1 Magic Hammer to test inventory, keeps 100 coins, shows popup + plays chime & vibration
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            (vm.uiState.value.boostInventory[BoostType.MAGIC_HAMMER] ?: 0) == initialHammerCount + 1 &&
                composeTestRule.onAllNodes(hasTestTag("purchase_success_dialog"))
                    .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
        assertEquals(100, vm.uiState.value.profile.coins)
        composeTestRule.onNode(
            hasText("Magic Hammer") and hasAnyAncestor(hasTestTag("purchase_success_dialog"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("x1 Added to Boost Forge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_test_mode_badge").assertIsDisplayed()
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
        assertEquals(1, vibrationCount)
    }

    @Test
    fun demoPurchaseRespectsVibrationSettingAndBlocksDuplicateEventRewards() {
        prepareVibratorAndSystemSettings()
        val vm = viewModel()
        val recordedSounds = mutableListOf<String>()
        var vibrationCount = 0

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.soundManager.soundObserverForTest = { recordedSounds += it }
            vm.purchaseHapticsManager.onVibratedForTest = { vibrationCount++ }
        }

        // Disable Vibration in Settings (Sound stays ON)
        composeTestRule.onNodeWithTag("home_settings_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_vibration_switch").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            !vm.uiState.value.profile.vibrationEnabled
        }
        composeTestRule.onNodeWithTag("settings_close_button").performClick()
        composeTestRule.waitForIdle()

        // Open Store -> Coin Pack 50
        composeTestRule.onNodeWithTag("home_store_button").performClick()
        composeTestRule.waitForIdle()
        recordedSounds.clear()
        vibrationCount = 0

        composeTestRule.onNodeWithTag("buy_coin_pack_pack_50").performClick()
        composeTestRule.waitForIdle()

        // Process same demo purchase token ONCE -> grants 50 coins, chime plays once, vibration skipped (setting OFF)
        composeTestRule.runOnUiThread {
            vm.triggerDemoOutcome(
                outcome = DemoPurchaseOutcome.SUCCESS,
                demoToken = "demo_duplicate_event_token_1"
            )
        }
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            vm.uiState.value.profile.coins == 50 &&
                composeTestRule.onAllNodes(hasTestTag("purchase_success_dialog"))
                    .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
        assertEquals(0, vibrationCount)

        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()

        // Re-open pack and process the EXACT SAME test purchase token again -> duplicate reward MUST be blocked!
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_50").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.triggerDemoOutcome(
                outcome = DemoPurchaseOutcome.SUCCESS,
                demoToken = "demo_duplicate_event_token_1"
            )
        }
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            composeTestRule.onAllNodes(hasTestTag("purchase_error_banner"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()

        assertEquals(50, vm.uiState.value.profile.coins)
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
        assertEquals(0, vibrationCount)
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertDoesNotExist()
    }

    @Test
    fun themeShopContinuesUsingVirtualCoinsEarnedFromDemoPurchases() {
        val vm = viewModel()
        composeTestRule.waitForIdle()

        // Buy 500 demo coins in Treasure Vault
        composeTestRule.onNodeWithTag("home_store_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_500").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            vm.uiState.value.profile.coins == 500
        }
        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("store_close_button").performClick()
        composeTestRule.waitForIdle()

        // Open Theme Shop -> Candy World costs 500 Virtual Coins
        composeTestRule.onNodeWithTag("home_theme_shop_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_buy_bg_candy_world").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_confirm_buy_button").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            vm.uiState.value.profile.coins == 0 &&
                "bg_candy_world" in vm.uiState.value.ownedThemeIds
        }
        composeTestRule.waitForIdle()

        // Theme Shop purchase popup is normal Virtual Coin unlock (not labeled as Demo payment)
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_test_mode_badge").assertDoesNotExist()
        composeTestRule.onAllNodesWithText("Candy World").onFirst().assertIsDisplayed()
    }
}
