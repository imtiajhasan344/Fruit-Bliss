package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.BoardTheme
import com.example.model.BoostType
import com.example.model.FruitSkin
import com.example.model.FruitType
import com.example.model.SpecialFruitKind
import com.example.ui.theme.AppleRed
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.BlueberryTeal
import com.example.ui.theme.GrapePurple
import com.example.ui.theme.OrangeFruit
import com.example.ui.theme.StrawberryRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FruitTileArtwork(
    fruit: FruitType,
    special: SpecialFruitKind = SpecialFruitKind.NONE,
    skin: FruitSkin = FruitSkin.CLASSIC,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = size.minDimension * 0.38f

        when (fruit) {
            FruitType.APPLE -> drawApple(cx, cy, r)
            FruitType.ORANGE -> drawOrange(cx, cy, r)
            FruitType.STRAWBERRY -> drawStrawberry(cx, cy, r)
            FruitType.BANANA -> drawBanana(cx, cy, r)
            FruitType.GRAPE -> drawGrape(cx, cy, r)
            FruitType.BLUEBERRY -> drawBlueberry(cx, cy, r)
        }

        when (skin) {
            FruitSkin.CLASSIC -> Unit
            FruitSkin.CARTOON -> drawCartoonFaceOverlay(cx, cy, r)
            FruitSkin.CRYSTAL -> drawCrystalFacetOverlay(cx, cy, r)
            FruitSkin.GOLDEN -> drawGoldenShimmerOverlay(cx, cy, r)
        }

        when (special) {
            SpecialFruitKind.STRIPED_ROW -> {
                drawLine(
                    color = Color.White,
                    start = Offset(w * 0.10f, cy),
                    end = Offset(w * 0.90f, cy),
                    strokeWidth = r * 0.22f,
                    cap = StrokeCap.Round
                )
                drawCircle(Color(0xAAFFFFFF), radius = r * 1.1f, center = Offset(cx, cy), style = Stroke(width = 3f))
            }
            SpecialFruitKind.STRIPED_COL -> {
                drawLine(
                    color = Color.White,
                    start = Offset(cx, h * 0.10f),
                    end = Offset(cx, h * 0.90f),
                    strokeWidth = r * 0.22f,
                    cap = StrokeCap.Round
                )
                drawCircle(Color(0xAAFFFFFF), radius = r * 1.1f, center = Offset(cx, cy), style = Stroke(width = 3f))
            }
            SpecialFruitKind.BLISS_BOMB -> {
                drawCircle(
                    color = Color(0xFFFFE066),
                    radius = r * 1.15f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 4.5f)
                )
                drawCircle(
                    color = Color(0x55FFFFFF),
                    radius = r * 0.45f,
                    center = Offset(cx, cy)
                )
            }
            SpecialFruitKind.NONE -> Unit
        }
    }
}

private fun DrawScope.drawCartoonFaceOverlay(cx: Float, cy: Float, r: Float) {
    // Cheerful kawaii comic eyes & smile
    val eyeY = cy + r * 0.02f
    val eyeOffset = r * 0.28f
    val eyeR = r * 0.14f
    drawCircle(Color(0xFF1E1B4B), radius = eyeR, center = Offset(cx - eyeOffset, eyeY))
    drawCircle(Color(0xFF1E1B4B), radius = eyeR, center = Offset(cx + eyeOffset, eyeY))
    drawCircle(Color.White, radius = eyeR * 0.42f, center = Offset(cx - eyeOffset - eyeR * 0.25f, eyeY - eyeR * 0.25f))
    drawCircle(Color.White, radius = eyeR * 0.42f, center = Offset(cx + eyeOffset - eyeR * 0.25f, eyeY - eyeR * 0.25f))
    val smile = Path().apply {
        moveTo(cx - r * 0.20f, cy + r * 0.24f)
        quadraticTo(cx, cy + r * 0.46f, cx + r * 0.20f, cy + r * 0.24f)
    }
    drawPath(smile, color = Color(0xFF1E1B4B), style = Stroke(width = (r * 0.10f).coerceAtLeast(2f), cap = StrokeCap.Round))
}

