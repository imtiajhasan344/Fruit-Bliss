package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.BackgroundTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeadowBackground(
    modifier: Modifier = Modifier,
    theme: BackgroundTheme = BackgroundTheme.CLASSIC_MEADOW,
    dimmedForModal: Boolean = false
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (theme) {
            BackgroundTheme.CLASSIC_MEADOW -> drawClassicMeadow(w, h)
            BackgroundTheme.CANDY_WORLD -> drawCandyWorld(w, h)
            BackgroundTheme.JUNGLE_WORLD -> drawJungleWorld(w, h)
            BackgroundTheme.SNOW_WORLD -> drawSnowWorld(w, h)
            BackgroundTheme.GALAXY_WORLD -> drawGalaxyWorld(w, h)
            BackgroundTheme.OCEAN_WORLD -> drawOceanWorld(w, h)
            BackgroundTheme.FANTASY_WORLD -> drawFantasyWorld(w, h)
        }

        if (dimmedForModal) {
            drawRect(color = Color(0x661E0A36))
        }
    }
}

@Composable
fun BackgroundThemePreview(
    theme: BackgroundTheme,
    modifier: Modifier = Modifier
) {
    MeadowBackground(
        modifier = modifier,
        theme = theme,
        dimmedForModal = false
    )
}

private fun DrawScope.drawClassicMeadow(w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF2FA4F7), Color(0xFF70D1FE), Color(0xFFB5ECFF)),
            startY = 0f,
            endY = h * 0.62f
        )
    )
    drawCloud(Offset(w * 0.18f, h * 0.14f), w * 0.16f)
    drawCloud(Offset(w * 0.68f, h * 0.18f), w * 0.21f)
    drawCloud(Offset(w * 0.88f, h * 0.27f), w * 0.14f)

    val backHill = Path().apply {
        moveTo(0f, h * 0.56f)
        quadraticTo(w * 0.28f, h * 0.45f, w * 0.58f, h * 0.52f)
        quadraticTo(w * 0.82f, h * 0.57f, w, h * 0.48f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = backHill,
        brush = Brush.verticalGradient(listOf(Color(0xFF6ADB4B), Color(0xFF3CAE34)), h * 0.45f, h)
    )

    val frontMeadow = Path().apply {
        moveTo(0f, h * 0.61f)
        quadraticTo(w * 0.35f, h * 0.56f, w * 0.65f, h * 0.60f)
        quadraticTo(w * 0.85f, h * 0.63f, w, h * 0.58f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = frontMeadow,
        brush = Brush.verticalGradient(listOf(Color(0xFF83E544), Color(0xFF39A92E)), h * 0.56f, h)
    )

    val pathShape = Path().apply {
        moveTo(w * 0.54f, h * 0.57f)
        quadraticTo(w * 0.66f, h * 0.68f, w * 0.55f, h * 0.80f)
        quadraticTo(w * 0.47f, h * 0.90f, w * 0.45f, h)
        lineTo(w * 0.72f, h)
        quadraticTo(w * 0.70f, h * 0.85f, w * 0.73f, h * 0.72f)
        quadraticTo(w * 0.74f, h * 0.63f, w * 0.63f, h * 0.57f)
        close()
    }
    drawPath(
        path = pathShape,
        brush = Brush.verticalGradient(listOf(Color(0xFFF3C68F), Color(0xFFD69A61)), h * 0.57f, h)
    )
    listOf(
        Offset(w * 0.61f, h * 0.64f) to w * 0.045f,
        Offset(w * 0.66f, h * 0.70f) to w * 0.052f,
        Offset(w * 0.59f, h * 0.76f) to w * 0.06f,
        Offset(w * 0.64f, h * 0.83f) to w * 0.055f,
        Offset(w * 0.56f, h * 0.90f) to w * 0.068f,
        Offset(w * 0.62f, h * 0.96f) to w * 0.072f
    ).forEach { (center, radius) ->
        drawOval(
            color = Color(0x55FFF0D4),
            topLeft = Offset(center.x - radius, center.y - radius * 0.5f),
            size = Size(radius * 2f, radius)
        )
    }

    drawCottage(Offset(w * 0.76f, h * 0.42f), w * 0.22f)
    drawCottage(Offset(w * 0.05f, h * 0.53f), w * 0.11f)
    drawOakTree(w, h)
    drawLeafyBushesAndFlowers(w, h)
}

private fun DrawScope.drawCandyWorld(w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFF85C0), Color(0xFFFFB8DE), Color(0xFFFFE6F5))
        )
    )
    drawCloud(Offset(w * 0.22f, h * 0.15f), w * 0.18f, Color(0xE8FFF0FA))
    drawCloud(Offset(w * 0.76f, h * 0.18f), w * 0.20f, Color(0xE8FFF0FA))

    // Back strawberry hill
    val backHill = Path().apply {
        moveTo(0f, h * 0.55f)
        quadraticTo(w * 0.3f, h * 0.44f, w * 0.65f, h * 0.53f)
        quadraticTo(w * 0.85f, h * 0.58f, w, h * 0.47f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(backHill, Brush.verticalGradient(listOf(Color(0xFFFF7EB6), Color(0xFFE0488D)), h * 0.44f, h))

    // Mint frosting hill
    val frontHill = Path().apply {
        moveTo(0f, h * 0.64f)
        quadraticTo(w * 0.4f, h * 0.56f, w * 0.78f, h * 0.64f)
        quadraticTo(w * 0.92f, h * 0.67f, w, h * 0.61f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(frontHill, Brush.verticalGradient(listOf(Color(0xFFA7F3D0), Color(0xFF34D399)), h * 0.56f, h))

    // Giant lollipops
    drawLollipop(Offset(w * 0.16f, h * 0.42f), w * 0.085f, h * 0.26f, Color(0xFFFF3D7F), Color.White)
    drawLollipop(Offset(w * 0.84f, h * 0.46f), w * 0.075f, h * 0.24f, Color(0xFF38BDF8), Color(0xFFFFF59D))
    drawLollipop(Offset(w * 0.46f, h * 0.50f), w * 0.055f, h * 0.18f, Color(0xFFA855F7), Color.White)

    // Sprinkles along foreground
    val sprinkleColors = listOf(Color(0xFFFF3D7F), Color(0xFFFACC15), Color(0xFF38BDF8), Color(0xFFFFFFFF))
    for (i in 0..14) {
        val sx = w * (0.06f + (i * 0.064f))
        val sy = h * (0.72f + (i % 4) * 0.06f)
        drawLine(
            color = sprinkleColors[i % sprinkleColors.size],
            start = Offset(sx, sy),
            end = Offset(sx + w * 0.025f, sy + h * 0.012f),
            strokeWidth = (w * 0.012f).coerceAtLeast(3f),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawLollipop(center: Offset, radius: Float, stickH: Float, c1: Color, c2: Color) {
    drawLine(
        color = Color(0xFFFDF2F8),
        start = Offset(center.x, center.y + radius * 0.8f),
        end = Offset(center.x, center.y + stickH),
        strokeWidth = (radius * 0.22f).coerceAtLeast(3f),
        cap = StrokeCap.Round
    )
    drawCircle(c1, radius = radius, center = center)
    drawCircle(c2, radius = radius * 0.65f, center = center, style = Stroke(width = (radius * 0.22f).coerceAtLeast(2f)))
    drawCircle(c2, radius = radius * 0.24f, center = center)
    drawCircle(Color(0xFF831843), radius = radius, center = center, style = Stroke(width = 2f))
}

private fun DrawScope.drawJungleWorld(w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF14532D), Color(0xFF22C55E), Color(0xFFBEF264))
        )
    )
    // Sunbeams
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0x88FEF08A), Color.Transparent), Offset(w * 0.72f, h * 0.18f), w * 0.45f),
        radius = w * 0.45f,
        center = Offset(w * 0.72f, h * 0.18f)
    )

    // Back & front emerald canopy hills
    val hill1 = Path().apply {
        moveTo(0f, h * 0.56f)
        quadraticTo(w * 0.35f, h * 0.46f, w * 0.7f, h * 0.54f)
        quadraticTo(w * 0.88f, h * 0.58f, w, h * 0.49f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(hill1, Brush.verticalGradient(listOf(Color(0xFF16A34A), Color(0xFF14532D)), h * 0.46f, h))

    val hill2 = Path().apply {
        moveTo(0f, h * 0.68f)
        quadraticTo(w * 0.45f, h * 0.58f, w, h * 0.66f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(hill2, Brush.verticalGradient(listOf(Color(0xFF4ADE80), Color(0xFF15803D)), h * 0.58f, h))

    // Hanging jungle vines
    listOf(0.14f, 0.32f, 0.78f, 0.90f).forEachIndexed { idx, xp ->
        val vx = w * xp
        val len = h * (0.30f + (idx % 2) * 0.12f)
        drawLine(Color(0xFF166534), Offset(vx, 0f), Offset(vx + w * 0.02f, len), strokeWidth = 4f, cap = StrokeCap.Round)
        for (l in 1..4) {
            val ly = len * (l / 4.5f)
            drawOval(Color(0xFF4ADE80), topLeft = Offset(vx - w * 0.025f, ly), size = Size(w * 0.055f, h * 0.025f))
        }
    }

    // Palm silhouettes on left & right
    drawPalmTree(Offset(w * 0.12f, h * 0.72f), w * 0.18f, h * 0.42f)
    drawPalmTree(Offset(w * 0.88f, h * 0.75f), w * 0.16f, h * 0.38f)
}

private fun DrawScope.drawPalmTree(base: Offset, crownW: Float, height: Float) {
    val top = Offset(base.x + crownW * 0.08f, base.y - height)
    drawLine(Color(0xFF78350F), base, top, strokeWidth = (crownW * 0.14f).coerceAtLeast(4f), cap = StrokeCap.Round)
    val leafColor = Color(0xFF15803D)
    for (a in listOf(-150f, -110f, -65f, -20f, 15f)) {
        val rad = Math.toRadians(a.toDouble())
        val tip = Offset(top.x + cos(rad).toFloat() * crownW, top.y + sin(rad).toFloat() * crownW * 0.65f)
        drawLine(leafColor, top, tip, strokeWidth = (crownW * 0.15f).coerceAtLeast(3f), cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawSnowWorld(w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF1E3A8A), Color(0xFF38BDF8), Color(0xFFE0F2FE))
        )
    )
    // Snowy mountain peaks
    val m1 = Path().apply {
        moveTo(0f, h * 0.62f); lineTo(w * 0.28f, h * 0.30f); lineTo(w * 0.58f, h * 0.62f); close()
    }
    drawPath(m1, Color(0xFF7DD3FC))
    val m2 = Path().apply {
        moveTo(w * 0.40f, h * 0.64f); lineTo(w * 0.74f, h * 0.28f); lineTo(w, h * 0.64f); close()
    }
    drawPath(m2, Color(0xFF93C5FD))

    // White snow drifts
    val drift = Path().apply {
        moveTo(0f, h * 0.58f)
        quadraticTo(w * 0.35f, h * 0.51f, w * 0.68f, h * 0.59f)
        quadraticTo(w * 0.88f, h * 0.63f, w, h * 0.56f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(drift, Brush.verticalGradient(listOf(Color.White, Color(0xFFDBEAFE)), h * 0.51f, h))

    // Frosted pines
    drawPineTree(Offset(w * 0.14f, h * 0.68f), w * 0.14f, h * 0.24f)
    drawPineTree(Offset(w * 0.86f, h * 0.68f), w * 0.13f, h * 0.22f)

    // Drifting snowflakes
    for (i in 0..22) {
        val sx = w * (((i * 37) % 97) / 100f + 0.02f)
        val sy = h * (((i * 53) % 85) / 100f + 0.04f)
        drawCircle(Color(0xDDF0F9FF), radius = (w * (0.008f + (i % 3) * 0.004f)).coerceAtLeast(2f), center = Offset(sx, sy))
    }
}

private fun DrawScope.drawPineTree(base: Offset, width: Float, height: Float) {
    drawRect(Color(0xFF78350F), Offset(base.x - width * 0.1f, base.y - height * 0.18f), Size(width * 0.2f, height * 0.2f))
    val tier = Path().apply {
        moveTo(base.x, base.y - height)
        lineTo(base.x - width * 0.5f, base.y - height * 0.15f)
        lineTo(base.x + width * 0.5f, base.y - height * 0.15f)
        close()
    }
    drawPath(tier, Color(0xFF0F766E))
    val cap = Path().apply {
        moveTo(base.x, base.y - height)
        lineTo(base.x - width * 0.26f, base.y - height * 0.55f)
        lineTo(base.x + width * 0.26f, base.y - height * 0.55f)
        close()
    }
    drawPath(cap, Color(0xFFF0F9FF))
}

private fun DrawScope.drawGalaxyWorld(w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF09001A), Color(0xFF240046), Color(0xFF5A189A))
        )
    )
    // Nebula glow
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0x66EC4899), Color.Transparent), Offset(w * 0.30f, h * 0.34f), w * 0.45f),
        radius = w * 0.45f,
        center = Offset(w * 0.30f, h * 0.34f)
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0x5506B6D4), Color.Transparent), Offset(w * 0.78f, h * 0.62f), w * 0.42f),
        radius = w * 0.42f,
        center = Offset(w * 0.78f, h * 0.62f)
    )

    // Twinkling stars
    for (i in 0..32) {
        val sx = w * (((i * 29 + 11) % 97) / 100f + 0.02f)
        val sy = h * (((i * 41 + 7) % 92) / 100f + 0.03f)
        val col = if (i % 4 == 0) Color(0xFFFDE047) else Color(0xFFE0E7FF)
        drawCircle(col, radius = (w * (0.005f + (i % 3) * 0.004f)).coerceAtLeast(1.5f), center = Offset(sx, sy))
    }

    // Ringed Saturn planet
    val planetCenter = Offset(w * 0.76f, h * 0.22f)
    val pr = w * 0.11f
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFDE047), Color(0xFFF97316), Color(0xFF9A3412))),
        radius = pr,
        center = planetCenter
    )
    drawOval(
        color = Color(0xCCBAE6FD),
        topLeft = Offset(planetCenter.x - pr * 1.65f, planetCenter.y - pr * 0.34f),
        size = Size(pr * 3.3f, pr * 0.68f),
        style = Stroke(width = (pr * 0.22f).coerceAtLeast(2.5f))
    )

    // Moon hill foreground
    val moonHill = Path().apply {
        moveTo(0f, h * 0.66f)
        quadraticTo(w * 0.48f, h * 0.56f, w, h * 0.68f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(moonHill, Brush.verticalGradient(listOf(Color(0xFF4C1D95), Color(0xFF1E1B4B)), h * 0.56f, h))
}

private fun DrawScope.drawOceanWorld(w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF00B4D8), Color(0xFF0077B6), Color(0xFF03045E))
        )
    )
    // Light rays from surface
    for (i in 0..3) {
        val rx = w * (0.18f + i * 0.22f)
        drawLine(Color(0x28CAF0F8), Offset(rx, 0f), Offset(rx + w * 0.08f, h * 0.72f), strokeWidth = w * 0.08f)
    }

    // Sandy sea floor
    val sand = Path().apply {
        moveTo(0f, h * 0.70f)
        quadraticTo(w * 0.38f, h * 0.62f, w * 0.75f, h * 0.70f)
        quadraticTo(w * 0.90f, h * 0.73f, w, h * 0.67f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(sand, Brush.verticalGradient(listOf(Color(0xFFFDE68A), Color(0xFFD97706)), h * 0.62f, h))

    // Coral & seaweed silhouettes
    drawCircle(Color(0xFFFF5E7E), radius = w * 0.08f, center = Offset(w * 0.14f, h * 0.72f))
    drawCircle(Color(0xFFFF85A1), radius = w * 0.06f, center = Offset(w * 0.22f, h * 0.76f))
    drawCircle(Color(0xFFFB7185), radius = w * 0.07f, center = Offset(w * 0.86f, h * 0.73f))

    // Rising bubbles
    for (i in 0..16) {
        val bx = w * (((i * 31 + 9) % 92) / 100f + 0.04f)
        val by = h * (((i * 47 + 13) % 75) / 100f + 0.06f)
        val br = (w * (0.014f + (i % 4) * 0.008f)).coerceAtLeast(2.5f)
        drawCircle(Color(0x44CAF0F8), radius = br, center = Offset(bx, by))
        drawCircle(Color(0xAAE0F2FE), radius = br, center = Offset(bx, by), style = Stroke(width = 1.6f))
    }
}

private fun DrawScope.drawFantasyWorld(w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF4C1D95), Color(0xFFEC4899), Color(0xFFFDE047))
        )
    )
    // Aurora ribbon
    val aurora = Path().apply {
        moveTo(0f, h * 0.25f)
        cubicTo(w * 0.30f, h * 0.12f, w * 0.65f, h * 0.36f, w, h * 0.18f)
        lineTo(w, h * 0.32f)
        cubicTo(w * 0.65f, h * 0.48f, w * 0.30f, h * 0.24f, 0f, h * 0.38f)
        close()
    }
    drawPath(aurora, Brush.horizontalGradient(listOf(Color(0x5534D399), Color(0x5560A5FA), Color(0x55F472B6))))

    // Floating crystal spires
    drawCrystalSpire(Offset(w * 0.18f, h * 0.56f), w * 0.09f, h * 0.20f, Color(0xFF67E8F9))
    drawCrystalSpire(Offset(w * 0.82f, h * 0.54f), w * 0.10f, h * 0.22f, Color(0xFFF0ABFC))

    // Enchanted lavender hills
    val fHill = Path().apply {
        moveTo(0f, h * 0.62f)
        quadraticTo(w * 0.45f, h * 0.52f, w, h * 0.62f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(fHill, Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF581C87)), h * 0.52f, h))

    // Golden fairy sparkles
    for (i in 0..18) {
        val fx = w * (((i * 33 + 5) % 94) / 100f + 0.03f)
        val fy = h * (((i * 59 + 11) % 82) / 100f + 0.05f)
        drawCircle(Color(0xFFFFF59D), radius = (w * 0.008f).coerceAtLeast(2f), center = Offset(fx, fy))
    }
}

