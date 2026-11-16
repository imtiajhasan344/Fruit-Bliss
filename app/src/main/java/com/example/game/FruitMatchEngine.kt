package com.example.game

import com.example.model.FruitCell
import com.example.model.FruitType
import com.example.model.SpecialFruitKind
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

data class GridPos(val row: Int, val col: Int)

data class MatchGroup(
    val cells: Set<GridPos>,
    val spawnPos: GridPos,
    val specialToSpawn: SpecialFruitKind
)

data class MatchScanResult(
    val matchedPositions: Set<GridPos>,
    val groups: List<MatchGroup>
)

object FruitMatchEngine {
    const val BOARD_SIZE = 8
    private val nextCellId = AtomicLong(1L)

    private fun allocateId(): Long = nextCellId.getAndIncrement()

    fun createInitialBoard(
        varietyCount: Int = 6,
        random: Random = Random.Default
    ): List<List<FruitCell>> {
        val types = FruitType.entries.take(varietyCount.coerceIn(4, FruitType.entries.size))
        val grid = MutableList(BOARD_SIZE) { row ->
            MutableList(BOARD_SIZE) { col ->
                FruitCell(id = allocateId(), fruit = types[random.nextInt(types.size)])
            }
        }

        // Remove any initial 3-in-a-row
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                var candidate = grid[r][c].fruit
                var attempts = 0
                while (attempts < 12 && wouldFormInitialMatch(grid, r, c, candidate)) {
                    candidate = types[random.nextInt(types.size)]
                    attempts++
                }
                if (wouldFormInitialMatch(grid, r, c, candidate)) {
                    val safeTypes = types.filter { !wouldFormInitialMatch(grid, r, c, it) }
                    if (safeTypes.isNotEmpty()) {
                        candidate = safeTypes[random.nextInt(safeTypes.size)]
                    }
                }
                grid[r][c] = grid[r][c].copy(fruit = candidate)
            }
        }

        var board = grid.map { it.toList() }
        if (findBestValidMove(board) == null) {
            board = shuffleBoard(board, varietyCount, random)
        }
        return board
    }

    private fun wouldFormInitialMatch(
        grid: List<List<FruitCell>>,
        r: Int,
        c: Int,
        fruit: FruitType
    ): Boolean {
        val horiz = c >= 2 && grid[r][c - 1].fruit == fruit && grid[r][c - 2].fruit == fruit
        val vert = r >= 2 && grid[r - 1][c].fruit == fruit && grid[r - 2][c].fruit == fruit
        return horiz || vert
    }

    fun areAdjacent(a: GridPos, b: GridPos): Boolean {
        val dr = kotlin.math.abs(a.row - b.row)
        val dc = kotlin.math.abs(a.col - b.col)
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1)
    }

    fun swapCells(board: List<List<FruitCell>>, a: GridPos, b: GridPos): List<List<FruitCell>> {
        val mutable = board.map { it.toMutableList() }
        val temp = mutable[a.row][a.col]
        mutable[a.row][a.col] = mutable[b.row][b.col]
        mutable[b.row][b.col] = temp
        return mutable.map { it.toList() }
    }

    fun findMatches(board: List<List<FruitCell?>>): MatchScanResult {
        val horizontalRuns = mutableListOf<Set<GridPos>>()
        val verticalRuns = mutableListOf<Set<GridPos>>()

        // Horizontal runs
        for (r in 0 until BOARD_SIZE) {
            var c = 0
            while (c < BOARD_SIZE) {
                val cell = board[r][c]
                if (cell == null) {
                    c++
                    continue
                }
                var end = c + 1
                while (end < BOARD_SIZE && board[r][end]?.fruit == cell.fruit) {
                    end++
                }
                if (end - c >= 3) {
                    horizontalRuns.add((c until end).map { GridPos(r, it) }.toSet())
                }
                c = end
            }
        }

        // Vertical runs
        for (c in 0 until BOARD_SIZE) {
            var r = 0
            while (r < BOARD_SIZE) {
                val cell = board[r][c]
                if (cell == null) {
                    r++
                    continue
                }
                var end = r + 1
                while (end < BOARD_SIZE && board[end][c]?.fruit == cell.fruit) {
                    end++
                }
                if (end - r >= 3) {
                    verticalRuns.add((r until end).map { GridPos(it, c) }.toSet())
                }
                r = end
            }
        }

        val allRuns = horizontalRuns + verticalRuns
        val groups = mutableListOf<MatchGroup>()
        val usedRunIndices = mutableSetOf<Int>()

        for (i in allRuns.indices) {
            if (i in usedRunIndices) continue
            val combined = allRuns[i].toMutableSet()
            var isHoriz = i < horizontalRuns.size
            var hasIntersection = false
            usedRunIndices.add(i)

            var added: Boolean
            do {
                added = false
                for (j in allRuns.indices) {
                    if (j !in usedRunIndices && allRuns[j].any { it in combined }) {
                        combined.addAll(allRuns[j])
                        usedRunIndices.add(j)
                        hasIntersection = true
                        added = true
                    }
                }
            } while (added)

            val special = when {
                combined.size >= 5 || hasIntersection -> SpecialFruitKind.BLISS_BOMB
                combined.size == 4 -> if (isHoriz) SpecialFruitKind.STRIPED_COL else SpecialFruitKind.STRIPED_ROW
                else -> SpecialFruitKind.NONE
            }
            groups.add(
                MatchGroup(
                    cells = combined,
                    spawnPos = combined.first(),
                    specialToSpawn = special
                )
            )
        }

        val matched = groups.flatMap { it.cells }.toSet()
        return MatchScanResult(matchedPositions = matched, groups = groups)
    }

    fun expandSpecialEffects(
        board: List<List<FruitCell?>>,
        initialMatched: Set<GridPos>
    ): Set<GridPos> {
        val result = initialMatched.toMutableSet()
        val queue = ArrayDeque(initialMatched)
        val processed = mutableSetOf<GridPos>()

        while (queue.isNotEmpty()) {
            val pos = queue.removeFirst()
            if (!processed.add(pos)) continue
            val cell = board.getOrNull(pos.row)?.getOrNull(pos.col) ?: continue
            when (cell.special) {
                SpecialFruitKind.STRIPED_ROW -> {
                    for (c in 0 until BOARD_SIZE) {
                        val p = GridPos(pos.row, c)
                        if (result.add(p)) queue.add(p)
                    }
                }
                SpecialFruitKind.STRIPED_COL -> {
                    for (r in 0 until BOARD_SIZE) {
                        val p = GridPos(r, pos.col)
                        if (result.add(p)) queue.add(p)
                    }
                }
                SpecialFruitKind.BLISS_BOMB -> {
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = pos.row + dr
                            val nc = pos.col + dc
                            if (nr in 0 until BOARD_SIZE && nc in 0 until BOARD_SIZE) {
                                val p = GridPos(nr, nc)
                                if (result.add(p)) queue.add(p)
                            }
                        }
                    }
                }
                SpecialFruitKind.NONE -> Unit
            }
        }
        return result
    }

    fun collapseAndRefill(
        board: List<List<FruitCell>>,
        cleared: Set<GridPos>,
        groups: List<MatchGroup> = emptyList(),
        varietyCount: Int = 6,
        random: Random = Random.Default
    ): List<List<FruitCell>> {
        val types = FruitType.entries.take(varietyCount.coerceIn(4, FruitType.entries.size))
        val mutable: MutableList<MutableList<FruitCell?>> =
            board.map { row -> row.map<FruitCell, FruitCell?> { it }.toMutableList() }.toMutableList()

        // Clear matched cells
        for (pos in cleared) {
            mutable[pos.row][pos.col] = null
        }

        // Spawn special fruits where 4+ or intersection match occurred
        for (group in groups) {
            if (group.specialToSpawn != SpecialFruitKind.NONE) {
                val p = group.spawnPos
                val originalFruit = board[p.row][p.col].fruit
                mutable[p.row][p.col] = FruitCell(
                    id = allocateId(),
                    fruit = originalFruit,
                    special = group.specialToSpawn
                )
            }
        }

        // Collapse downward per column
        for (c in 0 until BOARD_SIZE) {
            var writeRow = BOARD_SIZE - 1
            for (r in BOARD_SIZE - 1 downTo 0) {
                val cell = mutable[r][c]
                if (cell != null) {
                    mutable[writeRow][c] = cell
                    if (writeRow != r) {
                        mutable[r][c] = null
                    }
                    writeRow--
                }
            }
            for (r in writeRow downTo 0) {
                mutable[r][c] = FruitCell(
                    id = allocateId(),
                    fruit = types[random.nextInt(types.size)]
                )
            }
        }

        return mutable.map { row -> row.map { it!! } }
    }

    fun findBestValidMove(board: List<List<FruitCell>>): Pair<GridPos, GridPos>? {
        var bestMove: Pair<GridPos, GridPos>? = null
        var bestCount = 0

        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val pos = GridPos(r, c)
                val neighbors = listOfNotNull(
                    if (c + 1 < BOARD_SIZE) GridPos(r, c + 1) else null,
                    if (r + 1 < BOARD_SIZE) GridPos(r + 1, c) else null
                )
                for (neighbor in neighbors) {
                    if (board[pos.row][pos.col].fruit == board[neighbor.row][neighbor.col].fruit) continue
                    val swapped = swapCells(board, pos, neighbor)
                    val matches = findMatches(swapped)
                    if (matches.matchedPositions.size > bestCount) {
                        bestCount = matches.matchedPositions.size
                        bestMove = pos to neighbor
                    }
                }
            }
        }
        return bestMove
    }

    fun shuffleBoard(
        board: List<List<FruitCell>>,
        varietyCount: Int = 6,
        random: Random = Random.Default
    ): List<List<FruitCell>> {
        val flat = board.flatten().toMutableList()
        repeat(15) {
            flat.shuffle(random)
            val candidate = List(BOARD_SIZE) { r ->
                List(BOARD_SIZE) { c ->
                    flat[r * BOARD_SIZE + c].copy(id = allocateId())
                }
            }
            if (findMatches(candidate).matchedPositions.isEmpty() &&
                findBestValidMove(candidate) != null
            ) {
                return candidate
            }
        }
        return createInitialBoard(varietyCount, random)
    }
}