private fun DrawScope.drawCrystalFacetOverlay(cx: Float, cy: Float, r: Float) {
    val diamond = Path().apply {
        moveTo(cx, cy - r * 0.72f)
        lineTo(cx + r * 0.65f, cy)
        lineTo(cx, cy + r * 0.72f)
        lineTo(cx - r * 0.65f, cy)
        close()
    }
    drawPath(diamond, color = Color(0x3DFFFFFF))
    drawPath(diamond, color = Color(0xCCECFEFF), style = Stroke(width = (r * 0.08f).coerceAtLeast(1.8f)))
    drawLine(Color(0xAAFFFFFF), Offset(cx - r * 0.65f, cy), Offset(cx + r * 0.65f, cy), strokeWidth = 1.6f)
    drawLine(Color(0xAAFFFFFF), Offset(cx, cy - r * 0.72f), Offset(cx, cy + r * 0.72f), strokeWidth = 1.6f)
}

private fun DrawScope.drawGoldenShimmerOverlay(cx: Float, cy: Float, r: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xAAFFF59D), Color(0x77FFD60A), Color(0x00F59E0B)),
            center = Offset(cx, cy),
            radius = r * 1.15f
        ),
        radius = r * 1.15f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFFFFD60A),
        radius = r * 0.94f,
        center = Offset(cx, cy),
        style = Stroke(width = (r * 0.12f).coerceAtLeast(2f))
    )
    drawSparkle(Offset(cx - r * 0.45f, cy - r * 0.45f), r * 0.26f)
    drawSparkle(Offset(cx + r * 0.48f, cy + r * 0.42f), r * 0.22f)
}

