package com.example.model

import androidx.compose.runtime.Immutable

enum class FruitType(val displayName: String) {
    APPLE("Apple"),
    ORANGE("Orange"),
    STRAWBERRY("Strawberry"),
    BANANA("Banana"),
    GRAPE("Grape"),
    BLUEBERRY("Blueberry")
}

enum class SpecialFruitKind {
    NONE,
    STRIPED_ROW,
    STRIPED_COL,
    BLISS_BOMB
}

@Immutable
data class FruitCell(
    val id: Long,
    val fruit: FruitType,
    val special: SpecialFruitKind = SpecialFruitKind.NONE
)

@Immutable
data class CoinPack(
    val id: String,
    val productId: String,
    val coins: Int,
    val title: String,
    val priceText: String,
    val tier: Int
)

enum class BoostType(
    val id: String,
    val productId: String,
    val title: String,
    val priceText: String,
    val description: String,
    val requiresTileTarget: Boolean
) {
    MAGIC_HAMMER(
        id = "magic_hammer",
        productId = "fruit_bliss_boost_magic_hammer",
        title = "Magic Hammer",
        priceText = "$0.10",
        description = "Tap any fruit on the board to smash it instantly.",
        requiresTileTarget = true
    ),
    SHUFFLE(
        id = "shuffle",
        productId = "fruit_bliss_boost_shuffle",
        title = "Shuffle",
        priceText = "$0.50",
        description = "Mixes up all fruits on the board for fresh matches.",
        requiresTileTarget = false
    ),
    EXTRA_MOVE(
        id = "extra_move",
        productId = "fruit_bliss_boost_extra_move",
        title = "Extra Move",
        priceText = "$1.00",
        description = "Adds +5 extra moves to the current game.",
        requiresTileTarget = false
    ),
    TIME_FREEZE(
        id = "time_freeze",
        productId = "fruit_bliss_boost_time_freeze",
        title = "Time Freeze",
        priceText = "$2.00",
        description = "Freezes timer for 15s or shields your next 2 moves.",
        requiresTileTarget = false
    ),
    ROCKET(
        id = "rocket",
        productId = "fruit_bliss_boost_rocket",
        title = "Rocket",
        priceText = "$3.00",
        description = "Tap any fruit to launch a rocket clearing its row and column.",
        requiresTileTarget = true
    ),
    BOMB(
        id = "bomb",
        productId = "fruit_bliss_boost_bomb",
        title = "Bomb",
        priceText = "$4.00",
        description = "Tap any fruit to blast a 3x3 area around it.",
        requiresTileTarget = true
    ),
    SUPER_HINT(
        id = "super_hint",
        productId = "fruit_bliss_boost_super_hint",
        title = "Super Hint",
        priceText = "$5.00",
        description = "Highlights the highest-scoring swap on the board.",
        requiresTileTarget = false
    ),
    MEGA_SHUFFLE(
        id = "mega_shuffle",
        productId = "fruit_bliss_boost_mega_shuffle",
        title = "Mega Shuffle",
        priceText = "$6.00",
        description = "Reshuffles the board and grants +250 bonus points.",
        requiresTileTarget = false
    ),
    DOUBLE_COINS(
        id = "double_coins",
        productId = "fruit_bliss_boost_double_coins",
        title = "Double Coins",
        priceText = "$7.00",
        description = "Doubles all coins earned on level victory.",
        requiresTileTarget = false
    ),
    ADD_TIME(
        id = "add_time",
        productId = "fruit_bliss_boost_add_time",
        title = "Add Time",
        priceText = "$8.00",
        description = "Adds +30s (timed levels) or +4 moves & +400 score.",
        requiresTileTarget = false
    ),
    EXTRA_LIFE(
        id = "extra_life",
        productId = "fruit_bliss_boost_extra_life",
        title = "Extra Life",
        priceText = "$9.00",
        description = "Revives on defeat or grants +6 moves right away.",
        requiresTileTarget = false
    ),
    MEGA_BONUS(
        id = "mega_bonus",
        productId = "fruit_bliss_boost_mega_bonus",
        title = "Mega Bonus",
        priceText = "$10.00",
        description = "Clears center cross, grants 2x score for 3 turns & +800 pts.",
        requiresTileTarget = false
    )
}

