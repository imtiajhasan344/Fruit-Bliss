package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.FruitMatchEngine
import com.example.game.GridPos
import com.example.model.BoardTheme
import com.example.model.BoostType
import com.example.model.CatSkin
import com.example.model.FruitCell
import com.example.model.FruitSkin
import com.example.model.StoreCatalog
import com.example.ui.components.BoostIconArtwork
import com.example.ui.components.CartoonCatCanvas
import com.example.ui.components.CoinCurrencyPill
import com.example.ui.components.FruitTileArtwork
import com.example.ui.components.GlossyMenuButton
import com.example.ui.components.GoldCoinIcon
import com.example.ui.components.GoldStarIcon
import com.example.ui.components.OutlinedCartoonText
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnRim
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.CardCream
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GreenBottom
import com.example.ui.theme.GreenRim
import com.example.ui.theme.GreenTop
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.OrangeBottom
import com.example.ui.theme.OrangeRim
import com.example.ui.theme.OrangeTop
import com.example.ui.theme.PurpleBtnBottom
import com.example.ui.theme.PurpleBtnRim
import com.example.ui.theme.PurpleBtnTop
import com.example.ui.theme.StorePurpleDark
import com.example.ui.theme.StorePurpleDeep
import com.example.ui.theme.TextBrownDark
import com.example.ui.theme.WoodDark
import com.example.ui.theme.WoodLight
import com.example.viewmodel.GameStatus
import com.example.viewmodel.GameUiState
import kotlin.math.abs