private fun DrawScope.drawCrystalSpire(base: Offset, width: Float, height: Float, color: Color) {
    val crystal = Path().apply {
        moveTo(base.x, base.y - height)
        lineTo(base.x + width * 0.55f, base.y - height * 0.35f)
        lineTo(base.x + width * 0.32f, base.y)
        lineTo(base.x - width * 0.32f, base.y)
        lineTo(base.x - width * 0.55f, base.y - height * 0.35f)
        close()
    }
    drawPath(crystal, color.copy(alpha = 0.85f))
    drawPath(crystal, Color.White.copy(alpha = 0.7f), style = Stroke(width = 2f))
}

private fun DrawScope.drawCloud(center: Offset, width: Float, color: Color = Color(0xEBFFFFFF)) {
    drawCircle(color, radius = width * 0.34f, center = Offset(center.x - width * 0.28f, center.y))
    drawCircle(color, radius = width * 0.44f, center = Offset(center.x, center.y - width * 0.10f))
    drawCircle(color, radius = width * 0.32f, center = Offset(center.x + width * 0.30f, center.y + width * 0.02f))
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - width * 0.52f, center.y - width * 0.12f),
        size = Size(width * 1.04f, width * 0.44f),
        cornerRadius = CornerRadius(width * 0.25f, width * 0.25f)
    )
}

