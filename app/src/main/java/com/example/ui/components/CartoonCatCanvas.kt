package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.CatSkin

/**
 * Cute cartoon cat sitting on a wooden tree stump, supporting equipped CatSkin styles.
 */
@Composable
fun CartoonCatCanvas(
    modifier: Modifier = Modifier,
    skin: CatSkin = CatSkin.CLASSIC
) {
    val transition = rememberInfiniteTransition(label = "cat_idle")
    val waveAngle by transition.animateFloat(
        initialValue = -7f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "paw_wave"
    )
    val tailSway by transition.animateFloat(
        initialValue = -5f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tail_sway"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val furOrangeLight = when (skin) {
            CatSkin.CLASSIC -> Color(0xFFFFA938)
            CatSkin.ROYAL -> Color(0xFFFFB74D)
            CatSkin.NINJA -> Color(0xFF334155)
            CatSkin.SPACE -> Color(0xFFE2E8F0)
            CatSkin.GOLDEN -> Color(0xFFFFE066)
        }
        val furOrangeMid = when (skin) {
            CatSkin.CLASSIC -> Color(0xFFF37A1F)
            CatSkin.ROYAL -> Color(0xFFF59E0B)
            CatSkin.NINJA -> Color(0xFF1E293B)
            CatSkin.SPACE -> Color(0xFF94A3B8)
            CatSkin.GOLDEN -> Color(0xFFF59E0B)
        }
        val furOrangeDark = when (skin) {
            CatSkin.CLASSIC -> Color(0xFFD65A10)
            CatSkin.ROYAL -> Color(0xFFB45309)
            CatSkin.NINJA -> Color(0xFF0F172A)
            CatSkin.SPACE -> Color(0xFF64748B)
            CatSkin.GOLDEN -> Color(0xFFB45309)
        }
        val creamWhite = when (skin) {
            CatSkin.NINJA -> Color(0xFFCBD5E1)
            CatSkin.GOLDEN -> Color(0xFFFFFBEB)
            else -> Color(0xFFFFF8EE)
        }
        val outlineBrown = when (skin) {
            CatSkin.NINJA -> Color(0xFF020617)
            CatSkin.SPACE -> Color(0xFF334155)
            else -> Color(0xFF6B3210)
        }

        // --- 1. Wooden Tree Stump ---
        val stumpTopY = h * 0.78f
        val stumpBottomY = h * 0.96f
        val stumpPath = Path().apply {
            moveTo(w * 0.14f, stumpTopY)
            lineTo(w * 0.78f, stumpTopY)
            quadraticTo(w * 0.84f, h * 0.89f, w * 0.88f, stumpBottomY)
            quadraticTo(w * 0.46f, h * 1.01f, w * 0.06f, stumpBottomY)
            quadraticTo(w * 0.10f, h * 0.89f, w * 0.14f, stumpTopY)
            close()
        }
        drawPath(
            path = stumpPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFC47B3B), Color(0xFF7B4116)),
                startY = stumpTopY,
                endY = stumpBottomY
            )
        )
        // Bark lines
        for (k in 1..5) {
            val bx = w * (0.16f + k * 0.11f)
            drawLine(
                color = Color(0x554E2408),
                start = Offset(bx, stumpTopY + h * 0.04f),
                end = Offset(bx - w * 0.01f, stumpBottomY - h * 0.02f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
        // Stump top ring
        drawOval(
            color = Color(0xFFE7AD6B),
            topLeft = Offset(w * 0.13f, stumpTopY - h * 0.045f),
            size = Size(w * 0.66f, h * 0.09f)
        )
        drawOval(
            color = Color(0x669E5B24),
            topLeft = Offset(w * 0.21f, stumpTopY - h * 0.028f),
            size = Size(w * 0.50f, h * 0.055f),
            style = Stroke(width = 2.5f)
        )

        // --- 2. Swaying Tail ---
        rotate(degrees = tailSway, pivot = Offset(w * 0.24f, h * 0.73f)) {
            val tailPath = Path().apply {
                moveTo(w * 0.24f, h * 0.73f)
                cubicTo(w * 0.05f, h * 0.68f, w * 0.03f, h * 0.48f, w * 0.15f, h * 0.47f)
                cubicTo(w * 0.21f, h * 0.47f, w * 0.22f, h * 0.57f, w * 0.17f, h * 0.62f)
                cubicTo(w * 0.16f, h * 0.65f, w * 0.24f, h * 0.67f, w * 0.27f, h * 0.68f)
                close()
            }
            drawPath(
                path = tailPath,
                brush = Brush.linearGradient(listOf(furOrangeMid, furOrangeLight))
            )
            drawPath(tailPath, color = outlineBrown, style = Stroke(width = 2.5f))
            // Cream tail tip
            drawCircle(creamWhite, radius = w * 0.042f, center = Offset(w * 0.14f, h * 0.50f))
        }

        // --- 3. Sitting Cat Body ---
        val bodyCenterX = w * 0.45f
        val bodyTopY = h * 0.48f
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(furOrangeLight, furOrangeMid),
                startY = bodyTopY,
                endY = h * 0.82f
            ),
            topLeft = Offset(w * 0.22f, bodyTopY),
            size = Size(w * 0.45f, h * 0.34f)
        )
        drawOval(
            color = outlineBrown,
            topLeft = Offset(w * 0.22f, bodyTopY),
            size = Size(w * 0.45f, h * 0.34f),
            style = Stroke(width = 3f)
        )
        // White Belly
        drawOval(
            color = creamWhite,
            topLeft = Offset(w * 0.31f, h * 0.54f),
            size = Size(w * 0.28f, h * 0.26f)
        )

        // --- 4. Waving Right Paw (viewer's right) ---
        rotate(degrees = waveAngle, pivot = Offset(w * 0.62f, h * 0.57f)) {
            val wavingArm = Path().apply {
                moveTo(w * 0.60f, h * 0.56f)
                quadraticTo(w * 0.78f, h * 0.47f, w * 0.81f, h * 0.37f)
                quadraticTo(w * 0.85f, h * 0.31f, w * 0.76f, h * 0.31f)
                quadraticTo(w * 0.68f, h * 0.34f, w * 0.64f, h * 0.44f)
                quadraticTo(w * 0.59f, h * 0.48f, w * 0.56f, h * 0.53f)
                close()
            }
            drawPath(wavingArm, color = furOrangeLight)
            drawPath(wavingArm, color = outlineBrown, style = Stroke(width = 2.8f))
            // Cream paw tip + pink toe beans
            drawCircle(creamWhite, radius = w * 0.068f, center = Offset(w * 0.78f, h * 0.36f))
            val padColor = Color(0xFFFF8AA1)
            drawCircle(padColor, radius = w * 0.022f, center = Offset(w * 0.78f, h * 0.37f))
            drawCircle(padColor, radius = w * 0.011f, center = Offset(w * 0.75f, h * 0.335f))
            drawCircle(padColor, radius = w * 0.011f, center = Offset(w * 0.78f, h * 0.325f))
            drawCircle(padColor, radius = w * 0.011f, center = Offset(w * 0.81f, h * 0.338f))
        }

        // Front Left Paw & Back Paws resting on stump
        drawOval(
            color = creamWhite,
            topLeft = Offset(w * 0.24f, h * 0.72f),
            size = Size(w * 0.14f, h * 0.09f)
        )
        drawOval(
            color = outlineBrown,
            topLeft = Offset(w * 0.24f, h * 0.72f),
            size = Size(w * 0.14f, h * 0.09f),
            style = Stroke(width = 2.5f)
        )
        drawOval(
            color = creamWhite,
            topLeft = Offset(w * 0.37f, h * 0.73f),
            size = Size(w * 0.14f, h * 0.095f)
        )
        drawOval(
            color = outlineBrown,
            topLeft = Offset(w * 0.37f, h * 0.73f),
            size = Size(w * 0.14f, h * 0.095f),
            style = Stroke(width = 2.5f)
        )
        drawOval(
            color = creamWhite,
            topLeft = Offset(w * 0.51f, h * 0.72f),
            size = Size(w * 0.14f, h * 0.09f)
        )
        drawOval(
            color = outlineBrown,
            topLeft = Offset(w * 0.51f, h * 0.72f),
            size = Size(w * 0.14f, h * 0.09f),
            style = Stroke(width = 2.5f)
        )

        // --- 5. Bandana / Collar matching CatSkin ---
        val bandanaTop = when (skin) {
            CatSkin.CLASSIC -> Color(0xFFF72C3D)
            CatSkin.ROYAL -> Color(0xFF9333EA)
            CatSkin.NINJA -> Color(0xFFDC2626)
            CatSkin.SPACE -> Color(0xFF0284C7)
            CatSkin.GOLDEN -> Color(0xFFFFD60A)
        }
        val bandanaBottom = when (skin) {
            CatSkin.CLASSIC -> Color(0xFFBA1121)
            CatSkin.ROYAL -> Color(0xFF581C87)
            CatSkin.NINJA -> Color(0xFF7F1D1D)
            CatSkin.SPACE -> Color(0xFF0369A1)
            CatSkin.GOLDEN -> Color(0xFFD97706)
        }
        val bandanaBorder = when (skin) {
            CatSkin.ROYAL -> Color(0xFFFFD54F)
            CatSkin.SPACE -> Color(0xFFBAE6FD)
            else -> Color(0xFF700812)
        }
        val bandanaPath = Path().apply {
            moveTo(w * 0.26f, h * 0.49f)
            quadraticTo(w * 0.45f, h * 0.56f, w * 0.64f, h * 0.49f)
            lineTo(w * 0.58f, h * 0.62f)
            quadraticTo(w * 0.46f, h * 0.69f, w * 0.33f, h * 0.61f)
            close()
        }
        drawPath(
            path = bandanaPath,
            brush = Brush.verticalGradient(listOf(bandanaTop, bandanaBottom))
        )
        drawPath(bandanaPath, color = bandanaBorder, style = Stroke(width = 2.5f))

        // Knot on left side of bandana
        drawCircle(bandanaTop, radius = w * 0.036f, center = Offset(w * 0.27f, h * 0.53f))
        // Yellow Paw Emblem on bandana
        val pawGold = Color(0xFFFFD83B)
        val pawCenter = Offset(w * 0.47f, h * 0.605f)
        drawOval(
            color = pawGold,
            topLeft = Offset(pawCenter.x - w * 0.030f, pawCenter.y - w * 0.015f),
            size = Size(w * 0.060f, w * 0.044f)
        )
        drawCircle(pawGold, radius = w * 0.013f, center = Offset(pawCenter.x - w * 0.030f, pawCenter.y - w * 0.025f))
        drawCircle(pawGold, radius = w * 0.013f, center = Offset(pawCenter.x, pawCenter.y - w * 0.035f))
        drawCircle(pawGold, radius = w * 0.013f, center = Offset(pawCenter.x + w * 0.030f, pawCenter.y - w * 0.025f))

        // --- 6. Cute Cat Head & Ears ---
        // Left Ear
        val leftEar = Path().apply {
            moveTo(w * 0.22f, h * 0.28f)
            quadraticTo(w * 0.16f, h * 0.07f, w * 0.27f, h * 0.07f)
            quadraticTo(w * 0.34f, h * 0.12f, w * 0.36f, h * 0.21f)
            close()
        }
        drawPath(leftEar, color = furOrangeLight)
        drawPath(leftEar, color = outlineBrown, style = Stroke(width = 2.8f))
        val leftEarInner = Path().apply {
            moveTo(w * 0.24f, h * 0.24f)
            quadraticTo(w * 0.21f, h * 0.12f, w * 0.27f, h * 0.12f)
            quadraticTo(w * 0.31f, h * 0.16f, w * 0.32f, h * 0.21f)
            close()
        }
        drawPath(leftEarInner, color = Color(0xFFFF9FB2))

        // Right Ear
        val rightEar = Path().apply {
            moveTo(w * 0.56f, h * 0.21f)
            quadraticTo(w * 0.62f, h * 0.08f, w * 0.70f, h * 0.09f)
            quadraticTo(w * 0.76f, h * 0.15f, w * 0.69f, h * 0.29f)
            close()
        }
        drawPath(rightEar, color = furOrangeLight)
        drawPath(rightEar, color = outlineBrown, style = Stroke(width = 2.8f))
        val rightEarInner = Path().apply {
            moveTo(w * 0.59f, h * 0.21f)
            quadraticTo(w * 0.63f, h * 0.13f, w * 0.68f, h * 0.14f)
            quadraticTo(w * 0.70f, h * 0.18f, w * 0.66f, h * 0.24f)
            close()
        }
        drawPath(rightEarInner, color = Color(0xFFFF9FB2))

        // Head Oval
        val headLeft = w * 0.16f
        val headTop = h * 0.17f
        val headW = w * 0.58f
        val headH = h * 0.36f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (skin == CatSkin.GOLDEN) Color(0xFFFFF59D) else furOrangeLight,
                    furOrangeMid
                ),
                center = Offset(bodyCenterX, h * 0.32f),
                radius = w * 0.34f
            ),
            topLeft = Offset(headLeft, headTop),
            size = Size(headW, headH)
        )
        drawOval(
            color = outlineBrown,
            topLeft = Offset(headLeft, headTop),
            size = Size(headW, headH),
            style = Stroke(width = 3f)
        )

        // Cream White Face Blaze & Muzzle
        val muzzlePath = Path().apply {
            moveTo(w * 0.45f, h * 0.20f)
            quadraticTo(w * 0.49f, h * 0.31f, w * 0.61f, h * 0.37f)
            quadraticTo(w * 0.67f, h * 0.47f, w * 0.45f, h * 0.52f)
            quadraticTo(w * 0.24f, h * 0.47f, w * 0.30f, h * 0.37f)
            quadraticTo(w * 0.41f, h * 0.31f, w * 0.45f, h * 0.20f)
            close()
        }
        drawPath(muzzlePath, color = creamWhite)

        // Tabby Forehead Stripes
        for (s in -1..1) {
            val sx = w * (0.45f + s * 0.045f)
            drawRoundRect(
                color = furOrangeDark,
                topLeft = Offset(sx - w * 0.012f, h * 0.18f),
                size = Size(w * 0.024f, h * 0.055f),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }
        // Cheek Stripes
        drawLine(furOrangeDark, Offset(w * 0.18f, h * 0.34f), Offset(w * 0.25f, h * 0.35f), strokeWidth = 4f, cap = StrokeCap.Round)
        drawLine(furOrangeDark, Offset(w * 0.18f, h * 0.38f), Offset(w * 0.25f, h * 0.385f), strokeWidth = 4f, cap = StrokeCap.Round)
        drawLine(furOrangeDark, Offset(w * 0.66f, h * 0.35f), Offset(w * 0.72f, h * 0.34f), strokeWidth = 4f, cap = StrokeCap.Round)
        drawLine(furOrangeDark, Offset(w * 0.66f, h * 0.385f), Offset(w * 0.72f, h * 0.38f), strokeWidth = 4f, cap = StrokeCap.Round)

        // --- 7. Sparkling Big Blue Eyes ---
        drawCatEye(Offset(w * 0.34f, h * 0.34f), w * 0.068f)
        drawCatEye(Offset(w * 0.56f, h * 0.34f), w * 0.068f)

        // Rosy Cheeks
        drawOval(
            color = Color(0x66FF7096),
            topLeft = Offset(w * 0.24f, h * 0.39f),
            size = Size(w * 0.07f, h * 0.035f)
        )
        drawOval(
            color = Color(0x66FF7096),
            topLeft = Offset(w * 0.59f, h * 0.39f),
            size = Size(w * 0.07f, h * 0.035f)
        )

        // Pink Nose
        val nose = Path().apply {
            moveTo(w * 0.43f, h * 0.39f)
            lineTo(w * 0.48f, h * 0.39f)
            lineTo(w * 0.455f, h * 0.415f)
            close()
        }
        drawPath(nose, color = Color(0xFFFF6E8D))

        // Happy Open Smile & Pink Tongue
        val mouth = Path().apply {
            moveTo(w * 0.40f, h * 0.425f)
            quadraticTo(w * 0.455f, h * 0.495f, w * 0.51f, h * 0.425f)
            close()
        }
        drawPath(mouth, color = Color(0xFF9C1F33))
        drawOval(
            color = Color(0xFFFF7A95),
            topLeft = Offset(w * 0.43f, h * 0.445f),
            size = Size(w * 0.052f, h * 0.030f)
        )

        // Whiskers
        val whiskerColor = Color(0xAA4A250E)
        drawLine(whiskerColor, Offset(w * 0.26f, h * 0.41f), Offset(w * 0.11f, h * 0.39f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        drawLine(whiskerColor, Offset(w * 0.26f, h * 0.435f), Offset(w * 0.11f, h * 0.445f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        drawLine(whiskerColor, Offset(w * 0.65f, h * 0.41f), Offset(w * 0.79f, h * 0.39f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        drawLine(whiskerColor, Offset(w * 0.65f, h * 0.435f), Offset(w * 0.79f, h * 0.445f), strokeWidth = 2.2f, cap = StrokeCap.Round)

        // --- 8. CatSkin Headgear & Special Effects ---
        when (skin) {
            CatSkin.ROYAL -> {
                val crown = Path().apply {
                    moveTo(w * 0.33f, h * 0.19f)
                    lineTo(w * 0.31f, h * 0.07f)
                    lineTo(w * 0.39f, h * 0.13f)
                    lineTo(w * 0.45f, h * 0.04f)
                    lineTo(w * 0.51f, h * 0.13f)
                    lineTo(w * 0.59f, h * 0.07f)
                    lineTo(w * 0.57f, h * 0.19f)
                    close()
                }
                drawPath(crown, Brush.verticalGradient(listOf(Color(0xFFFFF59D), Color(0xFFF59E0B))))
                drawPath(crown, Color(0xFF78350F), style = Stroke(width = 2.4f))
                drawCircle(Color(0xFFEF4444), radius = w * 0.020f, center = Offset(w * 0.45f, h * 0.14f))
                drawCircle(Color(0xFF38BDF8), radius = w * 0.015f, center = Offset(w * 0.37f, h * 0.16f))
                drawCircle(Color(0xFF38BDF8), radius = w * 0.015f, center = Offset(w * 0.53f, h * 0.16f))
            }
            CatSkin.NINJA -> {
                // Crimson Ninja Headband & metal crest + trailing ties
                drawRoundRect(
                    color = Color(0xFFDC2626),
                    topLeft = Offset(w * 0.18f, h * 0.21f),
                    size = Size(w * 0.54f, h * 0.065f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(w * 0.37f, h * 0.215f),
                    size = Size(w * 0.16f, h * 0.052f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawLine(Color(0xFFDC2626), Offset(w * 0.18f, h * 0.24f), Offset(w * 0.06f, h * 0.21f), strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(Color(0xFFDC2626), Offset(w * 0.18f, h * 0.25f), Offset(w * 0.08f, h * 0.29f), strokeWidth = 4.5f, cap = StrokeCap.Round)
            }
            CatSkin.SPACE -> {
                // Translucent Astro Glass Dome Helmet + antenna
                val domeCenter = Offset(w * 0.45f, h * 0.32f)
                drawLine(Color(0xFF38BDF8), Offset(w * 0.45f, h * 0.06f), Offset(w * 0.45f, h * 0.01f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawCircle(Color(0xFF22D3EE), radius = w * 0.022f, center = Offset(w * 0.45f, h * 0.01f))
                drawCircle(Color(0x2A38BDF8), radius = w * 0.33f, center = domeCenter)
                drawCircle(Color(0xCCBAE6FD), radius = w * 0.33f, center = domeCenter, style = Stroke(width = 3.5f))
                drawArc(
                    color = Color(0xAAFFFFFF),
                    startAngle = 210f,
                    sweepAngle = 55f,
                    useCenter = false,
                    topLeft = Offset(domeCenter.x - w * 0.26f, domeCenter.y - w * 0.26f),
                    size = Size(w * 0.52f, w * 0.52f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
            CatSkin.GOLDEN -> {
                // 24K Diamond Sparkle Aura
                listOf(
                    Offset(w * 0.15f, h * 0.16f) to w * 0.032f,
                    Offset(w * 0.78f, h * 0.16f) to w * 0.036f,
                    Offset(w * 0.84f, h * 0.48f) to w * 0.028f,
                    Offset(w * 0.12f, h * 0.55f) to w * 0.028f
                ).forEach { (c, r) ->
                    drawLine(Color(0xFFFFF9C4), Offset(c.x - r, c.y), Offset(c.x + r, c.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
                    drawLine(Color(0xFFFFF9C4), Offset(c.x, c.y - r), Offset(c.x, c.y + r), strokeWidth = 2.5f, cap = StrokeCap.Round)
                }
            }
            CatSkin.CLASSIC -> Unit
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCatEye(
    center: Offset,
    radius: Float
) {
    // Outer white sclera
    drawCircle(color = Color.White, radius = radius, center = center)
    // Rich blue-indigo iris
    drawCircle(
        brush = Brush.verticalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF38BDF8))),
        radius = radius * 0.85f,
        center = center
    )
    // Deep black pupil
    drawCircle(
        color = Color(0xFF0A1128),
        radius = radius * 0.52f,
        center = center
    )
    // Specular sparkles
    drawCircle(
        color = Color.White,
        radius = radius * 0.27f,
        center = Offset(center.x - radius * 0.28f, center.y - radius * 0.28f)
    )
    drawCircle(
        color = Color.White,
        radius = radius * 0.13f,
        center = Offset(center.x + radius * 0.28f, center.y + radius * 0.26f)
    )
    drawCircle(
        color = Color(0xFF3D1E08),
        radius = radius,
        center = center,
        style = Stroke(width = 2.2f)
    )
}