@Composable
fun BoardThemePreview(
    theme: BoardTheme,
    modifier: Modifier = Modifier
) {
    val frameColors = when (theme) {
        BoardTheme.CLASSIC -> listOf(Color(0xFFB4692C), Color(0xFF6B3410))
        BoardTheme.CANDY -> listOf(Color(0xFFFF85C0), Color(0xFFDB2777))
        BoardTheme.NEON -> listOf(Color(0xFF06B6D4), Color(0xFF9333EA))
        BoardTheme.GALAXY -> listOf(Color(0xFF312E81), Color(0xFF0F172A))
    }
    val borderColor = when (theme) {
        BoardTheme.CLASSIC -> Color(0xFFFFB703)
        BoardTheme.CANDY -> Color(0xFFFFE4E6)
        BoardTheme.NEON -> Color(0xFF22D3EE)
        BoardTheme.GALAXY -> Color(0xFFA78BFA)
    }
    val boardBg = when (theme) {
        BoardTheme.CLASSIC -> Color(0xCC2C0B48)
        BoardTheme.CANDY -> Color(0xDD831843)
        BoardTheme.NEON -> Color(0xEE090D16)
        BoardTheme.GALAXY -> Color(0xEE140736)
    }
    val lightTile = when (theme) {
        BoardTheme.CLASSIC -> Color(0x28FFF8E7)
        BoardTheme.CANDY -> Color(0x38FBCFE8)
        BoardTheme.NEON -> Color(0x2A22D3EE)
        BoardTheme.GALAXY -> Color(0x2A818CF8)
    }
    val darkTile = when (theme) {
        BoardTheme.CLASSIC -> Color(0x14000000)
        BoardTheme.CANDY -> Color(0x22BE185D)
        BoardTheme.NEON -> Color(0x2AD946EF)
        BoardTheme.GALAXY -> Color(0x330F0524)
    }
    Canvas(modifier = modifier) {
        val pad = size.minDimension * 0.09f
        drawRoundRect(
            brush = Brush.verticalGradient(frameColors),
            cornerRadius = CornerRadius(14f, 14f)
        )
        drawRoundRect(
            color = borderColor,
            cornerRadius = CornerRadius(14f, 14f),
            style = Stroke(width = 3f)
        )
        drawRoundRect(
            color = boardBg,
            topLeft = Offset(pad, pad),
            size = Size(size.width - pad * 2f, size.height - pad * 2f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        val gridW = size.width - pad * 2.4f
        val cellW = gridW / 4f
        val origin = pad * 1.2f
        for (r in 0..3) {
            for (c in 0..3) {
                drawRoundRect(
                    color = if ((r + c) % 2 == 0) lightTile else darkTile,
                    topLeft = Offset(origin + c * cellW + 1.5f, origin + r * cellW + 1.5f),
                    size = Size(cellW - 3f, cellW - 3f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }
    }
}

private fun DrawScope.drawApple(cx: Float, cy: Float, r: Float) {
    val body = Path().apply {
        moveTo(cx, cy - r * 0.65f)
        cubicTo(cx - r * 1.1f, cy - r * 0.95f, cx - r * 1.15f, cy + r * 0.45f, cx - r * 0.30f, cy + r * 0.92f)
        quadraticTo(cx, cy + r * 0.80f, cx + r * 0.30f, cy + r * 0.92f)
        cubicTo(cx + r * 1.15f, cy + r * 0.45f, cx + r * 1.1f, cy - r * 0.95f, cx, cy - r * 0.65f)
        close()
    }
    drawPath(
        path = body,
        brush = Brush.radialGradient(
            listOf(Color(0xFFFF758F), AppleRed, Color(0xFF9B0A1B)),
            center = Offset(cx - r * 0.25f, cy - r * 0.25f),
            radius = r * 1.4f
        )
    )
    drawPath(body, color = Color(0xFF6B0411), style = Stroke(width = 2.5f))
    // Stem + Leaf
    drawLine(Color(0xFF6B3A14), Offset(cx, cy - r * 0.6f), Offset(cx + r * 0.08f, cy - r * 1.05f), strokeWidth = 4f, cap = StrokeCap.Round)
    drawOval(Color(0xFF38B000), topLeft = Offset(cx + r * 0.05f, cy - r * 1.02f), size = Size(r * 0.55f, r * 0.30f))
    // Specular shine
    drawOval(Color(0x66FFFFFF), topLeft = Offset(cx - r * 0.55f, cy - r * 0.48f), size = Size(r * 0.30f, r * 0.48f))
}

private fun DrawScope.drawOrange(cx: Float, cy: Float, r: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFD166), OrangeFruit, Color(0xFFD95300)),
            center = Offset(cx - r * 0.3f, cy - r * 0.3f),
            radius = r * 1.35f
        ),
        radius = r * 0.95f,
        center = Offset(cx, cy + r * 0.08f)
    )
    drawCircle(Color(0xFF8C3300), radius = r * 0.95f, center = Offset(cx, cy + r * 0.08f), style = Stroke(width = 2.5f))
    // Leaf
    rotate(-20f, pivot = Offset(cx + r * 0.2f, cy - r * 0.85f)) {
        drawOval(Color(0xFF2EC44D), topLeft = Offset(cx - r * 0.05f, cy - r * 1.05f), size = Size(r * 0.62f, r * 0.32f))
    }
    drawCircle(Color(0x66FFFFFF), radius = r * 0.22f, center = Offset(cx - r * 0.32f, cy - r * 0.24f))
}

private fun DrawScope.drawStrawberry(cx: Float, cy: Float, r: Float) {
    val body = Path().apply {
        moveTo(cx, cy + r * 1.02f)
        cubicTo(cx - r * 1.05f, cy + r * 0.42f, cx - r * 0.98f, cy - r * 0.65f, cx, cy - r * 0.62f)
        cubicTo(cx + r * 0.98f, cy - r * 0.65f, cx + r * 1.05f, cy + r * 0.42f, cx, cy + r * 1.02f)
        close()
    }
    drawPath(
        path = body,
        brush = Brush.radialGradient(
            listOf(Color(0xFFFF6B8B), StrawberryRed, Color(0xFFB5002A)),
            center = Offset(cx - r * 0.2f, cy - r * 0.2f),
            radius = r * 1.25f
        )
    )
    drawPath(body, color = Color(0xFF70001A), style = Stroke(width = 2.5f))
    // Cap
    val cap = Path().apply {
        moveTo(cx, cy - r * 0.55f)
        lineTo(cx - r * 0.58f, cy - r * 0.82f)
        lineTo(cx - r * 0.18f, cy - r * 0.68f)
        lineTo(cx, cy - r * 0.98f)
        lineTo(cx + r * 0.18f, cy - r * 0.68f)
        lineTo(cx + r * 0.58f, cy - r * 0.82f)
        close()
    }
    drawPath(cap, color = Color(0xFF38B000))
    val seeds = listOf(Offset(-0.32f, -0.2f), Offset(0.32f, -0.2f), Offset(0f, 0.08f), Offset(-0.28f, 0.35f), Offset(0.28f, 0.35f), Offset(0f, 0.62f))
    seeds.forEach { (dx, dy) ->
        drawCircle(Color(0xFFFFE66D), radius = r * 0.085f, center = Offset(cx + dx * r, cy + dy * r))
    }
}

private fun DrawScope.drawBanana(cx: Float, cy: Float, r: Float) {
    val banana = Path().apply {
        moveTo(cx - r * 0.85f, cy - r * 0.20f)
        quadraticTo(cx - r * 0.15f, cy + r * 0.95f, cx + r * 0.88f, cy - r * 0.35f)
        quadraticTo(cx + r * 0.65f, cy + r * 0.10f, cx - r * 0.05f, cy + r * 0.30f)
        quadraticTo(cx - r * 0.55f, cy + r * 0.20f, cx - r * 0.85f, cy - r * 0.20f)
        close()
    }
    val fullBanana = Path().apply {
        moveTo(cx - r * 0.88f, cy + r * 0.10f)
        cubicTo(cx - r * 0.55f, cy + r * 0.95f, cx + r * 0.65f, cy + r * 0.85f, cx + r * 0.88f, cy - r * 0.45f)
        cubicTo(cx + r * 0.72f, cy - r * 0.70f, cx + r * 0.52f, cy - r * 0.55f, cx + r * 0.42f, cy - r * 0.15f)
        cubicTo(cx + r * 0.22f, cy + r * 0.35f, cx - r * 0.38f, cy + r * 0.35f, cx - r * 0.72f, cy - r * 0.08f)
        close()
    }
    drawPath(
        path = fullBanana,
        brush = Brush.linearGradient(listOf(Color(0xFFFFF799), BananaYellow, Color(0xFFF4B400)))
    )
    drawPath(fullBanana, color = Color(0xFF8C6200), style = Stroke(width = 2.5f, join = StrokeJoin.Round))
    drawCircle(Color(0xFF6B3A14), radius = r * 0.11f, center = Offset(cx + r * 0.74f, cy - r * 0.52f))
    // Second shadow banana peek
    drawPath(banana, color = Color(0x33D97706))
}

private fun DrawScope.drawGrape(cx: Float, cy: Float, r: Float) {
    // Stem
    drawLine(Color(0xFF2EC44D), Offset(cx, cy - r * 0.5f), Offset(cx + r * 0.1f, cy - r * 0.98f), strokeWidth = 4f, cap = StrokeCap.Round)
    val centers = listOf(
        Offset(-0.34f, -0.25f), Offset(0.34f, -0.25f), Offset(0f, -0.32f),
        Offset(-0.38f, 0.15f), Offset(0f, 0.10f), Offset(0.38f, 0.15f),
        Offset(-0.18f, 0.52f), Offset(0.18f, 0.52f), Offset(0f, 0.82f)
    )
    centers.forEach { (dx, dy) ->
        val p = Offset(cx + dx * r, cy + dy * r)
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFFD8B4FE), GrapePurple, Color(0xFF5B21B6))),
            radius = r * 0.30f,
            center = p
        )
        drawCircle(Color(0xFF3B0764), radius = r * 0.30f, center = p, style = Stroke(width = 1.6f))
        drawCircle(Color(0x66FFFFFF), radius = r * 0.08f, center = Offset(p.x - r * 0.09f, p.y - r * 0.09f))
    }
}

