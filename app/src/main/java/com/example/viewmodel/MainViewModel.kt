package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.audio.PurchaseHapticsManager
import com.example.audio.SoundEffectsManager
import com.example.billing.BillingEvent
import com.example.billing.BillingGateway
import com.example.billing.BillingGatewayFactory
import com.example.billing.DemoBillingCapable
import com.example.billing.DemoPurchaseOutcome
import com.example.billing.PlayBillingManager
import com.example.data.FruitBlissDatabase
import com.example.data.FruitBlissRepository
import com.example.data.LevelProgressEntity
import com.example.data.PlayerProfileEntity
import com.example.data.ThemePurchaseOutcome
import com.example.game.FruitMatchEngine
import com.example.game.GridPos
import com.example.model.AppScreen
import com.example.model.BoostType
import com.example.model.CoinPack
import com.example.model.FruitCell
import com.example.model.HomeModal
import com.example.model.LevelConfig
import com.example.model.StoreCatalog
import com.example.model.StoreProduct
import com.example.model.StoreTab
import com.example.model.ThemeCategory
import com.example.model.ThemeShopCatalog
import com.example.model.ThemeShopItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GameStatus {
    PLAYING,
    ANIMATING,
    WON,
    LOST
}

data class FloatingScorePopup(
    val id: Long,
    val row: Int,
    val col: Int,
    val text: String
)

data class GameUiState(
    val level: LevelConfig = StoreCatalog.getLevelConfig(1),
    val board: List<List<FruitCell>> = emptyList(),
    val score: Int = 0,
    val movesLeft: Int = 20,
    val timeLeftSeconds: Int? = null,
    val freezeSecondsLeft: Int = 0,
    val shieldMovesLeft: Int = 0,
    val doubleCoinsActive: Boolean = false,
    val scoreMultiplierTurns: Int = 0,
    val selectedCell: GridPos? = null,
    val poppingCells: Set<GridPos> = emptySet(),
    val hintedPair: Pair<GridPos, GridPos>? = null,
    val activeTargetingBoost: BoostType? = null,
    val statusBanner: String? = null,
    val floatingPopup: FloatingScorePopup? = null,
    val comboCount: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val isPaused: Boolean = false,
    val showRestartConfirmation: Boolean = false,
    val starsEarned: Int = 0,
    val coinsEarned: Int = 0,
    val hapticTrigger: Int = 0
)

sealed interface PurchaseSuccessReward {
    data class CoinPackReward(
        val pack: CoinPack,
        val quantity: Int = 1,
        val isDemoTransaction: Boolean = false
    ) : PurchaseSuccessReward
    data class BoostReward(
        val boost: BoostType,
        val quantity: Int = 1,
        val isDemoTransaction: Boolean = false
    ) : PurchaseSuccessReward
    data class ThemeItemReward(val item: ThemeShopItem) : PurchaseSuccessReward
}