private fun DrawScope.drawCottage(origin: Offset, scale: Float) {
    drawRect(
        color = Color(0xFFBD5B38),
        topLeft = Offset(origin.x + scale * 0.62f, origin.y - scale * 0.25f),
        size = Size(scale * 0.16f, scale * 0.32f)
    )
    drawRoundRect(
        color = Color(0xFFFFF1D6),
        topLeft = Offset(origin.x + scale * 0.10f, origin.y + scale * 0.22f),
        size = Size(scale * 0.78f, scale * 0.55f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    val roof = Path().apply {
        moveTo(origin.x, origin.y + scale * 0.26f)
        lineTo(origin.x + scale * 0.48f, origin.y - scale * 0.18f)
        lineTo(origin.x + scale * 0.98f, origin.y + scale * 0.26f)
        close()
    }
    drawPath(
        path = roof,
        brush = Brush.verticalGradient(listOf(Color(0xFFF26430), Color(0xFFBF3B1B)), origin.y - scale * 0.18f, origin.y + scale * 0.26f)
    )
    drawRoundRect(
        color = Color(0xFF8C491C),
        topLeft = Offset(origin.x + scale * 0.40f, origin.y + scale * 0.44f),
        size = Size(scale * 0.20f, scale * 0.33f),
        cornerRadius = CornerRadius(scale * 0.08f, scale * 0.08f)
    )
    drawRoundRect(
        color = Color(0xFFFFD166),
        topLeft = Offset(origin.x + scale * 0.18f, origin.y + scale * 0.40f),
        size = Size(scale * 0.15f, scale * 0.15f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color(0xFFFFD166),
        topLeft = Offset(origin.x + scale * 0.66f, origin.y + scale * 0.40f),
        size = Size(scale * 0.15f, scale * 0.15f),
        cornerRadius = CornerRadius(4f, 4f)
    )
}

private fun DrawScope.drawOakTree(w: Float, h: Float) {
    val trunk = Path().apply {
        moveTo(w * 0.02f, h * 0.56f)
        quadraticTo(w * 0.06f, h * 0.36f, w * 0.03f, h * 0.20f)
        lineTo(w * 0.11f, h * 0.20f)
        quadraticTo(w * 0.09f, h * 0.38f, w * 0.15f, h * 0.56f)
        close()
    }
    drawPath(trunk, color = Color(0xFF7B3F19))

    val darkGreen = Color(0xFF1F8727)
    val midGreen = Color(0xFF39B837)
    val lightGreen = Color(0xFF72E34E)
    drawCircle(darkGreen, radius = w * 0.22f, center = Offset(w * 0.08f, h * 0.14f))
    drawCircle(midGreen, radius = w * 0.17f, center = Offset(w * 0.18f, h * 0.12f))
    drawCircle(midGreen, radius = w * 0.15f, center = Offset(w * 0.04f, h * 0.22f))
    drawCircle(lightGreen, radius = w * 0.13f, center = Offset(w * 0.14f, h * 0.08f))
    drawCircle(lightGreen, radius = w * 0.10f, center = Offset(w * 0.24f, h * 0.18f))
}

private fun DrawScope.drawLeafyBushesAndFlowers(w: Float, h: Float) {
    val bushColors = listOf(Color(0xFF1D7A24), Color(0xFF2FA335), Color(0xFF47C742))
    for (i in 0..9) {
        val x = w * (i / 9f)
        val r = w * (0.09f + (i % 3) * 0.02f)
        drawCircle(bushColors[i % 3], radius = r, center = Offset(x, h * 0.99f))
    }

    listOf(
        Triple(Offset(w * 0.07f, h * 0.91f), w * 0.032f, Color(0xFFFF5E97)),
        Triple(Offset(w * 0.15f, h * 0.95f), w * 0.026f, Color(0xFFFFE042)),
        Triple(Offset(w * 0.04f, h * 0.78f), w * 0.024f, Color(0xFF4FC3F7)),
        Triple(Offset(w * 0.88f, h * 0.93f), w * 0.034f, Color(0xFFFF5E97)),
        Triple(Offset(w * 0.94f, h * 0.87f), w * 0.026f, Color(0xFFFFE042)),
        Triple(Offset(w * 0.79f, h * 0.96f), w * 0.025f, Color(0xFFFF5E97)),
        Triple(Offset(w * 0.95f, h * 0.75f), w * 0.024f, Color(0xFF4FC3F7)),
        Triple(Offset(w * 0.27f, h * 0.96f), w * 0.022f, Color(0xFFFFE042))
    ).forEach { (center, radius, petalColor) ->
        drawFlower(center, radius, petalColor)
    }
}

private fun DrawScope.drawFlower(center: Offset, petalRadius: Float, petalColor: Color) {
    for (k in 0 until 5) {
        val angle = (k * 72.0) * Math.PI / 180.0
        val px = center.x + cos(angle).toFloat() * petalRadius * 0.92f
        val py = center.y + sin(angle).toFloat() * petalRadius * 0.92f
        drawCircle(color = petalColor, radius = petalRadius * 0.65f, center = Offset(px, py))
    }
    drawCircle(color = Color(0xFFFFD500), radius = petalRadius * 0.52f, center = center)
    drawCircle(color = Color(0x44B87A00), radius = petalRadius * 0.52f, center = center, style = Stroke(width = 2f))
}