private fun DrawScope.drawBlueberry(cx: Float, cy: Float, r: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFF93E5FF), BlueberryTeal, Color(0xFF0369A1)),
            center = Offset(cx - r * 0.25f, cy - r * 0.25f),
            radius = r * 1.25f
        ),
        radius = r * 0.88f,
        center = Offset(cx, cy + r * 0.05f)
    )
    drawCircle(Color(0xFF0C4A6E), radius = r * 0.88f, center = Offset(cx, cy + r * 0.05f), style = Stroke(width = 2.5f))
    // Crown
    val crown = Path().apply {
        moveTo(cx - r * 0.28f, cy - r * 0.72f)
        lineTo(cx - r * 0.15f, cy - r * 0.96f)
        lineTo(cx, cy - r * 0.78f)
        lineTo(cx + r * 0.15f, cy - r * 0.96f)
        lineTo(cx + r * 0.28f, cy - r * 0.72f)
        close()
    }
    drawPath(crown, color = Color(0xFF075985))
    drawCircle(Color(0x66FFFFFF), radius = r * 0.20f, center = Offset(cx - r * 0.30f, cy - r * 0.22f))
}

@Composable
fun CoinPackArtwork(
    tier: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when {
            tier <= 1 -> {
                // Neat stack of coins
                drawCoinStack(Offset(cx - w * 0.12f, cy + h * 0.16f), w * 0.16f, 3)
                drawCoinStack(Offset(cx + w * 0.12f, cy + h * 0.06f), w * 0.16f, 4)
            }
            tier == 2 -> {
                // Coin pouch + loose coins
                drawCoinPouch(cx, cy, w * 0.34f, Color(0xFFD97706), Color(0xFF92400E))
                drawFlatCoin(Offset(cx - w * 0.24f, cy + h * 0.24f), w * 0.11f)
                drawFlatCoin(Offset(cx + w * 0.24f, cy + h * 0.22f), w * 0.11f)
            }
            tier == 3 -> {
                // Wooden Treasure Chest filled with gold
                drawTreasureChest(cx, cy, w * 0.62f, isRoyal = false)
            }
            tier == 4 -> {
                // Big pile of coins
                drawCoinStack(Offset(cx - w * 0.22f, cy + h * 0.18f), w * 0.14f, 3)
                drawCoinStack(Offset(cx, cy + h * 0.06f), w * 0.15f, 5)
                drawCoinStack(Offset(cx + w * 0.22f, cy + h * 0.16f), w * 0.14f, 4)
            }
            tier == 5 -> {
                // Overflowing Chest + stray coins
                drawTreasureChest(cx, cy - h * 0.04f, w * 0.64f, isRoyal = false)
                drawFlatCoin(Offset(cx - w * 0.28f, cy + h * 0.28f), w * 0.10f)
                drawFlatCoin(Offset(cx + w * 0.26f, cy + h * 0.29f), w * 0.10f)
            }
            tier == 6 -> {
                // Purple Royal Gem Pouch + coins
                drawCoinPouch(cx, cy, w * 0.35f, Color(0xFF9333EA), Color(0xFF581C87))
                drawCoinStack(Offset(cx - w * 0.26f, cy + h * 0.20f), w * 0.11f, 3)
                drawFlatCoin(Offset(cx + w * 0.27f, cy + h * 0.25f), w * 0.11f)
            }
            else -> {
                // Royal Overflowing Chest with Sparkles (1500..10000)
                drawTreasureChest(cx, cy, w * 0.68f, isRoyal = true)
                drawCoinStack(Offset(cx - w * 0.30f, cy + h * 0.22f), w * 0.11f, 3)
                drawCoinStack(Offset(cx + w * 0.30f, cy + h * 0.22f), w * 0.11f, 3)
                drawSparkle(Offset(cx - w * 0.32f, cy - h * 0.28f), w * 0.08f)
                drawSparkle(Offset(cx + w * 0.30f, cy - h * 0.30f), w * 0.09f)
            }
        }
    }
}