@Immutable
data class LevelConfig(
    val levelNumber: Int,
    val title: String,
    val targetScore: Int,
    val maxMoves: Int,
    val timeLimitSeconds: Int?, // null for move-based levels, non-null for timed levels
    val coinReward: Int,
    val fruitVarietyCount: Int = 6
) {
    val twoStarScore: Int get() = (targetScore * 1.35f).toInt()
    val threeStarScore: Int get() = (targetScore * 1.75f).toInt()
}

enum class StoreTab {
    TREASURE_VAULT,
    BOOST_FORGE
}

enum class AppScreen {
    HOME,
    GAME
}

enum class HomeModal {
    NONE,
    STORE,
    THEME_SHOP,
    PROFILE,
    SETTINGS,
    LEVEL_SELECT
}

enum class ThemeCategory(val title: String) {
    BACKGROUND("Background Themes"),
    CAT_SKIN("Cat Skins"),
    BOARD("Board Themes"),
    FRUIT_SKIN("Fruit Skins")
}

enum class BackgroundTheme(
    val id: String,
    val title: String,
    val priceCoins: Int,
    val description: String
) {
    CLASSIC_MEADOW(
        id = "bg_classic_meadow",
        title = "Classic Meadow",
        priceCoins = 0,
        description = "Sunny village meadow with rolling green hills."
    ),
    CANDY_WORLD(
        id = "bg_candy_world",
        title = "Candy World",
        priceCoins = 500,
        description = "Sweet pastel skies, lollipops & frosted hills."
    ),
    JUNGLE_WORLD(
        id = "bg_jungle_world",
        title = "Jungle World",
        priceCoins = 800,
        description = "Lush emerald canopy, palms & sunlit vines."
    ),
    SNOW_WORLD(
        id = "bg_snow_world",
        title = "Snow World",
        priceCoins = 1000,
        description = "Frosted pine peaks & drifting winter snowflakes."
    ),
    GALAXY_WORLD(
        id = "bg_galaxy_world",
        title = "Galaxy World",
        priceCoins = 1500,
        description = "Cosmic nebula sky, glowing planets & starlight."
    ),
    OCEAN_WORLD(
        id = "bg_ocean_world",
        title = "Ocean World",
        priceCoins = 2000,
        description = "Crystal turquoise sea, coral reefs & bubbles."
    ),
    FANTASY_WORLD(
        id = "bg_fantasy_world",
        title = "Fantasy World",
        priceCoins = 2500,
        description = "Enchanted aurora clouds & crystal islands."
    );

    companion object {
        fun fromId(id: String): BackgroundTheme =
            entries.firstOrNull { it.id == id } ?: CLASSIC_MEADOW
    }
}

