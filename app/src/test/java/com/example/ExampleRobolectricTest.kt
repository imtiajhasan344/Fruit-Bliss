package com.example

import android.app.Activity
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.billing.BillingEvent
import com.example.billing.BillingGateway
import com.example.data.FruitBlissDatabase
import com.example.model.StoreCatalog
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class ExampleRobolectricTest {

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

    private class ScriptedTestBillingGateway : BillingGateway {
        private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 16)
        override val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

        private val _formattedPrices = MutableStateFlow<Map<String, String>>(emptyMap())
        override val formattedPrices: StateFlow<Map<String, String>> = _formattedPrices.asStateFlow()

        var launchHandler: (String) -> Unit = {}
        val launchedProductIds = mutableListOf<String>()

        override fun start() = Unit
        override fun refreshPurchases() = Unit
        override fun launchPurchase(activity: Activity, productId: String) {
            launchedProductIds += productId
            launchHandler(productId)
        }
        override fun destroy() = Unit

        fun emit(event: BillingEvent) {
            _events.tryEmit(event)
        }

        fun setFormattedPrices(prices: Map<String, String>) {
            _formattedPrices.value = prices
        }
    }

    @Test
    fun appNameMatchesFruitBliss() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Fruit Bliss", context.getString(R.string.app_name))
    }

    @Test
    fun homeScreenPlayBillingStoreAndGameFlow() {
        composeTestRule.waitForIdle()

        // Home elements visible; Leaderboard, Daily Rewards, and Star Box completely removed
        composeTestRule.onNodeWithTag("add_coins_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_store_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_theme_shop_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_profile_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_settings_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_settings_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("star_info_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_daily_rewards_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("top_daily_rewards_button").assertDoesNotExist()
        composeTestRule.onNodeWithText("Daily Rewards", substring = true, ignoreCase = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("top_leaderboard_button").assertDoesNotExist()
        composeTestRule.onNodeWithText("Leaderboard", substring = true, ignoreCase = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Leader Board", substring = true, ignoreCase = true).assertDoesNotExist()

        // Open Store -> Treasure Vault & verify 0 initial balance
        composeTestRule.onNodeWithTag("home_store_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("treasure_vault_grid").assertIsDisplayed()
        composeTestRule.onNodeWithText("Balance : 0 Virtual Coins").assertIsDisplayed()

        // Tap 10 Coins ($0.10) -> opens full-screen purchase page; Back cancels without granting
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("coin_pack_purchase_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("coin_purchase_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("BUY COINS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
        composeTestRule.onNodeWithTag("coin_purchase_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("coin_pack_purchase_screen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("purchase_cancelled_toast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Purchase Cancelled").assertIsDisplayed()
        composeTestRule.onNodeWithText("Balance : 0 Virtual Coins").assertIsDisplayed()

        // Debug builds show TEST MODE — NO REAL PAYMENT label on Store by default
        composeTestRule.onNodeWithTag("store_test_mode_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("TEST MODE — NO REAL PAYMENT").assertIsDisplayed()

        // Switch to real PlayBillingManager to verify Release billing NEVER fakes coins without Play Store
        val viewModel = ViewModelProvider(composeTestRule.activity)[MainViewModel::class.java]
        composeTestRule.runOnUiThread {
            viewModel.useRealPlayBillingForTest()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("buy_coin_pack_pack_10").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("purchase_error_banner"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("purchase_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertDoesNotExist()

        // Wire billing gateway driver to verify verified Play Billing UI responses
        val gateway = ScriptedTestBillingGateway()
        gateway.launchHandler = { productId ->
            val product = StoreCatalog.findByProductId(productId)!!
            viewModel.persistVerifiedPurchaseForTest(
                purchaseToken = "tok_test_coins_10",
                product = product,
                quantity = 1
            ) {
                gateway.emit(BillingEvent.PurchaseGranted(product, quantity = 1))
            }
        }
        composeTestRule.runOnUiThread {
            viewModel.setBillingGatewayForTest(gateway)
            gateway.setFormattedPrices(mapOf("fruit_bliss_coins_10" to "€0.19"))
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasTestTag("coin_purchase_price_text") and
                androidx.compose.ui.test.hasText("€0.19")
        ).assertIsDisplayed()
        composeTestRule.runOnUiThread {
            gateway.setFormattedPrices(emptyMap())
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasTestTag("buy_coin_pack_pack_10") and
                androidx.compose.ui.test.hasText("$0.10")
        ).assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasTestTag("coin_purchase_price_text") and
                androidx.compose.ui.test.hasText("$0.10")
        ).assertIsDisplayed()

        // Complete verified Coin Pack 10 purchase -> grants 10 coins & opens Purchase Successful! popup
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasText("Balance : 10 Virtual Coins"))
                .fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule
                    .onAllNodes(androidx.compose.ui.test.hasTestTag("purchase_success_dialog"))
                    .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf("fruit_bliss_coins_10"), gateway.launchedProductIds)
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_checkmark").assertIsDisplayed()
        composeTestRule.onAllNodes(
            androidx.compose.ui.test.hasText("Purchase Successful!")
        ).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Reward Added!").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_reward_name").assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasText("10 Coins") and
                androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag("purchase_success_dialog"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Your purchase was completed successfully.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_great_button").assertIsDisplayed()

        // Clicking outside the popup must NOT close it
        composeTestRule.onNodeWithTag("purchase_success_backdrop").performTouchInput {
            click(Offset(10f, 10f))
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertIsDisplayed()

        // Clicking GREAT! dismisses popup without reversing balance
        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithText("Balance : 10 Virtual Coins").assertIsDisplayed()

        // Switch to Boost Forge tab & verify Magic Hammer ($0.10) does not show virtual coin deductions
        composeTestRule.onNodeWithTag("tab_boost_forge").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("boost_forge_grid").assertIsDisplayed()
        composeTestRule.onNodeWithText("Magic Hammer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mega Bonus").assertIsDisplayed()
        composeTestRule.onNodeWithTag("buy_boost_magic_hammer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("boost_purchase_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boost_purchase_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boost_purchase_price_text").assertIsDisplayed()
        composeTestRule.onAllNodes(
            androidx.compose.ui.test.hasText("Magic Hammer") and
                androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag("boost_purchase_screen"))
        ).onFirst().assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasText("$0.10") and
                androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag("boost_purchase_screen"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("BUY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()

        // Tap Back -> returns to Boost Forge without deducting coins & shows Purchase Cancelled toast
        composeTestRule.onNodeWithTag("boost_purchase_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("boost_purchase_screen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("purchase_cancelled_toast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Balance : 10 Virtual Coins").assertIsDisplayed()

        // Tap Magic Hammer and test Pending Payment -> returns to Store with pending notice, no popup, balance untouched
        gateway.launchHandler = { productId ->
            val product = StoreCatalog.findByProductId(productId)!!
            gateway.emit(BillingEvent.PurchasePending(product))
        }
        composeTestRule.onNodeWithTag("buy_boost_magic_hammer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("store_feedback_banner"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithText("Balance : 10 Virtual Coins").assertIsDisplayed()

        // Tap Magic Hammer again and complete verified Play purchase -> balance stays 10 Virtual Coins (NEVER deducted!)
        gateway.launchHandler = { productId ->
            val product = StoreCatalog.findByProductId(productId)!!
            viewModel.persistVerifiedPurchaseForTest(
                purchaseToken = "tok_test_boost_hammer",
                product = product,
                quantity = 1
            ) {
                gateway.emit(BillingEvent.PurchaseGranted(product, quantity = 1))
            }
        }
        composeTestRule.onNodeWithTag("buy_boost_magic_hammer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("boost_purchase_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_purchase_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("purchase_success_dialog"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("boost_purchase_screen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.hasText("Magic Hammer") and
                androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag("purchase_success_dialog"))
        ).assertIsDisplayed()

        // System Back dismisses popup; virtual coin balance must NEVER have been deducted (still 10)
        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("purchase_success_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithText("Balance : 10 Virtual Coins").assertIsDisplayed()

        // Close Store and launch playable 8x8 game board
        composeTestRule.onNodeWithTag("store_close_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("play_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("fruit_match_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("game_boost_dock").assertIsDisplayed()
    }

    @Test
    fun settingsShowsSoundEffectsToggleNoBackgroundMusicAndPrivacyPolicy() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_settings_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_sound_switch").assertIsDisplayed()
        composeTestRule.onNodeWithText("Background Music: OFF  •  Offline Mode Active").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_music_switch").assertDoesNotExist()
        composeTestRule.onNodeWithTag("settings_sound_switch").performClick()
        composeTestRule.waitForIdle()

        // Verify Privacy Policy & Data dialog opens and closes cleanly
        composeTestRule.onNodeWithTag("settings_privacy_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_privacy_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("privacy_policy_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("privacy_policy_close_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("privacy_policy_dialog").assertDoesNotExist()

        composeTestRule.onNodeWithTag("settings_close_button").performClick()
    }
}
