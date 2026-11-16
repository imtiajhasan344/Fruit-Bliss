package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

import androidx.room.Transaction

@Dao
interface FruitBlissDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM boost_inventory")
    fun observeBoostInventory(): Flow<List<BoostInventoryEntity>>

    @Query("SELECT * FROM boost_inventory WHERE boostId = :boostId")
    suspend fun getBoostOnce(boostId: String): BoostInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBoost(boost: BoostInventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBoosts(boosts: List<BoostInventoryEntity>)

    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun observeLevelProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber")
    suspend fun getLevelOnce(levelNumber: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevel(level: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevels(levels: List<LevelProgressEntity>)

    @Query("SELECT * FROM purchase_history ORDER BY purchaseTimeMillis DESC")
    fun observePurchaseHistory(): Flow<List<PurchaseHistoryEntity>>

    @Query("SELECT * FROM purchase_history WHERE purchaseToken = :purchaseToken LIMIT 1")
    suspend fun getPurchaseByToken(purchaseToken: String): PurchaseHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPurchaseHistory(entry: PurchaseHistoryEntity)

    @Transaction
    suspend fun grantVerifiedCoinPackTransaction(
        entry: PurchaseHistoryEntity,
        coinsToAdd: Int
    ): Boolean {
        val existing = getPurchaseByToken(entry.purchaseToken)
        if (existing != null && (existing.status == "GRANTED" || existing.status == "DEMO_GRANTED")) {
            return false
        }
        upsertPurchaseHistory(entry)
        val profile = getProfileOnce() ?: PlayerProfileEntity()
        upsertProfile(profile.copy(coins = profile.coins + coinsToAdd))
        return true
    }

    @Transaction
    suspend fun grantVerifiedBoostTransaction(
        entry: PurchaseHistoryEntity,
        boostId: String,
        quantityToAdd: Int
    ): Boolean {
        val existing = getPurchaseByToken(entry.purchaseToken)
        if (existing != null && (existing.status == "GRANTED" || existing.status == "DEMO_GRANTED")) {
            return false
        }
        upsertPurchaseHistory(entry)
        val existingBoost = getBoostOnce(boostId) ?: BoostInventoryEntity(boostId, 0)
        upsertBoost(existingBoost.copy(quantity = existingBoost.quantity + quantityToAdd))
        return true
    }

    @Query("DELETE FROM level_progress")
    suspend fun clearLevels()

    @Query("DELETE FROM boost_inventory")
    suspend fun clearBoosts()

    @Query("SELECT * FROM unlocked_theme_items")
    fun observeUnlockedThemeItems(): Flow<List<UnlockedThemeItemEntity>>

    @Query("SELECT * FROM unlocked_theme_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getUnlockedThemeItemOnce(itemId: String): UnlockedThemeItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedThemeItemsIgnore(items: List<UnlockedThemeItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUnlockedThemeItem(item: UnlockedThemeItemEntity)

    @Transaction
    suspend fun purchaseThemeItemWithCoins(
        itemId: String,
        category: String,
        priceCoins: Int,
        unlockedAtMillis: Long
    ): ThemePurchaseOutcome {
        if (getUnlockedThemeItemOnce(itemId) != null) {
            return ThemePurchaseOutcome.ALREADY_OWNED
        }
        val profile = getProfileOnce() ?: PlayerProfileEntity()
        if (profile.coins < priceCoins) {
            return ThemePurchaseOutcome.INSUFFICIENT_COINS
        }
        upsertProfile(profile.copy(coins = profile.coins - priceCoins))
        upsertUnlockedThemeItem(
            UnlockedThemeItemEntity(
                itemId = itemId,
                category = category,
                unlockedAtMillis = unlockedAtMillis
            )
        )
        return ThemePurchaseOutcome.SUCCESS
    }
}

enum class ThemePurchaseOutcome {
    SUCCESS,
    ALREADY_OWNED,
    INSUFFICIENT_COINS
}