enum class CatSkin(
    val id: String,
    val title: String,
    val priceCoins: Int,
    val description: String
) {
    CLASSIC(
        id = "cat_classic",
        title = "Classic Cat",
        priceCoins = 0,
        description = "Sunny orange tabby with a lucky red bandana."
    ),
    ROYAL(
        id = "cat_royal",
        title = "Royal Cat",
        priceCoins = 500,
        description = "Regal tabby wearing a jeweled crown & cape."
    ),
    NINJA(
        id = "cat_ninja",
        title = "Ninja Cat",
        priceCoins = 800,
        description = "Stealthy midnight tabby with a crimson headband."
    ),
    SPACE(
        id = "cat_space",
        title = "Space Cat",
        priceCoins = 1200,
        description = "Cosmic explorer with a glowing astro helmet."
    ),
    GOLDEN(
        id = "cat_golden",
        title = "Golden Cat",
        priceCoins = 2000,
        description = "Radiant 24K golden tabby with diamond shine."
    );

    companion object {
        fun fromId(id: String): CatSkin =
            entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

enum class BoardTheme(
    val id: String,
    val title: String,
    val priceCoins: Int,
    val description: String
) {
    CLASSIC(
        id = "board_classic",
        title = "Classic Board",
        priceCoins = 0,
        description = "Warm carved wood frame with royal plum grid."
    ),
    CANDY(
        id = "board_candy",
        title = "Candy Board",
        priceCoins = 400,
        description = "Strawberry frosting frame & wafer-pink tiles."
    ),
    NEON(
        id = "board_neon",
        title = "Neon Board",
        priceCoins = 700,
        description = "Electric cyan & magenta arcade glow border."
    ),
    GALAXY(
        id = "board_galaxy",
        title = "Galaxy Board",
        priceCoins = 1000,
        description = "Deep cosmic nebula frame with twinkling stars."
    );

    companion object {
        fun fromId(id: String): BoardTheme =
            entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

enum class FruitSkin(
    val id: String,
    val title: String,
    val priceCoins: Int,
    val description: String
) {
    CLASSIC(
        id = "fruit_classic",
        title = "Classic Fruits",
        priceCoins = 0,
        description = "Juicy glossy orchard fruits with bright sheen."
    ),
    CARTOON(
        id = "fruit_cartoon",
        title = "Cartoon Fruits",
        priceCoins = 400,
        description = "Bold comic outlines with cheerful kawaii faces."
    ),
    CRYSTAL(
        id = "fruit_crystal",
        title = "Crystal Fruits",
        priceCoins = 800,
        description = "Faceted gem-cut fruits that gleam like jewels."
    ),
    GOLDEN(
        id = "fruit_golden",
        title = "Golden Fruits",
        priceCoins = 1200,
        description = "Luxurious 24K gold-dipped treasure fruits."
    );

    companion object {
        fun fromId(id: String): FruitSkin =
            entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

@Immutable
data class ThemeShopItem(
    val id: String,
    val category: ThemeCategory,
    val title: String,
    val priceCoins: Int,
    val description: String
)

object ThemeShopCatalog {
    val backgroundThemes: List<ThemeShopItem> = listOf(
        BackgroundTheme.CANDY_WORLD,
        BackgroundTheme.JUNGLE_WORLD,
        BackgroundTheme.SNOW_WORLD,
        BackgroundTheme.GALAXY_WORLD,
        BackgroundTheme.OCEAN_WORLD,
        BackgroundTheme.FANTASY_WORLD
    ).map {
        ThemeShopItem(
            id = it.id,
            category = ThemeCategory.BACKGROUND,
            title = it.title,
            priceCoins = it.priceCoins,
            description = it.description
        )
    }

    val catSkins: List<ThemeShopItem> = CatSkin.entries.map {
        ThemeShopItem(
            id = it.id,
            category = ThemeCategory.CAT_SKIN,
            title = it.title,
            priceCoins = it.priceCoins,
            description = it.description
        )
    }

    val boardThemes: List<ThemeShopItem> = BoardTheme.entries.map {
        ThemeShopItem(
            id = it.id,
            category = ThemeCategory.BOARD,
            title = it.title,
            priceCoins = it.priceCoins,
            description = it.description
        )
    }

    val fruitSkins: List<ThemeShopItem> = FruitSkin.entries.map {
        ThemeShopItem(
            id = it.id,
            category = ThemeCategory.FRUIT_SKIN,
            title = it.title,
            priceCoins = it.priceCoins,
            description = it.description
        )
    }

    val allItems: List<ThemeShopItem> =
        backgroundThemes + catSkins + boardThemes + fruitSkins

    val freeStarterIds: Set<String> = setOf(
        BackgroundTheme.CLASSIC_MEADOW.id,
        CatSkin.CLASSIC.id,
        BoardTheme.CLASSIC.id,
        FruitSkin.CLASSIC.id
    )

    fun itemsForCategory(category: ThemeCategory): List<ThemeShopItem> = when (category) {
        ThemeCategory.BACKGROUND -> backgroundThemes
        ThemeCategory.CAT_SKIN -> catSkins
        ThemeCategory.BOARD -> boardThemes
        ThemeCategory.FRUIT_SKIN -> fruitSkins
    }

    fun findById(id: String): ThemeShopItem? = allItems.firstOrNull { it.id == id }
}

sealed interface StoreProduct {
    val productId: String
    val displayTitle: String
    val fallbackPriceText: String

    data class CoinPackProduct(val pack: CoinPack) : StoreProduct {
        override val productId: String get() = pack.productId
        override val displayTitle: String get() = pack.title
        override val fallbackPriceText: String get() = pack.priceText
    }

    data class BoostProduct(val boost: BoostType) : StoreProduct {
        override val productId: String get() = boost.productId
        override val displayTitle: String get() = boost.title
        override val fallbackPriceText: String get() = boost.priceText
    }
}

object StoreCatalog {
    val coinPacks: List<CoinPack> = listOf(
        CoinPack("pack_10", "fruit_bliss_coins_10", 10, "10 Coins", "$0.10", 1),
        CoinPack("pack_50", "fruit_bliss_coins_50", 50, "50 Coins", "$0.51", 2),
        CoinPack("pack_100", "fruit_bliss_coins_100", 100, "100 Coins", "$1.01", 3),
        CoinPack("pack_200", "fruit_bliss_coins_200", 200, "200 Coins", "$2.01", 4),
        CoinPack("pack_500", "fruit_bliss_coins_500", 500, "500 Coins", "$4.01", 5),
        CoinPack("pack_1000", "fruit_bliss_coins_1000", 1000, "1,000 Coins", "$7.01", 6),
        CoinPack("pack_1500", "fruit_bliss_coins_1500", 1500, "1,500 Coins", "$10.01", 7),
        CoinPack("pack_2500", "fruit_bliss_coins_2500", 2500, "2,500 Coins", "$20.01", 8),
        CoinPack("pack_5000", "fruit_bliss_coins_5000", 5000, "5,000 Coins", "$50.01", 9),
        CoinPack("pack_10000", "fruit_bliss_coins_10000", 10000, "10,000 Coins", "$100.01", 10)
    )

    // Ordered exactly Row 1 (1..4), Row 2 (5..8), Row 3 (9..12)
    val boostItems: List<BoostType> = BoostType.entries.toList()

    val allPlayProductIds: List<String> by lazy {
        coinPacks.map { it.productId } + boostItems.map { it.productId }
    }

    private val productsByPlayId: Map<String, StoreProduct> by lazy {
        buildMap {
            coinPacks.forEach { put(it.productId, StoreProduct.CoinPackProduct(it)) }
            boostItems.forEach { put(it.productId, StoreProduct.BoostProduct(it)) }
        }
    }

    fun findByProductId(productId: String): StoreProduct? = productsByPlayId[productId]

    val levels: List<LevelConfig> = (1..20).map { lvl ->
        val isTimed = lvl % 4 == 0
        LevelConfig(
            levelNumber = lvl,
            title = when {
                lvl == 1 -> "Sunny Orchard"
                lvl == 2 -> "Berry Basket"
                lvl == 3 -> "Citrus Burst"
                lvl % 4 == 0 -> "Fruit Rush!"
                lvl <= 8 -> "Meadow Path"
                lvl <= 12 -> "Golden Harvest"
                lvl <= 16 -> "Blissful Valley"
                else -> "Royal Garden"
            },
            targetScore = 700 + (lvl - 1) * 420,
            maxMoves = if (isTimed) 40 else (18 + (lvl % 5) * 2).coerceAtMost(26),
            timeLimitSeconds = if (isTimed) 75 + (lvl / 4) * 10 else null,
            coinReward = 40 + lvl * 15,
            fruitVarietyCount = if (lvl <= 2) 5 else 6
        )
    }

    fun getLevelConfig(levelNumber: Int): LevelConfig {
        return levels.getOrNull(levelNumber - 1) ?: LevelConfig(
            levelNumber = levelNumber,
            title = "Endless Bliss $levelNumber",
            targetScore = 700 + (levelNumber - 1) * 450,
            maxMoves = if (levelNumber % 4 == 0) 40 else 22,
            timeLimitSeconds = if (levelNumber % 4 == 0) 90 else null,
            coinReward = 60 + levelNumber * 15,
            fruitVarietyCount = 6
        )
    }
}
