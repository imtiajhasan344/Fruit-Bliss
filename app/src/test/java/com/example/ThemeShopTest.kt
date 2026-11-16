package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.FruitBlissDatabase
import com.example.data.FruitBlissRepository
import com.example.data.ThemePurchaseOutcome
import com.example.model.BackgroundTheme
import com.example.model.BoardTheme
import com.example.model.CatSkin
import com.example.model.FruitSkin
import com.example.model.StoreCatalog
import com.example.model.StoreProduct
import com.example.model.ThemeCategory
import com.example.model.ThemeShopCatalog
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class ThemeShopTest {

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

    private lateinit var database: FruitBlissDatabase
    private lateinit var repository: FruitBlissRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FruitBlissDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FruitBlissRepository(database.fruitBlissDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun catalogMatchesAllCategoriesAndExactVirtualCoinPrices() {
        assertEquals(
            mapOf(
                "Candy World" to 500,
                "Jungle World" to 800,
                "Snow World" to 1000,
                "Galaxy World" to 1500,
                "Ocean World" to 2000,
                "Fantasy World" to 2500
            ),
            ThemeShopCatalog.backgroundThemes.associate { it.title to it.priceCoins }
        )

        assertEquals(
            mapOf(
                "Classic Cat" to 0,
                "Royal Cat" to 500,
                "Ninja Cat" to 800,
                "Space Cat" to 1200,
                "Golden Cat" to 2000
            ),
            ThemeShopCatalog.catSkins.associate { it.title to it.priceCoins }
        )

        assertEquals(
            mapOf(
                "Classic Board" to 0,
                "Candy Board" to 400,
                "Neon Board" to 700,
                "Galaxy Board" to 1000
            ),
            ThemeShopCatalog.boardThemes.associate { it.title to it.priceCoins }
        )

        assertEquals(
            mapOf(
                "Classic Fruits" to 0,
                "Cartoon Fruits" to 400,
                "Crystal Fruits" to 800,
                "Golden Fruits" to 1200
            ),
            ThemeShopCatalog.fruitSkins.associate { it.title to it.priceCoins }
        )

        // All IDs across all 4 categories are unique
        val allIds = ThemeShopCatalog.allItems.map { it.id }
        assertEquals(19, allIds.size)
        assertEquals(19, allIds.toSet().size)
    }

    @Test
    fun repositoryHandlesPurchaseDeductionDuplicatePreventionEquipAndPersistence() = runTest {
        repository.ensureInitialized()

        val initialOwned = repository.ownedThemeIdsFlow.first()
        assertTrue(BackgroundTheme.CLASSIC_MEADOW.id in initialOwned)
        assertTrue(CatSkin.CLASSIC.id in initialOwned)
        assertTrue(BoardTheme.CLASSIC.id in initialOwned)
        assertTrue(FruitSkin.CLASSIC.id in initialOwned)

        val initialProfile = repository.profileFlow.first()
        assertEquals(0, initialProfile.coins)
        assertEquals(BackgroundTheme.CLASSIC_MEADOW.id, initialProfile.equippedBackgroundId)
        assertEquals(CatSkin.CLASSIC.id, initialProfile.equippedCatSkinId)
        assertEquals(BoardTheme.CLASSIC.id, initialProfile.equippedBoardThemeId)
        assertEquals(FruitSkin.CLASSIC.id, initialProfile.equippedFruitSkinId)

        val candyWorld = ThemeShopCatalog.findById(BackgroundTheme.CANDY_WORLD.id)!!
        val royalCat = ThemeShopCatalog.findById(CatSkin.ROYAL.id)!!
        val neonBoard = ThemeShopCatalog.findById(BoardTheme.NEON.id)!!
        val goldenFruits = ThemeShopCatalog.findById(FruitSkin.GOLDEN.id)!!

        // Insufficient virtual coins must reject and deduct 0 coins
        assertEquals(ThemePurchaseOutcome.INSUFFICIENT_COINS, repository.purchaseThemeItem(candyWorld))
        assertEquals(0, repository.profileFlow.first().coins)
        assertFalse(candyWorld.id in repository.ownedThemeIdsFlow.first())

        // Cannot equip unowned item
        assertFalse(repository.equipThemeItem(candyWorld))
        assertEquals(BackgroundTheme.CLASSIC_MEADOW.id, repository.profileFlow.first().equippedBackgroundId)

        // Grant 3,000 virtual coins
        val pack1500 = StoreCatalog.coinPacks.first { it.coins == 1500 }
        assertTrue(
            repository.grantVerifiedPurchaseOnce(
                purchaseToken = "tok_theme_test_3000",
                product = StoreProduct.CoinPackProduct(pack1500),
                orderId = "GPA.THEME.1",
                quantity = 2,
                purchaseTimeMillis = 1_700_000_000_000L
            )
        )
        assertEquals(3000, repository.profileFlow.first().coins)

        // Purchase Candy World (500), Royal Cat (500), Neon Board (700), Golden Fruits (1200)
        // Total = 2900 -> remaining coins = 100
        assertEquals(ThemePurchaseOutcome.SUCCESS, repository.purchaseThemeItem(candyWorld))
        assertEquals(2500, repository.profileFlow.first().coins)
        assertTrue(candyWorld.id in repository.ownedThemeIdsFlow.first())

        // Duplicate purchase attempt must return ALREADY_OWNED and deduct nothing
        assertEquals(ThemePurchaseOutcome.ALREADY_OWNED, repository.purchaseThemeItem(candyWorld))
        assertEquals(2500, repository.profileFlow.first().coins)

        assertEquals(ThemePurchaseOutcome.SUCCESS, repository.purchaseThemeItem(royalCat))
        assertEquals(ThemePurchaseOutcome.SUCCESS, repository.purchaseThemeItem(neonBoard))
        assertEquals(ThemePurchaseOutcome.SUCCESS, repository.purchaseThemeItem(goldenFruits))
        assertEquals(100, repository.profileFlow.first().coins)

        // Equip across all 4 categories and verify persistence
        assertTrue(repository.equipThemeItem(candyWorld))
        assertTrue(repository.equipThemeItem(royalCat))
        assertTrue(repository.equipThemeItem(neonBoard))
        assertTrue(repository.equipThemeItem(goldenFruits))

        val equippedProfile = repository.profileFlow.first()
        assertEquals(candyWorld.id, equippedProfile.equippedBackgroundId)
        assertEquals(royalCat.id, equippedProfile.equippedCatSkinId)
        assertEquals(neonBoard.id, equippedProfile.equippedBoardThemeId)
        assertEquals(goldenFruits.id, equippedProfile.equippedFruitSkinId)

        // Reset to default background only affects background
        repository.resetToDefaultBackground()
        assertEquals(BackgroundTheme.CLASSIC_MEADOW.id, repository.profileFlow.first().equippedBackgroundId)
        assertEquals(royalCat.id, repository.profileFlow.first().equippedCatSkinId)

        // Reset progress preserves permanently unlocked themes
        repository.resetAllProgress()
        val ownedAfterReset = repository.ownedThemeIdsFlow.first()
        assertTrue(candyWorld.id in ownedAfterReset)
        assertTrue(royalCat.id in ownedAfterReset)
        assertTrue(neonBoard.id in ownedAfterReset)
        assertTrue(goldenFruits.id in ownedAfterReset)
    }

    @Test
    fun homeThemeShopButtonPurchaseEquipAndGameplayFlow() {
        composeTestRule.waitForIdle()

        // Home screen has a dedicated THEME SHOP button separate from Store
        composeTestRule.onNodeWithTag("home_store_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_theme_shop_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("THEME SHOP").assertIsDisplayed()

        // Open Theme Shop
        composeTestRule.onNodeWithTag("home_theme_shop_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_shop_screen").assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Theme Shop")).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_shop_balance_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Balance : 0 Virtual Coins").assertIsDisplayed()

        // Tapping Candy World (500 Coins) opens confirmation dialog; Cancel deducts nothing
        composeTestRule.onNodeWithTag("theme_buy_bg_candy_world").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_confirm_dialog").assertIsDisplayed()
        composeTestRule.onAllNodes(hasText("Confirm Purchase")).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Price: 500 Coins").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_confirm_error").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_confirm_cancel_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_confirm_dialog").assertDoesNotExist()
        composeTestRule.onNodeWithText("Balance : 0 Virtual Coins").assertIsDisplayed()

        // Grant 1,500 Virtual Coins into the app database via verified coin pack helper
        val viewModel = ViewModelProvider(composeTestRule.activity)[MainViewModel::class.java]
        val pack1500 = StoreCatalog.coinPacks.first { it.coins == 1500 }
        composeTestRule.runOnUiThread {
            viewModel.persistVerifiedPurchaseForTest(
                purchaseToken = "tok_ui_theme_coins",
                product = StoreProduct.CoinPackProduct(pack1500),
                quantity = 1
            ) {}
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasText("Balance : 1,500 Virtual Coins"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Purchase Candy World (500 Coins) -> confirms, deducts 500 -> shows Purchase Successful!
        composeTestRule.onNodeWithTag("theme_buy_bg_candy_world").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_confirm_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_confirm_buy_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasTestTag("purchase_success_dialog"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(hasText("Purchase Successful!")).onFirst().assertIsDisplayed()
        composeTestRule.onNode(
            hasText("Candy World") and hasAnyAncestor(hasTestTag("purchase_success_dialog"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()

        // Balance is now 1,000; Candy World shows OWNED and EQUIP (no duplicate buy button)
        composeTestRule.onNodeWithText("Balance : 1,000 Virtual Coins").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_buy_bg_candy_world").assertDoesNotExist()
        composeTestRule.onNodeWithTag("theme_equip_bg_candy_world").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_owned_badge_bg_candy_world", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode(
            hasText("OWNED") and hasAnyAncestor(hasTestTag("theme_card_bg_candy_world")),
            useUnmergedTree = true
        ).assertIsDisplayed()

        // Equip Candy World -> switches to EQUIPPED and enables Classic Meadow reset pill
        composeTestRule.onNodeWithTag("theme_equip_bg_candy_world").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasTestTag("theme_equipped_bg_candy_world"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_equipped_bg_candy_world").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reset_default_background_button").assertIsDisplayed()

        // Switch to Fruit Skins category, verify Classic Fruits equipped, buy & equip Cartoon Fruits (400 Coins)
        composeTestRule.onNodeWithTag("theme_filter_${ThemeCategory.FRUIT_SKIN.name}").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_equipped_fruit_classic").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_buy_fruit_cartoon").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_confirm_buy_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasTestTag("purchase_success_dialog"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("purchase_success_great_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Balance : 600 Virtual Coins").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_equip_fruit_cartoon").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasTestTag("theme_equipped_fruit_cartoon"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_equipped_fruit_cartoon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_equip_fruit_classic").assertIsDisplayed()

        // Switch to Cat Skins & Board Themes filters to confirm free starters are equipped
        composeTestRule.onNodeWithTag("theme_filter_${ThemeCategory.CAT_SKIN.name}").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_equipped_cat_classic").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_buy_cat_royal").assertIsDisplayed()

        composeTestRule.onNodeWithTag("theme_filter_${ThemeCategory.BOARD.name}").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_equipped_board_classic").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_buy_board_candy").assertIsDisplayed()

        // Close Theme Shop and start playable level with equipped Candy World + Cartoon Fruits
        composeTestRule.onNodeWithTag("theme_shop_close_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_shop_screen").assertDoesNotExist()

        composeTestRule.onNodeWithTag("play_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("fruit_match_board").assertIsDisplayed()
    }
}
