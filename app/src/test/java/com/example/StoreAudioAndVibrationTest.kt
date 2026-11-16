package com.example

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import java.time.Duration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.audio.PurchaseHapticsManager
import com.example.billing.BillingEvent
import com.example.billing.BillingGateway
import com.example.data.FruitBlissDatabase
import com.example.data.FruitBlissRepository
import com.example.game.FruitMatchEngine
import com.example.model.StoreCatalog
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class StoreAudioAndVibrationTest {

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

    private class FakeBillingGateway : BillingGateway {
        private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 16)
        override val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

        private val _formattedPrices = MutableStateFlow<Map<String, String>>(emptyMap())
        override val formattedPrices: StateFlow<Map<String, String>> = _formattedPrices.asStateFlow()

        var launchHandler: (String) -> Unit = {}

        override fun start() = Unit
        override fun refreshPurchases() = Unit
        override fun launchPurchase(activity: Activity, productId: String) {
            launchHandler(productId)
        }
        override fun destroy() = Unit

        fun emit(event: BillingEvent) {
            _events.tryEmit(event)
        }
    }

    private fun viewModel(): MainViewModel {
        return ViewModelProvider(composeTestRule.activity)[MainViewModel::class.java]
    }

    private fun ensureVibratorHasHardware(): Vibrator? {
        val context = ApplicationProvider.getApplicationContext<Context>()
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
    fun storeButtonsCancelledPendingFailedProduceNoSoundOrVibrationWhileVerifiedPlaysOnce() {
        val vibrator = ensureVibratorHasHardware()
        val vm = viewModel()
        val gateway = FakeBillingGateway()
        val recordedSounds = mutableListOf<String>()
        var vibrationCount = 0

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.setBillingGatewayForTest(gateway)
            vm.soundManager.soundObserverForTest = { event -> recordedSounds += event }
            vm.purchaseHapticsManager.onVibratedForTest = { vibrationCount++ }
        }

        // Open Store from Home, then clear Home open click so we test Store interactions strictly
        composeTestRule.onNodeWithTag("home_store_button").performClick()
        composeTestRule.waitForIdle()
        recordedSounds.clear()
        vibrationCount = 0

        // 1. Switch tabs & navigate back/forth inside Store -> ZERO sounds
        composeTestRule.onNodeWithTag("tab_boost_forge").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("buy_boost_magic_hammer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("boost_purchase_back_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tab_treasure_vault").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("coin_purchase_back_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue("Store navigation must produce no sounds, got $recordedSounds", recordedSounds.isEmpty())
        assertEquals(0, vibrationCount)

        // 2. Cancelled Play Billing flow -> ZERO sound, ZERO vibration
        gateway.launchHandler = { gateway.emit(BillingEvent.PurchaseCancelled) }
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Cancelled purchase must produce no sounds, got $recordedSounds", recordedSounds.isEmpty())
        assertEquals(0, vibrationCount)

        // 3. Failed / Error Play Billing flow -> ZERO sound, ZERO vibration
        gateway.launchHandler = { gateway.emit(BillingEvent.BillingError("Payment failed")) }
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("coin_purchase_back_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Failed purchase must produce no sounds, got $recordedSounds", recordedSounds.isEmpty())
        assertEquals(0, vibrationCount)

        // 4. Pending Play Billing flow -> ZERO sound, ZERO vibration
        gateway.launchHandler = { id ->
            val p = StoreCatalog.findByProductId(id)!!
            gateway.emit(BillingEvent.PurchasePending(p))
        }
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Pending purchase must produce no sounds, got $recordedSounds", recordedSounds.isEmpty())
        assertEquals(0, vibrationCount)

        // 5. Verified purchase -> plays PURCHASE_SUCCESS + vibrates immediately ONCE
        gateway.launchHandler = { id ->
            val p = StoreCatalog.findByProductId(id)!!
            vm.persistVerifiedPurchaseForTest(
                purchaseToken = "tok-verified-1",
                product = p,
                quantity = 1
            ) {
                gateway.emit(
                    BillingEvent.PurchaseGranted(
                        product = p,
                        quantity = 1,
                        purchaseToken = "tok-verified-1"
                    )
                )
            }
        }
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            recordedSounds.contains("PURCHASE_SUCCESS") && vibrationCount == 1
        }
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
        assertEquals(1, vibrationCount)
        if (vibrator != null) {
            assertTrue(shadowOf(vibrator).isVibrating)
        }

        // Tapping GREAT! & closing Store must add ZERO click sounds
        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("store_close_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)

        // 6. Duplicate transaction delivery for same token -> no second chime or vibration
        val duplicateProduct = StoreCatalog.findByProductId("fruit_bliss_coins_10")!!
        composeTestRule.runOnUiThread {
            gateway.emit(
                BillingEvent.PurchaseGranted(
                    product = duplicateProduct,
                    quantity = 1,
                    purchaseToken = "tok-verified-1"
                )
            )
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
        assertEquals(1, vibrationCount)
    }

    @Test
    fun settingsVibrationToggleDefaultsOnControlsPurchaseVibrationAndPreservesSuccessSound() {
        ensureVibratorHasHardware()
        val vm = viewModel()
        val gateway = FakeBillingGateway()
        val recordedSounds = mutableListOf<String>()
        var vibrationCount = 0

        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.setBillingGatewayForTest(gateway)
            vm.soundManager.soundObserverForTest = { event -> recordedSounds += event }
            vm.purchaseHapticsManager.onVibratedForTest = { vibrationCount++ }
        }

        // Settings shows separate "Vibration" toggle defaulting to ON
        composeTestRule.onNodeWithTag("home_settings_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Vibration").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_vibration_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_vibration_switch").assertIsOn()

        // Turn Vibration OFF and verify persisted state
        composeTestRule.onNodeWithTag("settings_vibration_switch").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            !vm.uiState.value.profile.vibrationEnabled
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_vibration_switch").assertIsOff()
        assertFalse(vm.uiState.value.profile.vibrationEnabled)

        composeTestRule.onNodeWithTag("settings_close_button").performClick()
        composeTestRule.waitForIdle()

        // Verified purchase with Vibration OFF -> chime STILL plays, vibration does NOT fire
        recordedSounds.clear()
        vibrationCount = 0
        composeTestRule.onNodeWithTag("home_store_button").performClick()
        composeTestRule.waitForIdle()
        recordedSounds.clear()

        gateway.launchHandler = { id ->
            val p = StoreCatalog.findByProductId(id)!!
            gateway.emit(
                BillingEvent.PurchaseGranted(
                    product = p,
                    quantity = 1,
                    purchaseToken = "tok-vibe-off-1"
                )
            )
        }
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(50))
            recordedSounds.contains("PURCHASE_SUCCESS")
        }
        assertEquals(listOf("PURCHASE_SUCCESS"), recordedSounds)
        assertEquals(0, vibrationCount)

        // Verify preference remains saved across fresh Room database close & reopen
        val context = ApplicationProvider.getApplicationContext<Context>()
        val persistDbName = "fruit_bliss_vibration_persist_test.db"
        context.deleteDatabase(persistDbName)
        val db1 = androidx.room.Room.databaseBuilder(
            context,
            FruitBlissDatabase::class.java,
            persistDbName
        ).allowMainThreadQueries().build()
        val repo1 = FruitBlissRepository(db1.fruitBlissDao())
        runBlocking {
            repo1.ensureInitialized()
            assertTrue(repo1.profileFlow.first().vibrationEnabled)
            repo1.updateSettings(soundEnabled = true, hapticsEnabled = true, vibrationEnabled = false)
        }
        db1.close()

        val db2 = androidx.room.Room.databaseBuilder(
            context,
            FruitBlissDatabase::class.java,
            persistDbName
        ).allowMainThreadQueries().build()
        runBlocking {
            val loaded = db2.fruitBlissDao().getProfileOnce()!!
            assertFalse(loaded.vibrationEnabled)
        }
        db2.close()
        context.deleteDatabase(persistDbName)
    }

    @Test
    fun purchaseHapticsRespectsSystemSilentModeAndMasterVibrationSetting() {
        ensureVibratorHasHardware()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val haptics = PurchaseHapticsManager(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Silent ringer mode disables vibration
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        assertFalse(haptics.vibratePurchaseSuccess())

        // Normal ringer mode + master system vibrate_on = 0 disables vibration
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        Settings.System.putInt(context.contentResolver, "vibrate_on", 0)
        assertFalse(haptics.vibratePurchaseSuccess())

        // Normal ringer mode + vibrate_on = 1 enables vibration
        Settings.System.putInt(context.contentResolver, "vibrate_on", 1)
        assertTrue(haptics.vibratePurchaseSuccess())
    }

    @Test
    fun normalGameplaySoundEffectsRemainActiveWhenStoreSoundsAreDisabled() {
        val vm = viewModel()
        val recordedSounds = mutableListOf<String>()
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread {
            vm.soundManager.soundObserverForTest = { event -> recordedSounds += event }
            vm.startLevel(1)
        }
        composeTestRule.waitForIdle()
        recordedSounds.clear()

        val move = FruitMatchEngine.findBestValidMove(vm.gameState.value.board)!!
        composeTestRule.runOnUiThread {
            vm.onTileTapped(move.first)
            vm.onTileTapped(move.second)
        }
        composeTestRule.waitForIdle()

        assertTrue("Gameplay tap should play BUTTON_CLICK, got $recordedSounds", recordedSounds.contains("BUTTON_CLICK"))
    }
}