@Composable
fun GameScreen(
    gameState: GameUiState,
    coins: Int,
    boostInventory: Map<BoostType, Int>,
    hapticsEnabled: Boolean,
    hasActiveModal: Boolean = false,
    boardTheme: BoardTheme = BoardTheme.CLASSIC,
    fruitSkin: FruitSkin = FruitSkin.CLASSIC,
    catSkin: CatSkin = CatSkin.CLASSIC,
    onTileTapped: (GridPos) -> Unit,
    onTileSwiped: (GridPos, Int, Int) -> Unit,
    onActivateBoost: (BoostType) -> Unit,
    onReviveExtraLife: () -> Unit,
    onRetryLevel: () -> Unit,
    onNextLevel: () -> Unit,
    onOpenBoostStore: () -> Unit,
    onPauseGame: () -> Unit = {},
    onResumeGame: () -> Unit = {},
    onRequestRestart: () -> Unit = {},
    onConfirmRestart: () -> Unit = {},
    onDismissRestartConfirm: () -> Unit = {},
    onExitHome: () -> Unit
) {
    BackHandler(enabled = !hasActiveModal) {
        when {
            gameState.showRestartConfirmation -> onDismissRestartConfirm()
            gameState.isPaused -> onResumeGame()
            gameState.status == GameStatus.WON || gameState.status == GameStatus.LOST -> onExitHome()
            else -> onPauseGame()
        }
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(gameState.hapticTrigger) {
        if (hapticsEnabled && gameState.hapticTrigger > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val isBoardInteractive = !gameState.isPaused && !hasActiveModal && gameState.status == GameStatus.PLAYING
    val hintPulse = if (gameState.hintedPair != null) {
        val hintPulseTransition = rememberInfiniteTransition(label = "hint")
        hintPulseTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
            label = "hint_pulse"
        ).value
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Game Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (gameState.status == GameStatus.WON || gameState.status == GameStatus.LOST) {
                            onExitHome()
                        } else {
                            onPauseGame()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xBB3E1F0D))
                        .border(2.dp, Color(0xFFFFCA28), CircleShape)
                        .testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = if (gameState.status == GameStatus.WON || gameState.status == GameStatus.LOST) {
                            "Back to Home"
                        } else {
                            "Pause Game"
                        },
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Level ${gameState.level.levelNumber}",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xAA000000),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = gameState.level.title,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFFF8E1)
                    )
                }

                CoinCurrencyPill(
                    amount = coins,
                    onAddClick = {
                        if (isBoardInteractive) {
                            onOpenBoostStore()
                        }
                    }
                )
            }

            Spacer(Modifier.height(6.dp))

            // 2. Score Target & Moves/Timer HUD Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(StorePurpleDeep, StorePurpleDark)))
                    .border(2.dp, GoldBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Score Progress
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Score: ${gameState.score}",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            modifier = Modifier.testTag("game_score_text")
                        )
                        Text(
                            text = "Goal: ${gameState.level.targetScore}",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFE082)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    val progress = (gameState.score.toFloat() / gameState.level.targetScore.toFloat())
                        .coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF1A062B))
                            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4ADE80), Color(0xFFFACC15))
                                    )
                                )
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Moves Left Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF6B1FA2))
                        .border(1.5.dp, Color(0xFFFFCA28), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (gameState.shieldMovesLeft > 0) "SHIELD" else "MOVES",
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color(0xFFFFE082)
                    )
                    Text(
                        text = gameState.movesLeft.toString(),
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.testTag("game_moves_text")
                    )
                }

                // Timer Pill if timed level
                if (gameState.timeLeftSeconds != null) {
                    Spacer(Modifier.width(6.dp))
                    val frozen = gameState.freezeSecondsLeft > 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (frozen) Color(0xFF0284C7) else Color(0xFF9F1239))
                            .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (frozen) "FROZEN" else "TIME",
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${gameState.timeLeftSeconds}s",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.testTag("game_timer_text")
                        )
                    }
                }
            }

            // Status Banner / Active Buff Badges
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val bannerText = buildString {
                    gameState.statusBanner?.let { append(it) }
                    if (gameState.doubleCoinsActive) {
                        if (isNotEmpty()) append("  •  ")
                        append("2x Coins")
                    }
                    if (gameState.scoreMultiplierTurns > 0) {
                        if (isNotEmpty()) append("  •  ")
                        append("2x Score (${gameState.scoreMultiplierTurns})")
                    }
                }
                if (bannerText.isNotEmpty()) {
                    Text(
                        text = bannerText,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xAA47126B))
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. 8x8 Fruit Match Board
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val boardSizeDp = minOf(maxWidth, maxHeight)
                val frameGradient = when (boardTheme) {
                    BoardTheme.CLASSIC -> listOf(WoodLight, WoodDark)
                    BoardTheme.CANDY -> listOf(Color(0xFFFF85C0), Color(0xFFDB2777))
                    BoardTheme.NEON -> listOf(Color(0xFF06B6D4), Color(0xFF9333EA))
                    BoardTheme.GALAXY -> listOf(Color(0xFF312E81), Color(0xFF0F172A))
                }
                val frameBorderColor = when (boardTheme) {
                    BoardTheme.CLASSIC -> GoldBorder
                    BoardTheme.CANDY -> Color(0xFFFFE4E6)
                    BoardTheme.NEON -> Color(0xFF22D3EE)
                    BoardTheme.GALAXY -> Color(0xFFA78BFA)
                }
                val innerBoardBg = when (boardTheme) {
                    BoardTheme.CLASSIC -> Color(0xCC2C0B48)
                    BoardTheme.CANDY -> Color(0xDD831843)
                    BoardTheme.NEON -> Color(0xEE090D16)
                    BoardTheme.GALAXY -> Color(0xEE140736)
                }
                Box(
                    modifier = Modifier
                        .size(boardSizeDp)
                        .aspectRatio(1f)
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(frameGradient))
                        .border(3.dp, frameBorderColor, RoundedCornerShape(20.dp))
                        .padding(6.dp)
                        .testTag("fruit_match_board")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(innerBoardBg)
                    ) {
                        for (r in 0 until FruitMatchEngine.BOARD_SIZE) {
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                for (c in 0 until FruitMatchEngine.BOARD_SIZE) {
                                    val pos = GridPos(r, c)
                                    val cell = gameState.board.getOrNull(r)?.getOrNull(c)
                                    val isSelected = gameState.selectedCell == pos
                                    val isPopping = pos in gameState.poppingCells
                                    val isHinted = gameState.hintedPair?.let {
                                        it.first == pos || it.second == pos
                                    } == true
                                    val isTargetMode = gameState.activeTargetingBoost != null

                                    BoardFruitCell(
                                        pos = pos,
                                        cell = cell,
                                        boardTheme = boardTheme,
                                        fruitSkin = fruitSkin,
                                        isSelected = isSelected,
                                        isPopping = isPopping,
                                        isHinted = isHinted,
                                        hintPulse = hintPulse,
                                        isTargetMode = isTargetMode,
                                        interactionEnabled = isBoardInteractive,
                                        onTap = { onTileTapped(pos) },
                                        onSwipe = { dR, dC -> onTileSwiped(pos, dR, dC) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                            }
                        }
                    }

                    // Floating Score Popup overlay
                    val popup = gameState.floatingPopup
                    if (popup != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xEEFF9F1C))
                                .border(2.dp, Color.White, RoundedCornerShape(50))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = popup.text,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // 4. Functional In-Game Boost Forge Dock (all 12 boosts accessible!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(StorePurpleDeep, StorePurpleDark)))
                    .border(2.dp, GoldBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Boost Forge",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFFE082)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF7B2CBF))
                            .clickable(enabled = isBoardInteractive, onClick = onOpenBoostStore)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingBag,
                            contentDescription = "Buy Boosts",
                            tint = GoldBright,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Get Boosts",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("game_boost_dock")
                ) {
                    items(StoreCatalog.boostItems, key = { it.id }) { boost ->
                        val qty = boostInventory[boost] ?: 0
                        val active = gameState.activeTargetingBoost == boost
                        GameBoostChip(
                            boost = boost,
                            quantity = qty,
                            isActive = active,
                            enabled = isBoardInteractive,
                            onClick = { onActivateBoost(boost) }
                        )
                    }
                }
            }
        }

        // Pause Menu Overlay (Resume, Restart, Back to Home)
        AnimatedVisibility(
            visible = gameState.isPaused &&
                !gameState.showRestartConfirmation &&
                gameState.status != GameStatus.WON &&
                gameState.status != GameStatus.LOST,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut()
        ) {
            GamePauseMenuOverlay(
                levelNumber = gameState.level.levelNumber,
                levelTitle = gameState.level.title,
                onResume = onResumeGame,
                onRestartClick = onRequestRestart,
                onBackToHome = onExitHome
            )
        }

        // Restart Confirmation Overlay
        AnimatedVisibility(
            visible = gameState.isPaused &&
                gameState.showRestartConfirmation &&
                gameState.status != GameStatus.WON &&
                gameState.status != GameStatus.LOST,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut()
        ) {
            RestartConfirmationOverlay(
                levelNumber = gameState.level.levelNumber,
                onConfirmRestart = onConfirmRestart,
                onCancel = onDismissRestartConfirm
            )
        }

        // Win / Lose Overlays
        AnimatedVisibility(
            visible = gameState.status == GameStatus.WON,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut()
        ) {
            LevelWonOverlay(
                gameState = gameState,
                catSkin = catSkin,
                onNextLevel = onNextLevel,
                onRetry = onRetryLevel,
                onHome = onExitHome
            )
        }

        AnimatedVisibility(
            visible = gameState.status == GameStatus.LOST,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut()
        ) {
            LevelLostOverlay(
                gameState = gameState,
                extraLifeOwned = boostInventory[BoostType.EXTRA_LIFE] ?: 0,
                coins = coins,
                onRevive = onReviveExtraLife,
                onRetry = onRetryLevel,
                onHome = onExitHome
            )
        }
    }
}

