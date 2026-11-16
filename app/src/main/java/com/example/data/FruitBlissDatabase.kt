package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        BoostInventoryEntity::class,
        LevelProgressEntity::class,
        PurchaseHistoryEntity::class,
        UnlockedThemeItemEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class FruitBlissDatabase : RoomDatabase() {
    abstract fun fruitBlissDao(): FruitBlissDao

    companion object {
        @Volatile
        private var INSTANCE: FruitBlissDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE player_profile SET coins = 0, stars = 0 " +
                        "WHERE id = 1 AND coins = 3420 AND stars = 45 AND gamesPlayed = 0 AND totalScore = 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE player_profile SET musicEnabled = 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `purchase_history` (
                        `purchaseToken` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `orderId` TEXT,
                        `productKind` TEXT NOT NULL,
                        `itemTitle` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `purchaseTimeMillis` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `acknowledged` INTEGER NOT NULL,
                        `verified` INTEGER NOT NULL,
                        PRIMARY KEY(`purchaseToken`)
                    )
                    """.trimIndent()
                )
            }
        }

        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `leaderboard_entries`")
            }
        }

        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `player_profile` ADD COLUMN `equippedBackgroundId` TEXT NOT NULL DEFAULT 'bg_classic_meadow'"
                )
                db.execSQL(
                    "ALTER TABLE `player_profile` ADD COLUMN `equippedCatSkinId` TEXT NOT NULL DEFAULT 'cat_classic'"
                )
                db.execSQL(
                    "ALTER TABLE `player_profile` ADD COLUMN `equippedBoardThemeId` TEXT NOT NULL DEFAULT 'board_classic'"
                )
                db.execSQL(
                    "ALTER TABLE `player_profile` ADD COLUMN `equippedFruitSkinId` TEXT NOT NULL DEFAULT 'fruit_classic'"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `unlocked_theme_items` (
                        `itemId` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `unlockedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`itemId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `unlocked_theme_items` (`itemId`, `category`, `unlockedAtMillis`) VALUES
                    ('bg_classic_meadow', 'BACKGROUND', 0),
                    ('cat_classic', 'CAT_SKIN', 0),
                    ('board_classic', 'BOARD', 0),
                    ('fruit_classic', 'FRUIT_SKIN', 0)
                    """.trimIndent()
                )
            }
        }

        internal val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `player_profile_new` (
                        `id` INTEGER NOT NULL,
                        `playerName` TEXT NOT NULL,
                        `avatarId` INTEGER NOT NULL,
                        `coins` INTEGER NOT NULL,
                        `stars` INTEGER NOT NULL,
                        `highestUnlockedLevel` INTEGER NOT NULL,
                        `totalScore` INTEGER NOT NULL,
                        `bestLevelScore` INTEGER NOT NULL,
                        `gamesPlayed` INTEGER NOT NULL,
                        `gamesWon` INTEGER NOT NULL,
                        `soundEnabled` INTEGER NOT NULL,
                        `musicEnabled` INTEGER NOT NULL,
                        `hapticsEnabled` INTEGER NOT NULL,
                        `equippedBackgroundId` TEXT NOT NULL,
                        `equippedCatSkinId` TEXT NOT NULL,
                        `equippedBoardThemeId` TEXT NOT NULL,
                        `equippedFruitSkinId` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `player_profile_new` (
                        `id`, `playerName`, `avatarId`, `coins`, `stars`,
                        `highestUnlockedLevel`, `totalScore`, `bestLevelScore`,
                        `gamesPlayed`, `gamesWon`, `soundEnabled`, `musicEnabled`,
                        `hapticsEnabled`, `equippedBackgroundId`, `equippedCatSkinId`,
                        `equippedBoardThemeId`, `equippedFruitSkinId`
                    )
                    SELECT
                        `id`, `playerName`, `avatarId`, `coins`, `stars`,
                        `highestUnlockedLevel`, `totalScore`, `bestLevelScore`,
                        `gamesPlayed`, `gamesWon`, `soundEnabled`, `musicEnabled`,
                        `hapticsEnabled`, `equippedBackgroundId`, `equippedCatSkinId`,
                        `equippedBoardThemeId`, `equippedFruitSkinId`
                    FROM `player_profile`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `player_profile`")
                db.execSQL("ALTER TABLE `player_profile_new` RENAME TO `player_profile`")
            }
        }

        internal val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `player_profile` ADD COLUMN `vibrationEnabled` INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        @Volatile
        @androidx.annotation.VisibleForTesting
        internal var forceInMemoryForTest: Boolean = false

        fun getInstance(context: Context): FruitBlissDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: if (forceInMemoryForTest) {
                    Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        FruitBlissDatabase::class.java
                    )
                        .allowMainThreadQueries()
                        .build()
                } else {
                    Room.databaseBuilder(
                        context.applicationContext,
                        FruitBlissDatabase::class.java,
                        "fruit_bliss_db"
                    )
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                            MIGRATION_7_8
                        )
                        .build()
                }.also { INSTANCE = it }
            }
        }

        @androidx.annotation.VisibleForTesting
        internal fun resetInstanceForTest(
            context: Context? = null,
            useInMemory: Boolean = false
        ) {
            synchronized(this) {
                forceInMemoryForTest = useInMemory
                INSTANCE?.close()
                INSTANCE = null
                context?.applicationContext?.deleteDatabase("fruit_bliss_db")
            }
        }
    }
}