private fun DrawScope.drawFlatCoin(center: Offset, r: Float) {
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFD97706))),
        radius = r,
        center = center
    )
    drawCircle(Color(0xFF92400E), radius = r, center = center, style = Stroke(width = 2f))
    drawCircle(Color(0xFFFFE082), radius = r * 0.68f, center = center, style = Stroke(width = 1.5f))
}

private fun DrawScope.drawCoinStack(baseCenter: Offset, rx: Float, count: Int) {
    for (i in 0 until count) {
        val y = baseCenter.y - i * rx * 0.34f
        drawOval(
            color = Color(0xFFB45309),
            topLeft = Offset(baseCenter.x - rx, y - rx * 0.32f + 3f),
            size = Size(rx * 2f, rx * 0.85f)
        )
        drawOval(
            brush = Brush.horizontalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFF176), Color(0xFFFFA000))),
            topLeft = Offset(baseCenter.x - rx, y - rx * 0.42f),
            size = Size(rx * 2f, rx * 0.85f)
        )
        drawOval(
            color = Color(0xFF92400E),
            topLeft = Offset(baseCenter.x - rx, y - rx * 0.42f),
            size = Size(rx * 2f, rx * 0.85f),
            style = Stroke(width = 1.8f)
        )
    }
}

private fun DrawScope.drawCoinPouch(cx: Float, cy: Float, r: Float, topColor: Color, bottomColor: Color) {
    val bag = Path().apply {
        moveTo(cx - r * 0.45f, cy - r * 0.45f)
        cubicTo(cx - r * 1.1f, cy + r * 0.25f, cx - r * 0.9f, cy + r * 0.95f, cx, cy + r * 0.95f)
        cubicTo(cx + r * 0.9f, cy + r * 0.95f, cx + r * 1.1f, cy + r * 0.25f, cx + r * 0.45f, cy - r * 0.45f)
        lineTo(cx + r * 0.60f, cy - r * 0.85f)
        lineTo(cx - r * 0.60f, cy - r * 0.85f)
        close()
    }
    drawPath(bag, brush = Brush.verticalGradient(listOf(topColor, bottomColor)))
    drawPath(bag, color = Color(0xFF3E1807), style = Stroke(width = 2.2f))
    // Ribbon tie
    drawRoundRect(
        color = Color(0xFFFFD60A),
        topLeft = Offset(cx - r * 0.48f, cy - r * 0.50f),
        size = Size(r * 0.96f, r * 0.18f),
        cornerRadius = CornerRadius(6f, 6f)
    )
}