@Composable
private fun BoardFruitCell(
    pos: GridPos,
    cell: FruitCell?,
    boardTheme: BoardTheme,
    fruitSkin: FruitSkin,
    isSelected: Boolean,
    isPopping: Boolean,
    isHinted: Boolean,
    hintPulse: Float,
    isTargetMode: Boolean,
    interactionEnabled: Boolean,
    onTap: () -> Unit,
    onSwipe: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isPopping -> 0.05f
            isSelected -> 1.15f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "tile_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPopping) 0f else 1f,
        animationSpec = tween(180),
        label = "tile_alpha"
    )

    val lightChecker = when (boardTheme) {
        BoardTheme.CLASSIC -> Color(0x28FFF8E7)
        BoardTheme.CANDY -> Color(0x38FBCFE8)
        BoardTheme.NEON -> Color(0x2A22D3EE)
        BoardTheme.GALAXY -> Color(0x2A818CF8)
    }
    val darkChecker = when (boardTheme) {
        BoardTheme.CLASSIC -> Color(0x14000000)
        BoardTheme.CANDY -> Color(0x22BE185D)
        BoardTheme.NEON -> Color(0x2AD946EF)
        BoardTheme.GALAXY -> Color(0x330F0524)
    }
    val checkerboardColor = if ((pos.row + pos.col) % 2 == 0) {
        lightChecker
    } else {
        darkChecker
    }

    var dragDx by remember(pos) { mutableFloatStateOf(0f) }
    var dragDy by remember(pos) { mutableFloatStateOf(0f) }
    var triggeredSwipe by remember(pos) { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Color(0x66FFD60A)
                    isHinted -> Color(0x5538BDF8)
                    isTargetMode -> Color(0x33FF5A5F)
                    else -> checkerboardColor
                }
            )
            .then(
                if (isSelected || isHinted) {
                    Modifier.border(2.dp, GoldBright, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .testTag("board_cell_${pos.row}_${pos.col}")
            .clickable(enabled = interactionEnabled, onClick = onTap)
            .pointerInput(cell?.id, isTargetMode, interactionEnabled) {
                if (isTargetMode || !interactionEnabled) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        dragDx = 0f
                        dragDy = 0f
                        triggeredSwipe = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (triggeredSwipe > 0f) return@detectDragGestures
                        dragDx += dragAmount.x
                        dragDy += dragAmount.y
                        val threshold = 26f
                        if (abs(dragDx) > threshold || abs(dragDy) > threshold) {
                            triggeredSwipe = 1f
                            if (abs(dragDx) > abs(dragDy)) {
                                onSwipe(0, if (dragDx > 0) 1 else -1)
                            } else {
                                onSwipe(if (dragDy > 0) 1 else -1, 0)
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (cell != null) {
            FruitTileArtwork(
                fruit = cell.fruit,
                special = cell.special,
                skin = fruitSkin,
                modifier = Modifier
                    .fillMaxSize(0.86f)
                    .graphicsLayer {
                        val finalScale = scale * if (isHinted) hintPulse else 1f
                        scaleX = finalScale
                        scaleY = finalScale
                        this.alpha = alpha
                    }
            )
        }
    }
}

@Composable
private fun GameBoostChip(
    boost: BoostType,
    quantity: Int,
    isActive: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .defaultMinSize(minHeight = 58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isActive -> Brush.verticalGradient(listOf(OrangeTop, OrangeBottom))
                    quantity > 0 -> Brush.verticalGradient(listOf(CardCream, Color(0xFFF3E2C0)))
                    else -> Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x33FFFFFF)))
                }
            )
            .border(
                width = if (isActive) 2.dp else 1.5.dp,
                color = if (isActive) Color.White else GoldBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("game_boost_${boost.id}")
            .clickable(enabled = enabled, onClick = onClick)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            BoostIconArtwork(boost = boost, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(2.dp))
            Text(
                text = boost.title.split(" ").last(),
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (isActive || quantity == 0) Color.White else TextBrownDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(if (quantity > 0) Color(0xFF15803D) else Color(0xFF64748B))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = quantity.toString(),
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GamePauseMenuOverlay(
    levelNumber: Int,
    levelTitle: String,
    onResume: () -> Unit,
    onRestartClick: () -> Unit,
    onBackToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB8140624))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .padding(20.dp)
            .testTag("game_pause_backdrop"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 390.dp)
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF6B1FA2), Color(0xFF360B57))))
                .border(3.dp, GoldBorder, RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(22.dp)
                .testTag("game_pause_menu"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedCartoonText(
                text = "Paused",
                fontSize = 32.sp,
                fillBrush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), GoldBright)),
                outlineColor = Color(0xFF4A1900),
                strokeWidth = 7f
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Level $levelNumber • $levelTitle",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFFFE082),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            GlossyMenuButton(
                text = "Resume",
                topColor = GreenTop,
                bottomColor = GreenBottom,
                rimColor = GreenRim,
                testTag = "pause_resume_button",
                onClick = onResume,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            GlossyMenuButton(
                text = "Restart",
                topColor = OrangeTop,
                bottomColor = OrangeBottom,
                rimColor = OrangeRim,
                testTag = "pause_restart_button",
                onClick = onRestartClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            GlossyMenuButton(
                text = "Back to Home",
                topColor = BlueBtnTop,
                bottomColor = BlueBtnBottom,
                rimColor = BlueBtnRim,
                testTag = "pause_home_button",
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun RestartConfirmationOverlay(
    levelNumber: Int,
    onConfirmRestart: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xC8140624))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .padding(20.dp)
            .testTag("pause_restart_confirm_backdrop"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 390.dp)
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFCF3), CardCream)))
                .border(3.5.dp, GoldBorder, RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(22.dp)
                .testTag("pause_restart_confirm_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedCartoonText(
                text = "Restart Level?",
                fontSize = 26.sp,
                fillBrush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), GoldBright, OrangeTop)),
                outlineColor = Color(0xFF4A1900),
                strokeWidth = 6f
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Restarting Level $levelNumber will reset your current score, moves, timer, and board progress.",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextBrownDark,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlossyMenuButton(
                    text = "Cancel",
                    topColor = Color(0xFF94A3B8),
                    bottomColor = Color(0xFF64748B),
                    rimColor = Color(0xFF475569),
                    testTag = "restart_confirm_cancel_button",
                    height = 50.dp,
                    fontSize = 15.sp,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {}

                GlossyMenuButton(
                    text = "Restart",
                    topColor = OrangeTop,
                    bottomColor = OrangeBottom,
                    rimColor = OrangeRim,
                    testTag = "restart_confirm_yes_button",
                    height = 50.dp,
                    fontSize = 15.sp,
                    onClick = onConfirmRestart,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun LevelWonOverlay(
    gameState: GameUiState,
    catSkin: CatSkin,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA160626))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF6B1FA2), Color(0xFF360B57))))
                .border(3.dp, GoldBorder, RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("win_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedCartoonText(
                text = "Level Complete!",
                fontSize = 30.sp,
                fillBrush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), GoldBright)),
                outlineColor = Color(0xFF4A1900),
                strokeWidth = 7f
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in 1..3) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer { alpha = if (i <= gameState.starsEarned) 1f else 0.28f }
                    ) {
                        GoldStarIcon(modifier = Modifier.fillMaxSize())
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            CartoonCatCanvas(skin = catSkin, modifier = Modifier.size(110.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Final Score: ${gameState.score}",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x55000000))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                GoldCoinIcon(modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "+${gameState.coinsEarned} Virtual Coins",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GoldBright
                )
            }
            Spacer(Modifier.height(16.dp))
            GlossyMenuButton(
                text = "Next Level",
                topColor = GreenTop,
                bottomColor = GreenBottom,
                rimColor = GreenRim,
                testTag = "win_next_level_button",
                onClick = onNextLevel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                GlossyMenuButton(
                    text = "Replay",
                    topColor = OrangeTop,
                    bottomColor = OrangeBottom,
                    rimColor = OrangeRim,
                    testTag = "win_replay_button",
                    height = 48.dp,
                    fontSize = 15.sp,
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                GlossyMenuButton(
                    text = "Home",
                    topColor = BlueBtnTop,
                    bottomColor = BlueBtnBottom,
                    rimColor = BlueBtnRim,
                    testTag = "win_home_button",
                    height = 48.dp,
                    fontSize = 15.sp,
                    onClick = onHome,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun LevelLostOverlay(
    gameState: GameUiState,
    extraLifeOwned: Int,
    coins: Int,
    onRevive: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val canRevive = extraLifeOwned > 0
    val loseHeadline = if (gameState.timeLeftSeconds == 0 && gameState.movesLeft > 0) {
        "Time's Up!"
    } else {
        "Out of Moves!"
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA160626))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF6B1FA2), Color(0xFF360B57))))
                .border(3.dp, GoldBorder, RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("lose_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedCartoonText(
                text = loseHeadline,
                fontSize = 28.sp,
                fillBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFFB4B4))),
                outlineColor = Color(0xFF5C0A15),
                strokeWidth = 7f
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Score: ${gameState.score} / Goal: ${gameState.level.targetScore}",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFFFFE082),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            if (canRevive) {
                GlossyMenuButton(
                    text = "Use Extra Life (+5 Moves)",
                    topColor = PurpleBtnTop,
                    bottomColor = PurpleBtnBottom,
                    rimColor = PurpleBtnRim,
                    testTag = "lose_revive_button",
                    fontSize = 15.sp,
                    onClick = onRevive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BoostIconArtwork(boost = BoostType.EXTRA_LIFE, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(10.dp))
            }
            GlossyMenuButton(
                text = "Try Again",
                topColor = GreenTop,
                bottomColor = GreenBottom,
                rimColor = GreenRim,
                testTag = "lose_retry_button",
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.height(10.dp))
            GlossyMenuButton(
                text = "Home",
                topColor = BlueBtnTop,
                bottomColor = BlueBtnBottom,
                rimColor = BlueBtnRim,
                testTag = "lose_home_button",
                height = 48.dp,
                fontSize = 16.sp,
                onClick = onHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White)
            }
        }
    }
}
