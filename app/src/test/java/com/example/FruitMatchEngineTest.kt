package com.example

import com.example.game.FruitMatchEngine
import com.example.game.GridPos
import com.example.model.BoostType
import com.example.model.FruitCell
import com.example.model.FruitType
import com.example.model.SpecialFruitKind
import com.example.model.StoreCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FruitMatchEngineTest {

    @Test
    fun initialBoardIs8x8WithNoPreExistingMatchesAndHasValidMove() {
        val board = FruitMatchEngine.createInitialBoard(varietyCount = 6, random = Random(42))
        assertEquals(8, board.size)
        assertTrue(board.all { it.size == 8 })
        val initialMatches = FruitMatchEngine.findMatches(board)
        assertTrue(initialMatches.matchedPositions.isEmpty())
        assertNotNull(FruitMatchEngine.findBestValidMove(board))
    }

    @Test
    fun findsHorizontalMatchAndSpawnsSpecialOn4Match() {
        var id = 100L
        val grid = List(8) { r ->
            List(8) { c ->
                val fruit = if (r == 0 && c in 0..3) {
                    FruitType.STRAWBERRY
                } else {
                    FruitType.entries[(r * 3 + c) % 4].let {
                        if (it == FruitType.STRAWBERRY) FruitType.APPLE else it
                    }
                }
                FruitCell(id = id++, fruit = fruit)
            }
        }
        val scan = FruitMatchEngine.findMatches(grid)
        assertEquals(4, scan.matchedPositions.size)
        assertEquals(1, scan.groups.size)
        assertEquals(SpecialFruitKind.STRIPED_COL, scan.groups.first().specialToSpawn)
    }

    @Test
    fun collapseAndRefillRestoresFull8x8Grid() {
        val board = FruitMatchEngine.createInitialBoard(6, Random(7))
        val cleared = setOf(GridPos(3, 2), GridPos(3, 3), GridPos(3, 4))
        val refilled = FruitMatchEngine.collapseAndRefill(board, cleared, varietyCount = 6, random = Random(7))
        assertEquals(8, refilled.size)
        assertTrue(refilled.all { it.size == 8 })
    }

    @Test
    fun storeCatalogPreservesExactCoinPackAndBoostPrices() {
        val expectedPacks = listOf(
            10 to "$0.10",
            50 to "$0.51",
            100 to "$1.01",
            200 to "$2.01",
            500 to "$4.01",
            1000 to "$7.01",
            1500 to "$10.01",
            2500 to "$20.01",
            5000 to "$50.01",
            10000 to "$100.01"
        )
        assertEquals(10, StoreCatalog.coinPacks.size)
        expectedPacks.forEachIndexed { index, (coins, price) ->
            assertEquals(coins, StoreCatalog.coinPacks[index].coins)
            assertEquals(price, StoreCatalog.coinPacks[index].priceText)
        }

        val expectedBoosts = listOf(
            "Magic Hammer" to "$0.10",
            "Shuffle" to "$0.50",
            "Extra Move" to "$1.00",
            "Time Freeze" to "$2.00",
            "Rocket" to "$3.00",
            "Bomb" to "$4.00",
            "Super Hint" to "$5.00",
            "Mega Shuffle" to "$6.00",
            "Double Coins" to "$7.00",
            "Add Time" to "$8.00",
            "Extra Life" to "$9.00",
            "Mega Bonus" to "$10.00"
        )
        assertEquals(12, BoostType.entries.size)
        expectedBoosts.forEachIndexed { index, (title, price) ->
            assertEquals(title, StoreCatalog.boostItems[index].title)
            assertEquals(price, StoreCatalog.boostItems[index].priceText)
        }
    }
}
