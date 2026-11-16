package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.model.BackgroundTheme
import com.example.model.BoardTheme
import com.example.model.CatSkin
import com.example.model.FruitSkin
import com.example.model.HomeModal
import com.example.model.StoreTab
import com.example.ui.components.MeadowBackground
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelSelectDialog
import com.example.ui.screens.ProfileDialog
import com.example.ui.screens.PurchaseCancelledTopToast
import com.example.ui.screens.PurchaseSuccessfulPopup
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.StoreDialog
import com.example.ui.screens.ThemeShopScreen
import com.example.ui.theme.FruitBlissTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FruitBlissTheme {
                FruitBlissApp()
            }
        }
    }
}

@Composable
fun FruitBlissApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPlayPurchases()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val equippedBackground = BackgroundTheme.fromId(uiState.profile.equippedBackgroundId)
    val equippedCatSkin = CatSkin.fromId(uiState.profile.equippedCatSkinId)
    val equippedBoardTheme = BoardTheme.fromId(uiState.profile.equippedBoardThemeId)
    val equippedFruitSkin = FruitSkin.fromId(uiState.profile.equippedFruitSkinId)

    Box(modifier = Modifier.fillMaxSize()) {
        // Persistent backdrop supporting equipped BackgroundTheme
        MeadowBackground(
            theme = equippedBackground,
            dimmedForModal = uiState.activeModal != HomeModal.NONE || uiState.currentScreen == AppScreen.GAME
        )

        // Primary Screen Switch (Home vs Playable 8x8 Match-3 Game)
        AnimatedContent(
            targetState = uiState.currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_switch"
        ) { screen ->
            when (screen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        profile = uiState.profile,
                        onPlayClick = { viewModel.startLevel(uiState.profile.highestUnlockedLevel) },
                        onOpenModal = { modal, tab -> viewModel.openModal(modal, tab) }
                    )
                }
                AppScreen.GAME -> {
                    GameScreen(
                        gameState = gameState,
                        coins = uiState.profile.coins,
                        boostInventory = uiState.boostInventory,
                        hapticsEnabled = uiState.profile.hapticsEnabled,
                        hasActiveModal = uiState.activeModal != HomeModal.NONE,
                        boardTheme = equippedBoardTheme,
                        fruitSkin = equippedFruitSkin,
                        catSkin = equippedCatSkin,
                        onTileTapped = { pos -> viewModel.onTileTapped(pos) },
                        onTileSwiped = { pos, dR, dC -> viewModel.onTileSwiped(pos, dR, dC) },
                        onActivateBoost = { boost -> viewModel.activateBoost(boost) },
                        onReviveExtraLife = { viewModel.useExtraLifeRevive() },
                        onRetryLevel = { viewModel.startLevel(gameState.level.levelNumber) },
                        onNextLevel = { viewModel.startLevel(gameState.level.levelNumber + 1) },
                        onOpenBoostStore = { viewModel.openModal(HomeModal.STORE, StoreTab.BOOST_FORGE) },
                        onPauseGame = { viewModel.pauseGame() },
                        onResumeGame = { viewModel.resumeGame() },
                        onRequestRestart = { viewModel.requestRestartFromPause() },
                        onConfirmRestart = { viewModel.confirmRestartLevel() },
                        onDismissRestartConfirm = { viewModel.dismissRestartConfirmation() },
                        onExitHome = { viewModel.exitGameToHome() }
                    )
                }
            }
        }

        // Store Overlay (works from Home and mid-Game)
        AnimatedVisibility(
            visible = uiState.activeModal == HomeModal.STORE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            StoreDialog(
                coins = uiState.profile.coins,
                selectedTab = uiState.storeTab,
                boostInventory = uiState.boostInventory,
                pendingCoinPack = uiState.pendingCoinPack,
                pendingBoostPurchase = uiState.pendingBoostPurchase,
                playFormattedPrices = uiState.playFormattedPrices,
                billingInProgress = uiState.billingPurchaseInProgress,
                purchaseErrorMessage = uiState.purchaseErrorMessage,
                hasPurchaseSuccess = uiState.purchaseSuccessReward != null,
                feedbackMessage = uiState.storeFeedbackMessage,
                isDemoPurchaseMode = uiState.isDemoPurchaseMode,
                onSelectTab = { viewModel.selectStoreTab(it) },
                onRequestCoinPack = { viewModel.requestCoinPackPurchase(it) },
                onRequestBoost = { viewModel.requestBoostPurchase(it) },
                onLaunchBilling = { activity -> viewModel.startGooglePlayPurchase(activity) },
                onDemoOutcome = { outcome -> viewModel.triggerDemoOutcome(outcome) },
                onDismissPending = { viewModel.dismissPendingPurchase() },
                onDismissFeedback = { viewModel.clearStoreFeedback() },
                onClose = { viewModel.closeModal() }
            )
        }

        // Theme Shop Overlay (Separate from Treasure Vault & Boost Forge; uses Virtual Coins)
        AnimatedVisibility(
            visible = uiState.activeModal == HomeModal.THEME_SHOP,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ThemeShopScreen(
                profile = uiState.profile,
                ownedThemeIds = uiState.ownedThemeIds,
                selectedCategory = uiState.selectedThemeCategory,
                pendingPurchase = uiState.pendingThemePurchase,
                errorMessage = uiState.themeShopErrorMessage,
                hasPurchaseSuccess = uiState.purchaseSuccessReward != null,
                onSelectCategory = { viewModel.selectThemeCategory(it) },
                onRequestPurchase = { viewModel.requestThemePurchase(it) },
                onConfirmPurchase = { viewModel.confirmThemePurchase() },
                onCancelPurchase = { viewModel.cancelThemePurchase() },
                onEquipItem = { viewModel.equipThemeItem(it) },
                onResetDefaultBackground = { viewModel.resetToDefaultBackground() },
                onClose = { viewModel.closeModal() }
            )
        }

        // Profile Modal
        AnimatedVisibility(
            visible = uiState.activeModal == HomeModal.PROFILE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ProfileDialog(
                profile = uiState.profile,
                onSaveProfile = { name, avatarId -> viewModel.updateProfile(name, avatarId) },
                onClose = { viewModel.closeModal() }
            )
        }

        // Settings Modal
        AnimatedVisibility(
            visible = uiState.activeModal == HomeModal.SETTINGS,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SettingsDialog(
                profile = uiState.profile,
                onUpdateSettings = { s, h, v -> viewModel.updateSettings(s, h, v) },
                onResetProgress = { viewModel.resetProgress() },
                onClose = { viewModel.closeModal() }
            )
        }

        // Level Select Modal
        AnimatedVisibility(
            visible = uiState.activeModal == HomeModal.LEVEL_SELECT,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LevelSelectDialog(
                highestUnlockedLevel = uiState.profile.highestUnlockedLevel,
                levelProgress = uiState.levelProgress,
                onSelectLevel = { lvl -> viewModel.startLevel(lvl) },
                onClose = { viewModel.closeModal() }
            )
        }

        // Purchase Successful Blocking Popup (Treasure Vault & Boost Forge)
        uiState.purchaseSuccessReward?.let { reward ->
            PurchaseSuccessfulPopup(
                reward = reward,
                onDismiss = { viewModel.dismissPurchaseSuccess() }
            )
        }

        // Purchase Cancelled Top Toast Notification (~2s auto-dismiss)
        PurchaseCancelledTopToast(
            visible = uiState.showPurchaseCancelledToast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
