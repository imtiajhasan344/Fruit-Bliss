package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val playerName: String = "Sunny Cat",
    val avatarId: Int = 0,
    val coins: Int = 0,
    val stars: Int = 0,
    val highestUnlockedLevel: Int = 1,
    val totalScore: Long = 0L,
    val bestLevelScore: Int = 0,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val equippedBackgroundId: String = "bg_classic_meadow",
    val equippedCatSkinId: String = "cat_classic",
    val equippedBoardThemeId: String = "board_classic",
    val equippedFruitSkinId: String = "fruit_classic"
)

@Entity(tableName = "boost_inventory")
data class BoostInventoryEntity(
    @PrimaryKey val boostId: String,
    val quantity: Int
)

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val bestScore: Int = 0,
    val starsEarned: Int = 0,
    val unlocked: Boolean = false
)

@Entity(tableName = "purchase_history")
data class PurchaseHistoryEntity(
    @PrimaryKey val purchaseToken: String,
    val productId: String,
    val orderId: String?,
    val productKind: String,
    val itemTitle: String,
    val quantity: Int,
    val purchaseTimeMillis: Long,
    val status: String,
    val acknowledged: Boolean,
    val verified: Boolean
)

@Entity(tableName = "unlocked_theme_items")
data class UnlockedThemeItemEntity(
    @PrimaryKey val itemId: String,
    val category: String,
    val unlockedAtMillis: Long
)
