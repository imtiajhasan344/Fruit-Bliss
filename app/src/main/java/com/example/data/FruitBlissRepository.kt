package com.example.data

import com.example.BuildConfig
import com.example.model.BackgroundTheme
import com.example.model.BoostType
import com.example.model.StoreCatalog
import com.example.model.StoreProduct
import com.example.model.ThemeCategory
import com.example.model.ThemeShopCatalog
import com.example.model.ThemeShopItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FruitBlissRepository(
    private val dao: FruitBlissDao
) {
    val profileFlow: Flow<PlayerProfileEntity> = dao.observeProfile()
        .map { it ?: PlayerProfileEntity() }

    val boostInventoryFlow: Flow<Map<BoostType, Int>> = dao.observeBoostInventory()
        .map { list ->
            val byId = list.associateBy { it.boostId }
            BoostType.entries.associateWith { boost -> byId[boost.id]?.quantity ?: 0 }
        }

    val levelProgressFlow: Flow<List<LevelProgressEntity>> = dao.observeLevelProgress()

    val purchaseHistoryFlow: Flow<List<PurchaseHistoryEntity>> = dao.observePurchaseHistory()

    val ownedThemeIdsFlow: Flow<Set<String>> = dao.observeUnlockedThemeItems()
        .map { items ->
            ThemeShopCatalog.freeStarterIds + items.map { it.itemId }
        }

    suspend fun ensureInitialized() {
        val existingProfile = dao.getProfileOnce()
        if (existingProfile == null) {
            dao.upsertProfile(PlayerProfileEntity())
        }
        val starterBoosts = BoostType.entries.map { boost ->
            val starterQty = when (boost) {
                BoostType.MAGIC_HAMMER -> 2
                BoostType.SHUFFLE -> 2
                BoostType.EXTRA_MOVE -> 1
                BoostType.SUPER_HINT -> 2
                BoostType.ROCKET -> 1
                BoostType.BOMB -> 1
                else -> 0
            }
            val existing = dao.getBoostOnce(boost.id)
            existing ?: BoostInventoryEntity(boostId = boost.id, quantity = starterQty)
        }
        dao.upsertBoosts(starterBoosts)

        val existingLvl1 = dao.getLevelOnce(1)
        if (existingLvl1 == null) {
            val initialLevels = StoreCatalog.levels.map { cfg ->
                LevelProgressEntity(
                    levelNumber = cfg.levelNumber,
                    bestScore = 0,
                    starsEarned = 0,
                    unlocked = cfg.levelNumber == 1
                )
            }
            dao.upsertLevels(initialLevels)
        }

        val starterThemes = listOf(
            UnlockedThemeItemEntity(
                itemId = BackgroundTheme.CLASSIC_MEADOW.id,
                category = ThemeCategory.BACKGROUND.name,
                unlockedAtMillis = 0L
            ),
            UnlockedThemeItemEntity(
                itemId = "cat_classic",
                category = ThemeCategory.CAT_SKIN.name,
                unlockedAtMillis = 0L
            ),
            UnlockedThemeItemEntity(
                itemId = "board_classic",
                category = ThemeCategory.BOARD.name,
                unlockedAtMillis = 0L
            ),
            UnlockedThemeItemEntity(
                itemId = "fruit_classic",
                category = ThemeCategory.FRUIT_SKIN.name,
                unlockedAtMillis = 0L
            )
        )
        dao.insertUnlockedThemeItemsIgnore(starterThemes)
    }

    suspend fun isPurchaseGranted(purchaseToken: String): Boolean {
        val status = dao.getPurchaseByToken(purchaseToken)?.status
        return status == "GRANTED" || status == "DEMO_GRANTED"
    }

    suspend fun recordPendingPurchase(
        purchaseToken: String,
        product: StoreProduct,
        orderId: String?,
        quantity: Int,
        purchaseTimeMillis: Long
    ) {
        val existing = dao.getPurchaseByToken(purchaseToken)
        if (existing?.status == "GRANTED") return
        dao.upsertPurchaseHistory(
            PurchaseHistoryEntity(
                purchaseToken = purchaseToken,
                productId = product.productId,
                orderId = orderId,
                productKind = when (product) {
                    is StoreProduct.CoinPackProduct -> "COIN_PACK"
                    is StoreProduct.BoostProduct -> "BOOST"
                },
                itemTitle = product.displayTitle,
                quantity = quantity.coerceAtLeast(1),
                purchaseTimeMillis = purchaseTimeMillis,
                status = "PENDING",
                acknowledged = false,
                verified = false
            )
        )
    }

    suspend fun recordRejectedPurchase(
        purchaseToken: String,
        productId: String,
        orderId: String?,
        quantity: Int,
        purchaseTimeMillis: Long
    ) {
        val existing = dao.getPurchaseByToken(purchaseToken)
        if (existing?.status == "GRANTED") return
        val product = StoreCatalog.findByProductId(productId)
        dao.upsertPurchaseHistory(
            PurchaseHistoryEntity(
                purchaseToken = purchaseToken,
                productId = productId,
                orderId = orderId,
                productKind = when (product) {
                    is StoreProduct.CoinPackProduct -> "COIN_PACK"
                    is StoreProduct.BoostProduct -> "BOOST"
                    null -> "UNKNOWN"
                },
                itemTitle = product?.displayTitle ?: productId,
                quantity = quantity.coerceAtLeast(1),
                purchaseTimeMillis = purchaseTimeMillis,
                status = "REJECTED",
                acknowledged = false,
                verified = false
            )
        )
    }

    suspend fun grantVerifiedPurchaseOnce(
        purchaseToken: String,
        product: StoreProduct,
        orderId: String?,
        quantity: Int,
        purchaseTimeMillis: Long
    ): Boolean {
        val safeQuantity = quantity.coerceAtLeast(1)
        val entry = PurchaseHistoryEntity(
            purchaseToken = purchaseToken,
            productId = product.productId,
            orderId = orderId,
            productKind = when (product) {
                is StoreProduct.CoinPackProduct -> "COIN_PACK"
                is StoreProduct.BoostProduct -> "BOOST"
            },
            itemTitle = product.displayTitle,
            quantity = safeQuantity,
            purchaseTimeMillis = purchaseTimeMillis,
            status = "GRANTED",
            acknowledged = true,
            verified = true
        )
        return when (product) {
            is StoreProduct.CoinPackProduct -> {
                dao.grantVerifiedCoinPackTransaction(
                    entry = entry,
                    coinsToAdd = product.pack.coins * safeQuantity
                )
            }
            is StoreProduct.BoostProduct -> {
                dao.grantVerifiedBoostTransaction(
                    entry = entry,
                    boostId = product.boost.id,
                    quantityToAdd = safeQuantity
                )
            }
        }
    }

    /**
     * Grants a local DEBUG-only demo purchase reward atomically once per token.
     * Permanently disabled in RELEASE builds.
     */
    suspend fun grantDemoPurchaseOnce(
        purchaseToken: String,
        product: StoreProduct,
        orderId: String,
        quantity: Int,
        purchaseTimeMillis: Long
    ): Boolean {
        if (!BuildConfig.DEBUG || !BuildConfig.DEMO_PURCHASES_ENABLED) {
            return false
        }
        val safeQuantity = quantity.coerceAtLeast(1)
        val entry = PurchaseHistoryEntity(
            purchaseToken = purchaseToken,
            productId = product.productId,
            orderId = orderId,
            productKind = when (product) {
                is StoreProduct.CoinPackProduct -> "COIN_PACK"
                is StoreProduct.BoostProduct -> "BOOST"
            },
            itemTitle = product.displayTitle,
            quantity = safeQuantity,
            purchaseTimeMillis = purchaseTimeMillis,
            status = "DEMO_GRANTED",
            acknowledged = false,
            verified = false
        )
        return when (product) {
            is StoreProduct.CoinPackProduct -> {
                dao.grantVerifiedCoinPackTransaction(
                    entry = entry,
                    coinsToAdd = product.pack.coins * safeQuantity
                )
            }
            is StoreProduct.BoostProduct -> {
                dao.grantVerifiedBoostTransaction(
                    entry = entry,
                    boostId = product.boost.id,
                    quantityToAdd = safeQuantity
                )
            }
        }
    }

    suspend fun consumeBoost(boost: BoostType): Boolean {
        val current = dao.getBoostOnce(boost.id) ?: return false
        if (current.quantity <= 0) return false
        dao.upsertBoost(current.copy(quantity = current.quantity - 1))
        return true
    }

    suspend fun recordLevelWin(
        levelNumber: Int,
        score: Int,
        starsEarned: Int,
        coinsEarned: Int
    ) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        val existingLevel = dao.getLevelOnce(levelNumber)
        val previousStars = existingLevel?.starsEarned ?: 0
        val starDelta = (starsEarned - previousStars).coerceAtLeast(0)
        val newHighestLevel = maxOf(current.highestUnlockedLevel, levelNumber + 1)
        val newTotalScore = current.totalScore + score

        dao.upsertProfile(
            current.copy(
                coins = current.coins + coinsEarned,
                stars = current.stars + starDelta,
                highestUnlockedLevel = newHighestLevel,
                totalScore = newTotalScore,
                bestLevelScore = maxOf(current.bestLevelScore, score),
                gamesPlayed = current.gamesPlayed + 1,
                gamesWon = current.gamesWon + 1
            )
        )

        dao.upsertLevel(
            LevelProgressEntity(
                levelNumber = levelNumber,
                bestScore = maxOf(existingLevel?.bestScore ?: 0, score),
                starsEarned = maxOf(previousStars, starsEarned),
                unlocked = true
            )
        )

        val nextLevel = levelNumber + 1
        val existingNext = dao.getLevelOnce(nextLevel)
        if (existingNext == null) {
            dao.upsertLevel(LevelProgressEntity(levelNumber = nextLevel, unlocked = true))
        } else if (!existingNext.unlocked) {
            dao.upsertLevel(existingNext.copy(unlocked = true))
        }
    }

    suspend fun recordLevelLoss(score: Int) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        val newTotalScore = current.totalScore + (score / 4)
        dao.upsertProfile(
            current.copy(
                totalScore = newTotalScore,
                bestLevelScore = maxOf(current.bestLevelScore, score),
                gamesPlayed = current.gamesPlayed + 1
            )
        )
    }

    suspend fun updateProfile(name: String, avatarId: Int) {
        val cleanName = name.trim().ifEmpty { "Sunny Cat" }.take(18)
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(current.copy(playerName = cleanName, avatarId = avatarId))
    }

    suspend fun updateSettings(
        soundEnabled: Boolean,
        hapticsEnabled: Boolean,
        vibrationEnabled: Boolean = true
    ) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(
            current.copy(
                soundEnabled = soundEnabled,
                musicEnabled = false,
                hapticsEnabled = hapticsEnabled,
                vibrationEnabled = vibrationEnabled
            )
        )
    }

    suspend fun purchaseThemeItem(item: ThemeShopItem): ThemePurchaseOutcome {
        return dao.purchaseThemeItemWithCoins(
            itemId = item.id,
            category = item.category.name,
            priceCoins = item.priceCoins,
            unlockedAtMillis = System.currentTimeMillis()
        )
    }

    suspend fun equipThemeItem(item: ThemeShopItem): Boolean {
        val isStarter = item.id in ThemeShopCatalog.freeStarterIds
        if (!isStarter && dao.getUnlockedThemeItemOnce(item.id) == null) {
            return false
        }
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        val updated = when (item.category) {
            ThemeCategory.BACKGROUND -> current.copy(equippedBackgroundId = item.id)
            ThemeCategory.CAT_SKIN -> current.copy(equippedCatSkinId = item.id)
            ThemeCategory.BOARD -> current.copy(equippedBoardThemeId = item.id)
            ThemeCategory.FRUIT_SKIN -> current.copy(equippedFruitSkinId = item.id)
        }
        dao.upsertProfile(updated)
        return true
    }

    suspend fun resetToDefaultBackground() {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(current.copy(equippedBackgroundId = BackgroundTheme.CLASSIC_MEADOW.id))
    }

    suspend fun resetAllProgress() {
        val previous = dao.getProfileOnce()
        dao.clearLevels()
        dao.clearBoosts()
        dao.upsertProfile(
            PlayerProfileEntity(
                equippedBackgroundId = previous?.equippedBackgroundId ?: BackgroundTheme.CLASSIC_MEADOW.id,
                equippedCatSkinId = previous?.equippedCatSkinId ?: "cat_classic",
                equippedBoardThemeId = previous?.equippedBoardThemeId ?: "board_classic",
                equippedFruitSkinId = previous?.equippedFruitSkinId ?: "fruit_classic"
            )
        )
        ensureInitialized()
    }
}