private fun DrawScope.drawTreasureChest(cx: Float, cy: Float, width: Float, isRoyal: Boolean) {
    val h = width * 0.72f
    // Open lid
    drawRoundRect(
        brush = Brush.verticalGradient(
            if (isRoyal) listOf(Color(0xFF9333EA), Color(0xFF581C87))
            else listOf(Color(0xFFB45309), Color(0xFF78350F))
        ),
        topLeft = Offset(cx - width * 0.46f, cy - h * 0.48f),
        size = Size(width * 0.92f, h * 0.40f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    // Gold mound inside
    drawOval(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFF176), Color(0xFFFFB300))),
        topLeft = Offset(cx - width * 0.40f, cy - h * 0.26f),
        size = Size(width * 0.80f, h * 0.36f)
    )
    // Chest base
    drawRoundRect(
        brush = Brush.verticalGradient(
            if (isRoyal) listOf(Color(0xFF7E22CE), Color(0xFF4C1D95))
            else listOf(Color(0xFFD97706), Color(0xFF78350F))
        ),
        topLeft = Offset(cx - width * 0.48f, cy - h * 0.08f),
        size = Size(width * 0.96f, h * 0.52f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Gold straps & lock
    drawRect(Color(0xFFFFD60A), topLeft = Offset(cx - width * 0.34f, cy - h * 0.08f), size = Size(width * 0.10f, h * 0.52f))
    drawRect(Color(0xFFFFD60A), topLeft = Offset(cx + width * 0.24f, cy - h * 0.08f), size = Size(width * 0.10f, h * 0.52f))
    drawRoundRect(
        color = Color(0xFFFFE066),
        topLeft = Offset(cx - width * 0.10f, cy - h * 0.02f),
        size = Size(width * 0.20f, h * 0.22f),
        cornerRadius = CornerRadius(5f, 5f)
    )
}

private fun DrawScope.drawSparkle(center: Offset, r: Float) {
    val p = Path().apply {
        moveTo(center.x, center.y - r)
        lineTo(center.x + r * 0.28f, center.y - r * 0.28f)
        lineTo(center.x + r, center.y)
        lineTo(center.x + r * 0.28f, center.y + r * 0.28f)
        lineTo(center.x, center.y + r)
        lineTo(center.x - r * 0.28f, center.y + r * 0.28f)
        lineTo(center.x - r, center.y)
        lineTo(center.x - r * 0.28f, center.y - r * 0.28f)
        close()
    }
    drawPath(p, color = Color(0xFFFFF59D))
}

@Composable
fun BoostIconArtwork(
    boost: BoostType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = size.minDimension * 0.38f

        when (boost) {
            BoostType.MAGIC_HAMMER -> {
                rotate(-32f, pivot = Offset(cx, cy)) {
                    // Handle
                    drawRoundRect(
                        color = Color(0xFFC47B3B),
                        topLeft = Offset(cx - r * 0.14f, cy - r * 0.15f),
                        size = Size(r * 0.28f, r * 1.25f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    // Blue magic head
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF1D4ED8))),
                        topLeft = Offset(cx - r * 0.65f, cy - r * 0.80f),
                        size = Size(r * 1.30f, r * 0.72f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
                drawSparkle(Offset(cx + r * 0.62f, cy - r * 0.65f), r * 0.28f)
            }
            BoostType.SHUFFLE -> {
                val pColor = Color(0xFF7E22CE)
                drawLine(pColor, Offset(cx - r * 0.7f, cy - r * 0.45f), Offset(cx + r * 0.65f, cy + r * 0.45f), strokeWidth = r * 0.26f, cap = StrokeCap.Round)
                drawLine(Color(0xFFA855F7), Offset(cx - r * 0.7f, cy + r * 0.45f), Offset(cx + r * 0.65f, cy - r * 0.45f), strokeWidth = r * 0.26f, cap = StrokeCap.Round)
                drawCircle(pColor, radius = r * 0.22f, center = Offset(cx + r * 0.68f, cy + r * 0.45f))
                drawCircle(Color(0xFFA855F7), radius = r * 0.22f, center = Offset(cx + r * 0.68f, cy - r * 0.45f))
            }
            BoostType.EXTRA_MOVE -> {
                val cyan = Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
                drawRoundRect(brush = cyan, topLeft = Offset(cx - r * 0.85f, cy - r * 0.22f), size = Size(r * 1.70f, r * 0.44f), cornerRadius = CornerRadius(10f, 10f))
                val leftTri = Path().apply {
                    moveTo(cx - r * 1.05f, cy)
                    lineTo(cx - r * 0.50f, cy - r * 0.55f)
                    lineTo(cx - r * 0.50f, cy + r * 0.55f)
                    close()
                }
                val rightTri = Path().apply {
                    moveTo(cx + r * 1.05f, cy)
                    lineTo(cx + r * 0.50f, cy - r * 0.55f)
                    lineTo(cx + r * 0.50f, cy + r * 0.55f)
                    close()
                }
                drawPath(leftTri, brush = cyan)
                drawPath(rightTri, brush = cyan)
            }
            BoostType.TIME_FREEZE -> {
                // Icy Hourglass
                drawRoundRect(Color(0xFF0369A1), topLeft = Offset(cx - r * 0.60f, cy - r * 0.90f), size = Size(r * 1.2f, r * 0.22f), cornerRadius = CornerRadius(6f, 6f))
                drawRoundRect(Color(0xFF0369A1), topLeft = Offset(cx - r * 0.60f, cy + r * 0.68f), size = Size(r * 1.2f, r * 0.22f), cornerRadius = CornerRadius(6f, 6f))
                val glass = Path().apply {
                    moveTo(cx - r * 0.48f, cy - r * 0.68f)
                    lineTo(cx + r * 0.48f, cy - r * 0.68f)
                    lineTo(cx + r * 0.12f, cy)
                    lineTo(cx + r * 0.48f, cy + r * 0.68f)
                    lineTo(cx - r * 0.48f, cy + r * 0.68f)
                    lineTo(cx - r * 0.12f, cy)
                    close()
                }
                drawPath(glass, brush = Brush.verticalGradient(listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8))))
                drawPath(glass, color = Color(0xFF0284C7), style = Stroke(width = 2.5f))
            }
            BoostType.ROCKET -> {
                rotate(42f, pivot = Offset(cx, cy)) {
                    // Flame
                    val flame = Path().apply {
                        moveTo(cx - r * 0.22f, cy + r * 0.55f)
                        lineTo(cx, cy + r * 1.05f)
                        lineTo(cx + r * 0.22f, cy + r * 0.55f)
                        close()
                    }
                    drawPath(flame, color = Color(0xFFFF8C00))
                    // Rocket body
                    drawOval(
                        brush = Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFF8FAFC))),
                        topLeft = Offset(cx - r * 0.34f, cy - r * 0.85f),
                        size = Size(r * 0.68f, r * 1.45f)
                    )
                    // Nose cone
                    val nose = Path().apply {
                        moveTo(cx, cy - r * 0.95f)
                        lineTo(cx - r * 0.26f, cy - r * 0.45f)
                        lineTo(cx + r * 0.26f, cy - r * 0.45f)
                        close()
                    }
                    drawPath(nose, color = Color(0xFFDC2626))
                    drawCircle(Color(0xFF38BDF8), radius = r * 0.16f, center = Offset(cx, cy - r * 0.10f))
                }
            }
            BoostType.BOMB -> {
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF64748B), Color(0xFF1E293B), Color(0xFF090D16))),
                    radius = r * 0.72f,
                    center = Offset(cx - r * 0.06f, cy + r * 0.14f)
                )
                // Fuse + spark
                drawLine(Color(0xFFD97706), Offset(cx + r * 0.30f, cy - r * 0.45f), Offset(cx + r * 0.58f, cy - r * 0.78f), strokeWidth = 4f, cap = StrokeCap.Round)
                drawCircle(Color(0xFFFFD60A), radius = r * 0.22f, center = Offset(cx + r * 0.62f, cy - r * 0.80f))
                drawCircle(Color(0xFFFF5400), radius = r * 0.12f, center = Offset(cx + r * 0.62f, cy - r * 0.80f))
            }
            BoostType.SUPER_HINT -> {
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFFFF59D), Color(0xFFFFCA28), Color(0xFFF59E0B))),
                    radius = r * 0.58f,
                    center = Offset(cx, cy - r * 0.15f)
                )
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(cx - r * 0.26f, cy + r * 0.38f),
                    size = Size(r * 0.52f, r * 0.34f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                for (a in listOf(-45f, 0f, 45f)) {
                    rotate(a, pivot = Offset(cx, cy - r * 0.15f)) {
                        drawLine(Color(0xFFFFB703), Offset(cx, cy - r * 0.88f), Offset(cx, cy - r * 1.05f), strokeWidth = 3.5f, cap = StrokeCap.Round)
                    }
                }
            }
            BoostType.MEGA_SHUFFLE -> {
                val colors = listOf(Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B))
                colors.forEachIndexed { idx, col ->
                    drawArc(
                        color = col,
                        startAngle = idx * 90f + 12f,
                        sweepAngle = 66f,
                        useCenter = false,
                        topLeft = Offset(cx - r * 0.72f, cy - r * 0.72f),
                        size = Size(r * 1.44f, r * 1.44f),
                        style = Stroke(width = r * 0.28f, cap = StrokeCap.Round)
                    )
                }
            }
            BoostType.DOUBLE_COINS -> {
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFD97706))),
                    radius = r * 0.78f,
                    center = Offset(cx, cy)
                )
                drawCircle(Color(0xFF92400E), radius = r * 0.78f, center = Offset(cx, cy), style = Stroke(width = 2.5f))
                drawSparkle(Offset(cx + r * 0.65f, cy - r * 0.65f), r * 0.26f)
                drawSparkle(Offset(cx - r * 0.68f, cy + r * 0.58f), r * 0.20f)
            }
            BoostType.ADD_TIME -> {
                drawCircle(Color(0xFF0EA5E9), radius = r * 0.78f, center = Offset(cx, cy + r * 0.08f))
                drawCircle(Color.White, radius = r * 0.62f, center = Offset(cx, cy + r * 0.08f))
                drawRoundRect(Color(0xFF0284C7), topLeft = Offset(cx - r * 0.22f, cy - r * 0.88f), size = Size(r * 0.44f, r * 0.20f), cornerRadius = CornerRadius(4f, 4f))
                drawLine(Color(0xFFEF4444), Offset(cx, cy + r * 0.08f), Offset(cx, cy - r * 0.35f), strokeWidth = 4f, cap = StrokeCap.Round)
                drawLine(Color(0xFF1E293B), Offset(cx, cy + r * 0.08f), Offset(cx + r * 0.32f, cy + r * 0.20f), strokeWidth = 3.5f, cap = StrokeCap.Round)
            }
            BoostType.EXTRA_LIFE -> {
                val heart = Path().apply {
                    moveTo(cx, cy + r * 0.80f)
                    cubicTo(cx - r * 1.15f, cy + r * 0.15f, cx - r * 0.95f, cy - r * 0.80f, cx, cy - r * 0.25f)
                    cubicTo(cx + r * 0.95f, cy - r * 0.80f, cx + r * 1.15f, cy + r * 0.15f, cx, cy + r * 0.80f)
                    close()
                }
                drawPath(heart, brush = Brush.verticalGradient(listOf(Color(0xFFFF5A79), Color(0xFFDC2626))))
                drawPath(heart, color = Color(0xFF7F1D1D), style = Stroke(width = 2.5f))
                drawCircle(Color(0x77FFFFFF), radius = r * 0.16f, center = Offset(cx - r * 0.35f, cy - r * 0.26f))
            }
            BoostType.MEGA_BONUS -> {
                val crown = Path().apply {
                    moveTo(cx - r * 0.82f, cy + r * 0.55f)
                    lineTo(cx - r * 0.90f, cy - r * 0.40f)
                    lineTo(cx - r * 0.38f, cy + r * 0.05f)
                    lineTo(cx, cy - r * 0.65f)
                    lineTo(cx + r * 0.38f, cy + r * 0.05f)
                    lineTo(cx + r * 0.90f, cy - r * 0.40f)
                    lineTo(cx + r * 0.82f, cy + r * 0.55f)
                    close()
                }
                drawPath(crown, brush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), Color(0xFFFFB703), Color(0xFFD97706))))
                drawPath(crown, color = Color(0xFF78350F), style = Stroke(width = 2.5f, join = StrokeJoin.Round))
                drawCircle(Color(0xFFEF4444), radius = r * 0.12f, center = Offset(cx, cy + r * 0.24f))
                drawCircle(Color(0xFF3B82F6), radius = r * 0.10f, center = Offset(cx - r * 0.45f, cy + r * 0.28f))
                drawCircle(Color(0xFF10B981), radius = r * 0.10f, center = Offset(cx + r * 0.45f, cy + r * 0.28f))
            }
        }
    }
}