data class MainUiState(
    val profile: PlayerProfileEntity = PlayerProfileEntity(),
    val boostInventory: Map<BoostType, Int> = emptyMap(),
    val levelProgress: List<LevelProgressEntity> = emptyList(),
    val ownedThemeIds: Set<String> = ThemeShopCatalog.freeStarterIds,
    val currentScreen: AppScreen = AppScreen.HOME,
    val activeModal: HomeModal = HomeModal.NONE,
    val storeTab: StoreTab = StoreTab.TREASURE_VAULT,
    val selectedThemeCategory: ThemeCategory? = null,
    val pendingCoinPack: CoinPack? = null,
    val pendingBoostPurchase: BoostType? = null,
    val pendingThemePurchase: ThemeShopItem? = null,
    val themeShopErrorMessage: String? = null,
    val billingPurchaseInProgress: Boolean = false,
    val purchaseErrorMessage: String? = null,
    val purchaseSuccessReward: PurchaseSuccessReward? = null,
    val showPurchaseCancelledToast: Boolean = false,
    val storeFeedbackMessage: String? = null,
    val playFormattedPrices: Map<String, String> = emptyMap(),
    val isDemoPurchaseMode: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FruitBlissDatabase.getInstance(application).fruitBlissDao()
    private val repository = FruitBlissRepository(dao)
    val soundManager = SoundEffectsManager()
    val purchaseHapticsManager = PurchaseHapticsManager(application)
    private var billingGateway: BillingGateway = BillingGatewayFactory.create(application, repository)
    private var billingCollectionJob: Job? = null
    private var billingPricesJob: Job? = null

    private val _navigationState = MutableStateFlow(NavAndDialogState())
    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    private var timerJob: Job? = null
    private var activeMoveJob: Job? = null
    private var bannerJob: Job? = null
    private var cancelToastJob: Job? = null
    private var popupIdCounter = 1L
    private var gameSessionId = 0L
    private var themePurchaseInFlight = false
    private var boostActionInFlight = false
    private var reviveInFlight = false
    private val celebratedPurchaseTokens = mutableSetOf<String>()

    private data class NavAndDialogState(
        val currentScreen: AppScreen = AppScreen.HOME,
        val activeModal: HomeModal = HomeModal.NONE,
        val storeTab: StoreTab = StoreTab.TREASURE_VAULT,
        val selectedThemeCategory: ThemeCategory? = null,
        val pendingCoinPack: CoinPack? = null,
        val pendingBoostPurchase: BoostType? = null,
        val pendingThemePurchase: ThemeShopItem? = null,
        val themeShopErrorMessage: String? = null,
        val billingPurchaseInProgress: Boolean = false,
        val purchaseErrorMessage: String? = null,
        val purchaseSuccessReward: PurchaseSuccessReward? = null,
        val showPurchaseCancelledToast: Boolean = false,
        val storeFeedbackMessage: String? = null,
        val playFormattedPrices: Map<String, String> = emptyMap(),
        val isDemoPurchaseMode: Boolean = false
    )

    private fun computeDemoPurchaseMode(): Boolean {
        return BuildConfig.DEBUG &&
            BuildConfig.DEMO_PURCHASES_ENABLED &&
            BillingGatewayFactory.isDemoPurchaseModeAvailable &&
            (billingGateway is DemoBillingCapable)
    }

    val uiState: StateFlow<MainUiState> = combine(
        repository.profileFlow,
        repository.boostInventoryFlow,
        repository.levelProgressFlow,
        repository.ownedThemeIdsFlow,
        _navigationState
    ) { profile, boosts, levels, ownedThemes, nav ->
        soundManager.soundEnabled = profile.soundEnabled
        purchaseHapticsManager.vibrationEnabled = profile.vibrationEnabled
        MainUiState(
            profile = profile,
            boostInventory = boosts,
            levelProgress = levels,
            ownedThemeIds = ownedThemes,
            currentScreen = nav.currentScreen,
            activeModal = nav.activeModal,
            storeTab = nav.storeTab,
            selectedThemeCategory = nav.selectedThemeCategory,
            pendingCoinPack = nav.pendingCoinPack,
            pendingBoostPurchase = nav.pendingBoostPurchase,
            pendingThemePurchase = nav.pendingThemePurchase,
            themeShopErrorMessage = nav.themeShopErrorMessage,
            billingPurchaseInProgress = nav.billingPurchaseInProgress,
            purchaseErrorMessage = nav.purchaseErrorMessage,
            purchaseSuccessReward = nav.purchaseSuccessReward,
            showPurchaseCancelledToast = nav.showPurchaseCancelledToast,
            storeFeedbackMessage = nav.storeFeedbackMessage,
            playFormattedPrices = nav.playFormattedPrices,
            isDemoPurchaseMode = nav.isDemoPurchaseMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
        _navigationState.update { it.copy(isDemoPurchaseMode = computeDemoPurchaseMode()) }
        observeBillingEvents()
        billingGateway.start()
    }

    @VisibleForTesting
    internal fun setBillingGatewayForTest(testGateway: BillingGateway) {
        billingGateway.destroy()
        billingGateway = testGateway
        _navigationState.update { it.copy(isDemoPurchaseMode = computeDemoPurchaseMode()) }
        observeBillingEvents()
        billingGateway.start()
    }

    @VisibleForTesting
    internal fun useRealPlayBillingForTest() {
        setBillingGatewayForTest(PlayBillingManager(getApplication(), repository))
    }

    @VisibleForTesting
    internal fun persistVerifiedPurchaseForTest(
        purchaseToken: String,
        product: StoreProduct,
        quantity: Int = 1,
        onPersisted: () -> Unit
    ) {
        viewModelScope.launch {
            val granted = repository.grantVerifiedPurchaseOnce(
                purchaseToken = purchaseToken,
                product = product,
                orderId = "GPA.TEST.$purchaseToken",
                quantity = quantity,
                purchaseTimeMillis = System.currentTimeMillis()
            )
            if (granted) {
                onPersisted()
            }
        }
    }

    private fun observeBillingEvents() {
        billingPricesJob?.cancel()
        billingPricesJob = viewModelScope.launch {
            billingGateway.formattedPrices.collect { prices ->
                _navigationState.update { it.copy(playFormattedPrices = prices) }
            }
        }
        billingCollectionJob?.cancel()
        billingCollectionJob = viewModelScope.launch {
            billingGateway.events.collect { event ->
                when (event) {
                    is BillingEvent.PurchaseGranted -> {
                        val token = event.purchaseToken
                        val shouldCelebrate = token == null || celebratedPurchaseTokens.add(token)
                        if (shouldCelebrate) {
                            soundManager.playPurchaseSuccess()
                            purchaseHapticsManager.vibratePurchaseSuccess()
                        }
                        cancelToastJob?.cancel()
                        val isDemo = event.isDemoTransaction &&
                            BuildConfig.DEBUG &&
                            BuildConfig.DEMO_PURCHASES_ENABLED
                        val reward = when (val p = event.product) {
                            is StoreProduct.CoinPackProduct ->
                                PurchaseSuccessReward.CoinPackReward(
                                    pack = p.pack,
                                    quantity = event.quantity,
                                    isDemoTransaction = isDemo
                                )
                            is StoreProduct.BoostProduct ->
                                PurchaseSuccessReward.BoostReward(
                                    boost = p.boost,
                                    quantity = event.quantity,
                                    isDemoTransaction = isDemo
                                )
                        }
                        _navigationState.update {
                            it.copy(
                                pendingCoinPack = null,
                                pendingBoostPurchase = null,
                                billingPurchaseInProgress = false,
                                purchaseErrorMessage = null,
                                purchaseSuccessReward = reward,
                                showPurchaseCancelledToast = false,
                                storeFeedbackMessage = null
                            )
                        }
                    }

                    is BillingEvent.PurchasePending -> {
                        cancelToastJob?.cancel()
                        _navigationState.update {
                            it.copy(
                                pendingCoinPack = null,
                                pendingBoostPurchase = null,
                                billingPurchaseInProgress = false,
                                purchaseErrorMessage = null,
                                showPurchaseCancelledToast = false,
                                storeFeedbackMessage = "Payment pending for ${event.product.displayTitle}. Reward will be added automatically once completed."
                            )
                        }
                    }

                    BillingEvent.PurchaseCancelled -> {
                        _navigationState.update {
                            it.copy(
                                pendingCoinPack = null,
                                pendingBoostPurchase = null,
                                billingPurchaseInProgress = false,
                                purchaseErrorMessage = null,
                                showPurchaseCancelledToast = true
                            )
                        }
                        scheduleCancelledToastDismiss()
                    }

                    is BillingEvent.BillingError -> {
                        _navigationState.update { state ->
                            val hasPendingScreen =
                                state.pendingCoinPack != null || state.pendingBoostPurchase != null
                            state.copy(
                                billingPurchaseInProgress = false,
                                purchaseErrorMessage = if (hasPendingScreen) event.message else null,
                                storeFeedbackMessage = if (!hasPendingScreen) event.message else state.storeFeedbackMessage
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        activeMoveJob?.cancel()
        bannerJob?.cancel()
        cancelToastJob?.cancel()
        billingCollectionJob?.cancel()
        billingPricesJob?.cancel()
        billingGateway.destroy()
        soundManager.release()
    }

    fun refreshPlayPurchases() {
        billingGateway.refreshPurchases()
    }

    fun openModal(modal: HomeModal, storeTab: StoreTab? = null) {
        soundManager.playButtonClick()
        cancelToastJob?.cancel()
        _navigationState.update {
            it.copy(
                activeModal = modal,
                storeTab = storeTab ?: it.storeTab,
                billingPurchaseInProgress = false,
                purchaseErrorMessage = null,
                purchaseSuccessReward = null,
                showPurchaseCancelledToast = false,
                storeFeedbackMessage = null
            )
        }
        if (modal == HomeModal.STORE) {
            billingGateway.refreshPurchases()
        }
    }

    fun closeModal() {
        val closingStore = _navigationState.value.activeModal == HomeModal.STORE
        if (!closingStore) {
            soundManager.playButtonClick()
        }
        cancelToastJob?.cancel()
        _navigationState.update {
            it.copy(
                activeModal = HomeModal.NONE,
                pendingCoinPack = null,
                pendingBoostPurchase = null,
                pendingThemePurchase = null,
                themeShopErrorMessage = null,
                billingPurchaseInProgress = false,
                purchaseErrorMessage = null,
                purchaseSuccessReward = null,
                showPurchaseCancelledToast = false,
                storeFeedbackMessage = null
            )
        }
    }

    fun selectStoreTab(tab: StoreTab) {
        _navigationState.update {
            it.copy(storeTab = tab, storeFeedbackMessage = null, purchaseErrorMessage = null)
        }
    }

    // --- Theme Shop Virtual Coin Purchase & Equip ---

    fun selectThemeCategory(category: ThemeCategory?) {
        soundManager.playButtonClick()
        _navigationState.update { it.copy(selectedThemeCategory = category) }
    }

    fun requestThemePurchase(item: ThemeShopItem) {
        if (item.id in uiState.value.ownedThemeIds) {
            equipThemeItem(item)
            return
        }
        soundManager.playButtonClick()
        _navigationState.update {
            it.copy(
                pendingThemePurchase = item,
                themeShopErrorMessage = null
            )
        }
    }

    fun cancelThemePurchase() {
        soundManager.playButtonClick()
        _navigationState.update {
            it.copy(
                pendingThemePurchase = null,
                themeShopErrorMessage = null
            )
        }
    }

    fun confirmThemePurchase() {
        if (themePurchaseInFlight) return
        val item = _navigationState.value.pendingThemePurchase ?: return
        themePurchaseInFlight = true
        viewModelScope.launch {
            try {
                when (repository.purchaseThemeItem(item)) {
                    ThemePurchaseOutcome.SUCCESS -> {
                        soundManager.playRewardCollect()
                        _navigationState.update {
                            it.copy(
                                pendingThemePurchase = null,
                                themeShopErrorMessage = null,
                                purchaseSuccessReward = PurchaseSuccessReward.ThemeItemReward(item)
                            )
                        }
                    }
                    ThemePurchaseOutcome.ALREADY_OWNED -> {
                        _navigationState.update {
                            it.copy(
                                pendingThemePurchase = null,
                                themeShopErrorMessage = null
                            )
                        }
                    }
                    ThemePurchaseOutcome.INSUFFICIENT_COINS -> {
                        soundManager.playInvalidSwap()
                        _navigationState.update {
                            it.copy(
                                themeShopErrorMessage = "Not enough Virtual Coins! Need ${item.priceCoins} Coins."
                            )
                        }
                    }
                }
            } finally {
                themePurchaseInFlight = false
            }
        }
    }

    fun equipThemeItem(item: ThemeShopItem) {
        viewModelScope.launch {
            val equipped = repository.equipThemeItem(item)
            if (equipped) {
                soundManager.playButtonClick()
            } else {
                soundManager.playInvalidSwap()
            }
        }
    }

    fun resetToDefaultBackground() {
        viewModelScope.launch {
            repository.resetToDefaultBackground()
            soundManager.playButtonClick()
        }
    }

    fun requestCoinPackPurchase(pack: CoinPack) {
        cancelToastJob?.cancel()
        _navigationState.update {
            it.copy(
                pendingCoinPack = pack,
                pendingBoostPurchase = null,
                billingPurchaseInProgress = false,
                purchaseErrorMessage = null,
                showPurchaseCancelledToast = false,
                storeFeedbackMessage = null
            )
        }
    }

    fun requestBoostPurchase(boost: BoostType) {
        cancelToastJob?.cancel()
        _navigationState.update {
            it.copy(
                pendingBoostPurchase = boost,
                pendingCoinPack = null,
                billingPurchaseInProgress = false,
                purchaseErrorMessage = null,
                showPurchaseCancelledToast = false,
                storeFeedbackMessage = null
            )
        }
    }

    fun dismissPendingPurchase() {
        val hadPending = _navigationState.value.let {
            it.pendingCoinPack != null || it.pendingBoostPurchase != null
        }
        _navigationState.update {
            it.copy(
                pendingCoinPack = null,
                pendingBoostPurchase = null,
                billingPurchaseInProgress = false,
                purchaseErrorMessage = null,
                showPurchaseCancelledToast = hadPending
            )
        }
        if (hadPending) {
            scheduleCancelledToastDismiss()
        }
    }

    private fun scheduleCancelledToastDismiss() {
        cancelToastJob?.cancel()
        cancelToastJob = viewModelScope.launch {
            delay(2000L)
            _navigationState.update { it.copy(showPurchaseCancelledToast = false) }
        }
    }

    fun startGooglePlayPurchase(activity: Activity?) {
        val state = _navigationState.value
        if (state.billingPurchaseInProgress) return
        val productId = state.pendingCoinPack?.productId
            ?: state.pendingBoostPurchase?.productId
            ?: return

        if (computeDemoPurchaseMode()) {
            val demoGateway = billingGateway as? DemoBillingCapable ?: return
            _navigationState.update {
                it.copy(
                    billingPurchaseInProgress = true,
                    purchaseErrorMessage = null
                )
            }
            demoGateway.simulateDemoPurchase(
                productId = productId,
                outcome = DemoPurchaseOutcome.SUCCESS
            )
            return
        }

        if (activity == null) {
            _navigationState.update {
                it.copy(
                    purchaseErrorMessage = "Unable to launch Google Play Billing without an active screen."
                )
            }
            return
        }

        _navigationState.update {
            it.copy(
                billingPurchaseInProgress = true,
                purchaseErrorMessage = null
            )
        }
        billingGateway.launchPurchase(activity, productId)
    }

    fun triggerDemoOutcome(outcome: DemoPurchaseOutcome, demoToken: String? = null) {
        if (!computeDemoPurchaseMode()) return
        val state = _navigationState.value
        if (state.billingPurchaseInProgress) return
        val productId = state.pendingCoinPack?.productId
            ?: state.pendingBoostPurchase?.productId
            ?: return
        val demoGateway = billingGateway as? DemoBillingCapable ?: return
        _navigationState.update {
            it.copy(
                billingPurchaseInProgress = true,
                purchaseErrorMessage = null
            )
        }
        demoGateway.simulateDemoPurchase(
            productId = productId,
            outcome = outcome,
            demoToken = demoToken
        )
    }

    fun dismissPurchaseSuccess() {
        val isStorePopup = _navigationState.value.let { nav ->
            nav.activeModal == HomeModal.STORE ||
                nav.purchaseSuccessReward is PurchaseSuccessReward.CoinPackReward ||
                nav.purchaseSuccessReward is PurchaseSuccessReward.BoostReward
        }
        if (!isStorePopup) {
            soundManager.playButtonClick()
        }
        _navigationState.update { it.copy(purchaseSuccessReward = null) }
    }

    fun clearStoreFeedback() {
        _navigationState.update { it.copy(storeFeedbackMessage = null) }
    }

    fun updateProfile(name: String, avatarId: Int) {
        viewModelScope.launch {
            repository.updateProfile(name, avatarId)
            soundManager.playButtonClick()
        }
    }

    fun updateSettings(
        soundEnabled: Boolean,
        hapticsEnabled: Boolean,
        vibrationEnabled: Boolean = uiState.value.profile.vibrationEnabled
    ) {
        viewModelScope.launch {
            repository.updateSettings(soundEnabled, hapticsEnabled, vibrationEnabled)
            soundManager.soundEnabled = soundEnabled
            purchaseHapticsManager.vibrationEnabled = vibrationEnabled
            if (soundEnabled) soundManager.playButtonClick()
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            gameSessionId++
            timerJob?.cancel()
            activeMoveJob?.cancel()
            bannerJob?.cancel()
            boostActionInFlight = false
            reviveInFlight = false
            _gameState.value = GameUiState()
            repository.resetAllProgress()
            soundManager.playButtonClick()
            closeModal()
        }
    }

    // --- Game Session Controls ---

    fun startLevel(levelNumber: Int = uiState.value.profile.highestUnlockedLevel) {
        soundManager.playButtonClick()
        gameSessionId++
        timerJob?.cancel()
        activeMoveJob?.cancel()
        bannerJob?.cancel()
        boostActionInFlight = false
        reviveInFlight = false

        val config = StoreCatalog.getLevelConfig(levelNumber)
        val board = FruitMatchEngine.createInitialBoard(config.fruitVarietyCount)
        _gameState.value = GameUiState(
            level = config,
            board = board,
            score = 0,
            movesLeft = config.maxMoves,
            timeLeftSeconds = config.timeLimitSeconds,
            status = GameStatus.PLAYING,
            isPaused = false,
            showRestartConfirmation = false,
            statusBanner = "${config.title} — Goal: ${config.targetScore}"
        )
        _navigationState.update {
            it.copy(currentScreen = AppScreen.GAME, activeModal = HomeModal.NONE)
        }
        if (config.timeLimitSeconds != null) {
            startLevelTimer(gameSessionId)
        }
        scheduleClearBanner()
    }

    fun pauseGame() {
        if (_navigationState.value.currentScreen != AppScreen.GAME) return
        val current = _gameState.value
        if (current.status == GameStatus.WON || current.status == GameStatus.LOST) return
        if (current.isPaused) return
        soundManager.playButtonClick()
        bannerJob?.cancel()
        _gameState.update { it.copy(isPaused = true, showRestartConfirmation = false) }
    }

    fun resumeGame() {
        val current = _gameState.value
        if (!current.isPaused) return
        soundManager.playButtonClick()
        _gameState.update { it.copy(isPaused = false, showRestartConfirmation = false) }
        if (_gameState.value.statusBanner != null || _gameState.value.floatingPopup != null) {
            scheduleClearBanner()
        }
    }

    fun requestRestartFromPause() {
        if (!_gameState.value.isPaused) return
        soundManager.playButtonClick()
        _gameState.update { it.copy(showRestartConfirmation = true) }
    }

    fun dismissRestartConfirmation() {
        if (!_gameState.value.showRestartConfirmation) return
        soundManager.playButtonClick()
        _gameState.update { it.copy(showRestartConfirmation = false) }
    }

    fun confirmRestartLevel() {
        val levelNumber = _gameState.value.level.levelNumber
        startLevel(levelNumber)
    }

    fun exitGameToHome() {
        gameSessionId++
        timerJob?.cancel()
        activeMoveJob?.cancel()
        bannerJob?.cancel()
        boostActionInFlight = false
        reviveInFlight = false
        soundManager.playButtonClick()
        _gameState.value = GameUiState()
        _navigationState.update {
            it.copy(currentScreen = AppScreen.HOME, activeModal = HomeModal.NONE)
        }
    }

    private fun isGameSessionValid(sessionId: Long): Boolean {
        return sessionId == gameSessionId && _navigationState.value.currentScreen == AppScreen.GAME
    }

    private suspend fun waitWhilePaused(sessionId: Long): Boolean {
        while (isGameSessionValid(sessionId) &&
            (_gameState.value.isPaused || _navigationState.value.activeModal != HomeModal.NONE)
        ) {
            delay(50L)
        }
        return isGameSessionValid(sessionId)
    }

    private fun startLevelTimer(sessionId: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isGameSessionValid(sessionId)) {
                delay(1000L)
                if (!isGameSessionValid(sessionId)) break
                val current = _gameState.value
                if (current.status == GameStatus.WON || current.status == GameStatus.LOST) break
                if (current.isPaused || _navigationState.value.activeModal != HomeModal.NONE) {
                    continue
                }
                val time = current.timeLeftSeconds ?: break
                if (current.freezeSecondsLeft > 0) {
                    _gameState.update { it.copy(freezeSecondsLeft = it.freezeSecondsLeft - 1) }
                    continue
                }
                val nextTime = time - 1
                if (nextTime <= 0) {
                    _gameState.update { it.copy(timeLeftSeconds = 0) }
                    evaluateEndOfTurnOrTimeout(forceTimeout = true, sessionId = sessionId)
                    break
                } else {
                    _gameState.update { it.copy(timeLeftSeconds = nextTime) }
                }
            }
        }
    }

    fun onTileTapped(pos: GridPos) {
        if (_navigationState.value.currentScreen != AppScreen.GAME ||
            _navigationState.value.activeModal != HomeModal.NONE
        ) return
        val current = _gameState.value
        if (current.isPaused || current.status != GameStatus.PLAYING) return

        // Handle targeted boost (Magic Hammer, Rocket, Bomb)
        val targetBoost = current.activeTargetingBoost
        if (targetBoost != null) {
            executeTargetedBoost(targetBoost, pos)
            return
        }

        val previous = current.selectedCell
        if (previous == null) {
            soundManager.playButtonClick()
            _gameState.update { it.copy(selectedCell = pos, hintedPair = null) }
        } else if (previous == pos) {
            _gameState.update { it.copy(selectedCell = null) }
        } else if (FruitMatchEngine.areAdjacent(previous, pos)) {
            attemptSwap(previous, pos)
        } else {
            soundManager.playButtonClick()
            _gameState.update { it.copy(selectedCell = pos, hintedPair = null) }
        }
    }

    fun onTileSwiped(from: GridPos, dRow: Int, dCol: Int) {
        if (_navigationState.value.currentScreen != AppScreen.GAME ||
            _navigationState.value.activeModal != HomeModal.NONE
        ) return
        val current = _gameState.value
        if (current.isPaused || current.status != GameStatus.PLAYING || current.activeTargetingBoost != null) return
        val to = GridPos(from.row + dRow, from.col + dCol)
        if (to.row !in 0 until FruitMatchEngine.BOARD_SIZE ||
            to.col !in 0 until FruitMatchEngine.BOARD_SIZE
        ) return
        attemptSwap(from, to)
    }

    private fun attemptSwap(a: GridPos, b: GridPos) {
        val current = _gameState.value
        if (current.isPaused || current.status != GameStatus.PLAYING || boostActionInFlight) return
        if (a.row !in 0 until FruitMatchEngine.BOARD_SIZE ||
            a.col !in 0 until FruitMatchEngine.BOARD_SIZE ||
            b.row !in 0 until FruitMatchEngine.BOARD_SIZE ||
            b.col !in 0 until FruitMatchEngine.BOARD_SIZE ||
            current.board.size != FruitMatchEngine.BOARD_SIZE
        ) return

        val sessionId = gameSessionId
        _gameState.update {
            it.copy(
                status = GameStatus.ANIMATING,
                selectedCell = null,
                hintedPair = null
            )
        }

        activeMoveJob?.cancel()
        activeMoveJob = viewModelScope.launch {
            val swapped = FruitMatchEngine.swapCells(current.board, a, b)
            _gameState.update { it.copy(board = swapped) }
            soundManager.playSwap()
            delay(170L)
            if (!waitWhilePaused(sessionId)) return@launch

            val scan = FruitMatchEngine.findMatches(swapped)
            if (scan.matchedPositions.isEmpty()) {
                // Bounce back
                soundManager.playInvalidSwap()
                _gameState.update { it.copy(board = current.board) }
                delay(150L)
                if (!waitWhilePaused(sessionId)) return@launch
                if (_gameState.value.status == GameStatus.ANIMATING) {
                    _gameState.update { it.copy(status = GameStatus.PLAYING) }
                }
                return@launch
            }

            // Valid move consumes 1 move (unless shielded by Time Freeze on move-based level)
            val shielded = _gameState.value.shieldMovesLeft > 0
            _gameState.update {
                it.copy(
                    movesLeft = if (shielded) it.movesLeft else (it.movesLeft - 1).coerceAtLeast(0),
                    shieldMovesLeft = if (shielded) it.shieldMovesLeft - 1 else 0
                )
            }

            resolveCascades(swapped, sessionId)
            if (isGameSessionValid(sessionId)) {
                evaluateEndOfTurnOrTimeout(forceTimeout = false, sessionId = sessionId)
            }
        }
    }

    private suspend fun resolveCascades(startingBoard: List<List<FruitCell>>, sessionId: Long) {
        var board = startingBoard
        var combo = 0
        while (isGameSessionValid(sessionId)) {
            if (!waitWhilePaused(sessionId)) return
            val curStatus = _gameState.value.status
            if (curStatus == GameStatus.WON || curStatus == GameStatus.LOST) return

            val scan = FruitMatchEngine.findMatches(board)
            if (scan.matchedPositions.isEmpty()) break

            combo++
            val expanded = FruitMatchEngine.expandSpecialEffects(board, scan.matchedPositions)
            val turnMult = if (_gameState.value.scoreMultiplierTurns > 0) 2 else 1
            val points = expanded.size * 25 * combo * turnMult
            val centerCell = expanded.first()

            soundManager.playFruitMatch(combo)
            _gameState.update {
                it.copy(
                    poppingCells = expanded,
                    comboCount = combo,
                    score = it.score + points,
                    hapticTrigger = it.hapticTrigger + 1,
                    floatingPopup = FloatingScorePopup(
                        id = popupIdCounter++,
                        row = centerCell.row,
                        col = centerCell.col,
                        text = if (combo > 1) "+$points x$combo!" else "+$points"
                    ),
                    statusBanner = when {
                        combo >= 3 -> "Blissful Combo x$combo!"
                        combo == 2 -> "Sweet Cascade x2!"
                        else -> it.statusBanner
                    }
                )
            }
            delay(180L)
            if (!waitWhilePaused(sessionId)) return
            soundManager.playFruitPop()
            delay(60L)
            if (!waitWhilePaused(sessionId)) return

            board = FruitMatchEngine.collapseAndRefill(
                board = board,
                cleared = expanded,
                groups = scan.groups,
                varietyCount = _gameState.value.level.fruitVarietyCount
            )
            _gameState.update {
                it.copy(
                    board = board,
                    poppingCells = emptySet()
                )
            }
            delay(180L)
        }

        if (!isGameSessionValid(sessionId)) return
        val afterStatus = _gameState.value.status
        if (afterStatus == GameStatus.WON || afterStatus == GameStatus.LOST) return

        // Decrement score multiplier turns if active
        if (_gameState.value.scoreMultiplierTurns > 0) {
            _gameState.update { it.copy(scoreMultiplierTurns = it.scoreMultiplierTurns - 1) }
        }

        // Auto-shuffle if deadlock
        if (FruitMatchEngine.findBestValidMove(board) == null) {
            val reshuffled = FruitMatchEngine.shuffleBoard(
                board = board,
                varietyCount = _gameState.value.level.fruitVarietyCount
            )
            _gameState.update {
                it.copy(
                    board = reshuffled,
                    selectedCell = null,
                    hintedPair = null,
                    statusBanner = "No moves left — Auto Shuffled!"
                )
            }
            scheduleClearBanner()
        }
    }

    private fun evaluateEndOfTurnOrTimeout(forceTimeout: Boolean, sessionId: Long = gameSessionId) {
        if (!isGameSessionValid(sessionId)) return
        val current = _gameState.value
        if (current.status == GameStatus.WON || current.status == GameStatus.LOST) return

        val reachedTarget = current.score >= current.level.targetScore
        val outOfMoves = current.movesLeft <= 0
        val outOfTime = forceTimeout || (current.timeLeftSeconds != null && current.timeLeftSeconds <= 0)

        if (reachedTarget && (outOfMoves || outOfTime || current.score >= current.level.threeStarScore)) {
            triggerWin(sessionId)
        } else if (outOfMoves || outOfTime) {
            if (reachedTarget) {
                triggerWin(sessionId)
            } else {
                triggerLoss(sessionId)
            }
        } else {
            _gameState.update { it.copy(status = GameStatus.PLAYING, comboCount = 0) }
        }
    }

    private fun triggerWin(sessionId: Long = gameSessionId) {
        if (!isGameSessionValid(sessionId)) return
        timerJob?.cancel()
        val current = _gameState.value
        if (current.status == GameStatus.WON || current.status == GameStatus.LOST) return
        val lvl = current.level
        val bonusFromMoves = current.movesLeft * 30
        val finalScore = current.score + bonusFromMoves
        val stars = when {
            finalScore >= lvl.threeStarScore -> 3
            finalScore >= lvl.twoStarScore -> 2
            else -> 1
        }
        val baseCoins = lvl.coinReward + (stars * 10)
        val finalCoins = if (current.doubleCoinsActive) baseCoins * 2 else baseCoins

        soundManager.playLevelComplete()
        if (finalCoins > 0) {
            soundManager.playCoinCollect()
        }
        _gameState.update {
            it.copy(
                score = finalScore,
                status = GameStatus.WON,
                isPaused = false,
                showRestartConfirmation = false,
                starsEarned = stars,
                coinsEarned = finalCoins
            )
        }
        viewModelScope.launch {
            repository.recordLevelWin(
                levelNumber = lvl.levelNumber,
                score = finalScore,
                starsEarned = stars,
                coinsEarned = finalCoins
            )
        }
    }

    private fun triggerLoss(sessionId: Long = gameSessionId) {
        if (!isGameSessionValid(sessionId)) return
        timerJob?.cancel()
        val current = _gameState.value
        if (current.status == GameStatus.WON || current.status == GameStatus.LOST) return
        soundManager.playGameOver()
        _gameState.update {
            it.copy(
                status = GameStatus.LOST,
                isPaused = false,
                showRestartConfirmation = false
            )
        }
        viewModelScope.launch {
            repository.recordLevelLoss(current.score)
        }
    }

    // --- Functional Boost Actions ---

    fun activateBoost(boost: BoostType) {
        if (_navigationState.value.currentScreen != AppScreen.GAME ||
            _navigationState.value.activeModal != HomeModal.NONE
        ) return
        val current = _gameState.value
        val owned = uiState.value.boostInventory[boost] ?: 0

        if (boost == BoostType.EXTRA_LIFE && current.status == GameStatus.LOST) {
            useExtraLifeRevive()
            return
        }

        if (current.isPaused || current.status != GameStatus.PLAYING || boostActionInFlight) return

        if (owned <= 0) {
            soundManager.playInvalidSwap()
            showTransientBanner("Out of ${boost.title}! Get more in Boost Forge.")
            return
        }

        if (current.activeTargetingBoost == boost) {
            _gameState.update { it.copy(activeTargetingBoost = null, statusBanner = null) }
            return
        }

        if (boost.requiresTileTarget) {
            soundManager.playButtonClick()
            _gameState.update {
                it.copy(
                    activeTargetingBoost = boost,
                    selectedCell = null,
                    statusBanner = "Tap any fruit to use ${boost.title}!"
                )
            }
            return
        }

        // Immediate boosts
        val sessionId = gameSessionId
        boostActionInFlight = true
        if (boost == BoostType.MEGA_BONUS) {
            _gameState.update { it.copy(status = GameStatus.ANIMATING) }
        }
        activeMoveJob = viewModelScope.launch {
            try {
                if (!isGameSessionValid(sessionId)) return@launch
                val consumed = repository.consumeBoost(boost)
                if (!consumed || !isGameSessionValid(sessionId)) {
                    if (boost == BoostType.MEGA_BONUS && isGameSessionValid(sessionId)) {
                        _gameState.update { it.copy(status = GameStatus.PLAYING) }
                    }
                    return@launch
                }
                soundManager.playPowerUp()

                when (boost) {
                    BoostType.SHUFFLE -> {
                        val shuffled = FruitMatchEngine.shuffleBoard(
                            current.board,
                            current.level.fruitVarietyCount
                        )
                        _gameState.update {
                            it.copy(
                                board = shuffled,
                                selectedCell = null,
                                hintedPair = null
                            )
                        }
                        showTransientBanner("Board Shuffled!")
                    }
                    BoostType.MEGA_SHUFFLE -> {
                        val shuffled = FruitMatchEngine.shuffleBoard(
                            current.board,
                            current.level.fruitVarietyCount
                        )
                        _gameState.update {
                            it.copy(
                                board = shuffled,
                                selectedCell = null,
                                hintedPair = null,
                                score = it.score + 250
                            )
                        }
                        showTransientBanner("Mega Shuffle! +250 pts")
                        checkEarlyThreeStarWin(sessionId)
                    }
                    BoostType.EXTRA_MOVE -> {
                        _gameState.update { it.copy(movesLeft = it.movesLeft + 5) }
                        showTransientBanner("+5 Extra Moves!")
                    }
                    BoostType.TIME_FREEZE -> {
                        if (current.timeLeftSeconds != null) {
                            _gameState.update { it.copy(freezeSecondsLeft = it.freezeSecondsLeft + 15) }
                            showTransientBanner("Timer Frozen for 15s!")
                        } else {
                            _gameState.update { it.copy(shieldMovesLeft = it.shieldMovesLeft + 2) }
                            showTransientBanner("Next 2 Swaps Cost 0 Moves!")
                        }
                    }
                    BoostType.SUPER_HINT -> {
                        val hint = FruitMatchEngine.findBestValidMove(current.board)
                        _gameState.update { it.copy(hintedPair = hint) }
                        showTransientBanner("Best Move Highlighted!")
                    }
                    BoostType.DOUBLE_COINS -> {
                        _gameState.update { it.copy(doubleCoinsActive = true) }
                        showTransientBanner("2x Coin Reward Active!")
                    }
                    BoostType.ADD_TIME -> {
                        if (current.timeLeftSeconds != null) {
                            _gameState.update { it.copy(timeLeftSeconds = (it.timeLeftSeconds ?: 0) + 30) }
                            showTransientBanner("+30 Seconds Added!")
                        } else {
                            _gameState.update { it.copy(movesLeft = it.movesLeft + 4, score = it.score + 400) }
                            showTransientBanner("+4 Moves & +400 Score!")
                            checkEarlyThreeStarWin(sessionId)
                        }
                    }
                    BoostType.EXTRA_LIFE -> {
                        _gameState.update { it.copy(movesLeft = it.movesLeft + 6) }
                        showTransientBanner("Extra Life! +6 Moves")
                    }
                    BoostType.MEGA_BONUS -> {
                        val cleared = mutableSetOf<GridPos>()
                        for (i in 2..5) {
                            cleared.add(GridPos(3, i))
                            cleared.add(GridPos(4, i))
                            cleared.add(GridPos(i, 3))
                            cleared.add(GridPos(i, 4))
                        }
                        _gameState.update {
                            it.copy(
                                status = GameStatus.ANIMATING,
                                poppingCells = cleared,
                                selectedCell = null,
                                hintedPair = null,
                                scoreMultiplierTurns = it.scoreMultiplierTurns + 3,
                                score = it.score + 800
                            )
                        }
                        showTransientBanner("MEGA BONUS! 2x Score & +800 pts!")
                        delay(260L)
                        if (!waitWhilePaused(sessionId)) return@launch
                        val nextBoard = FruitMatchEngine.collapseAndRefill(
                            board = current.board,
                            cleared = cleared,
                            varietyCount = current.level.fruitVarietyCount
                        )
                        _gameState.update { it.copy(board = nextBoard, poppingCells = emptySet()) }
                        delay(180L)
                        resolveCascades(nextBoard, sessionId)
                        if (isGameSessionValid(sessionId)) {
                            evaluateEndOfTurnOrTimeout(forceTimeout = false, sessionId = sessionId)
                        }
                    }
                    else -> Unit
                }
            } finally {
                boostActionInFlight = false
            }
        }
    }

    private fun executeTargetedBoost(boost: BoostType, target: GridPos) {
        val current = _gameState.value
        if (current.isPaused || current.status != GameStatus.PLAYING || boostActionInFlight) return
        val sessionId = gameSessionId
        boostActionInFlight = true
        _gameState.update {
            it.copy(
                status = GameStatus.ANIMATING,
                activeTargetingBoost = null,
                selectedCell = null,
                hintedPair = null
            )
        }
        activeMoveJob?.cancel()
        activeMoveJob = viewModelScope.launch {
            try {
                if (!isGameSessionValid(sessionId)) return@launch
                val consumed = repository.consumeBoost(boost)
                if (!consumed || !isGameSessionValid(sessionId)) {
                    if (isGameSessionValid(sessionId)) {
                        _gameState.update { it.copy(status = GameStatus.PLAYING) }
                    }
                    return@launch
                }
                soundManager.playPowerUp()
                val cleared = mutableSetOf<GridPos>()
                when (boost) {
                    BoostType.MAGIC_HAMMER -> cleared.add(target)
                    BoostType.ROCKET -> {
                        for (i in 0 until FruitMatchEngine.BOARD_SIZE) {
                            cleared.add(GridPos(target.row, i))
                            cleared.add(GridPos(i, target.col))
                        }
                    }
                    BoostType.BOMB -> {
                        for (dr in -1..1) {
                            for (dc in -1..1) {
                                val r = target.row + dr
                                val c = target.col + dc
                                if (r in 0 until FruitMatchEngine.BOARD_SIZE &&
                                    c in 0 until FruitMatchEngine.BOARD_SIZE
                                ) {
                                    cleared.add(GridPos(r, c))
                                }
                            }
                        }
                    }
                    else -> Unit
                }

                val bonusPts = cleared.size * 35
                _gameState.update {
                    it.copy(
                        poppingCells = cleared,
                        score = it.score + bonusPts,
                        hapticTrigger = it.hapticTrigger + 1,
                        floatingPopup = FloatingScorePopup(
                            id = popupIdCounter++,
                            row = target.row,
                            col = target.col,
                            text = "+$bonusPts!"
                        )
                    )
                }
                delay(180L)
                if (!waitWhilePaused(sessionId)) return@launch
                soundManager.playFruitPop()
                delay(70L)
                if (!waitWhilePaused(sessionId)) return@launch
                val nextBoard = FruitMatchEngine.collapseAndRefill(
                    board = current.board,
                    cleared = cleared,
                    varietyCount = current.level.fruitVarietyCount
                )
                _gameState.update { it.copy(board = nextBoard, poppingCells = emptySet()) }
                delay(180L)
                resolveCascades(nextBoard, sessionId)
                if (isGameSessionValid(sessionId)) {
                    evaluateEndOfTurnOrTimeout(forceTimeout = false, sessionId = sessionId)
                }
            } finally {
                boostActionInFlight = false
            }
        }
    }

    fun useExtraLifeRevive() {
        val current = _gameState.value
        if (current.status != GameStatus.LOST || reviveInFlight) return
        val sessionId = gameSessionId
        val owned = uiState.value.boostInventory[BoostType.EXTRA_LIFE] ?: 0
        reviveInFlight = true
        viewModelScope.launch {
            try {
                val canRevive = if (owned > 0 && isGameSessionValid(sessionId)) {
                    repository.consumeBoost(BoostType.EXTRA_LIFE)
                } else {
                    false
                }
                if (canRevive && isGameSessionValid(sessionId)) {
                    soundManager.playPowerUp()
                    _gameState.update {
                        it.copy(
                            status = GameStatus.PLAYING,
                            isPaused = false,
                            showRestartConfirmation = false,
                            movesLeft = it.movesLeft + 5,
                            timeLeftSeconds = it.timeLeftSeconds?.let { t -> t + 30 }
                        )
                    }
                    if (current.timeLeftSeconds != null) {
                        startLevelTimer(sessionId)
                    }
                    showTransientBanner("Revived! +5 Moves!")
                } else {
                    soundManager.playInvalidSwap()
                }
            } finally {
                reviveInFlight = false
            }
        }
    }

    private fun checkEarlyThreeStarWin(sessionId: Long = gameSessionId) {
        if (!isGameSessionValid(sessionId)) return
        val current = _gameState.value
        if (current.score >= current.level.threeStarScore) {
            triggerWin(sessionId)
        }
    }

    private fun showTransientBanner(message: String) {
        _gameState.update { it.copy(statusBanner = message) }
        scheduleClearBanner()
    }

    private fun scheduleClearBanner() {
        bannerJob?.cancel()
        val sessionId = gameSessionId
        bannerJob = viewModelScope.launch {
            delay(2200L)
            if (isGameSessionValid(sessionId) && !_gameState.value.isPaused) {
                _gameState.update { it.copy(statusBanner = null, floatingPopup = null) }
            }
        }
    }
}
