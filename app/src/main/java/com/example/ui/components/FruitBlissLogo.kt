package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FredokaFontFamily
import kotlin.math.cos
import kotlin.math.sin

/**
 * Wooden oval plaque surrounded by green leaves, two glossy red strawberries,
 * white blossoms, and bold 3D "Fruit Bliss" lettering matching the reference image.
 */
@Composable
fun FruitBlissLogo(
    modifier: Modifier = Modifier,
    width: Dp = 270.dp,
    height: Dp = 148.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Leaves behind plaque
            drawLeaf(Offset(w * 0.14f, h * 0.25f), w * 0.12f, -35f)
            drawLeaf(Offset(w * 0.22f, h * 0.14f), w * 0.11f, -15f)
            drawLeaf(Offset(w * 0.86f, h * 0.25f), w * 0.12f, 35f)
            drawLeaf(Offset(w * 0.78f, h * 0.14f), w * 0.11f, 15f)
            drawLeaf(Offset(w * 0.14f, h * 0.74f), w * 0.11f, -135f)
            drawLeaf(Offset(w * 0.86f, h * 0.74f), w * 0.11f, 135f)

            // Wooden Oval Plaque
            val plaqueLeft = w * 0.14f
            val plaqueTop = h * 0.17f
            val plaqueW = w * 0.72f
            val plaqueH = h * 0.68f
            // Drop shadow
            drawOval(
                color = Color(0x552E1204),
                topLeft = Offset(plaqueLeft, plaqueTop + 8f),
                size = Size(plaqueW, plaqueH)
            )
            // Outer rim
            drawOval(
                color = Color(0xFF703310),
                topLeft = Offset(plaqueLeft, plaqueTop),
                size = Size(plaqueW, plaqueH)
            )
            // Inner wood grain fill
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE79F52), Color(0xFFB55D22))
                ),
                topLeft = Offset(plaqueLeft + 7f, plaqueTop + 7f),
                size = Size(plaqueW - 14f, plaqueH - 14f)
            )

            // Left & Right Strawberries
            rotate(-18f, pivot = Offset(w * 0.17f, h * 0.45f)) {
                drawLogoStrawberry(Offset(w * 0.17f, h * 0.45f), w * 0.085f)
            }
            rotate(18f, pivot = Offset(w * 0.83f, h * 0.45f)) {
                drawLogoStrawberry(Offset(w * 0.83f, h * 0.45f), w * 0.085f)
            }

            // Cute White Blossoms
            drawWhiteBlossom(Offset(w * 0.22f, h * 0.67f), w * 0.036f)
            drawWhiteBlossom(Offset(w * 0.78f, h * 0.67f), w * 0.030f)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-2).dp)
        ) {
            OutlinedCartoonText(
                text = "Fruit",
                fontSize = 44.sp,
                fillBrush = Brush.verticalGradient(
                    listOf(Color(0xFFFFF066), Color(0xFFFFB703), Color(0xFFFF7B00))
                ),
                outlineColor = Color(0xFF691D0B)
            )
            OutlinedCartoonText(
                text = "Bliss",
                fontSize = 42.sp,
                fillBrush = Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFEAE2FF))
                ),
                outlineColor = Color(0xFF3E1878),
                modifier = Modifier.offset(y = (-12).dp)
            )
        }
    }
}

@Composable
fun OutlinedCartoonText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fillBrush: Brush,
    outlineColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 10f
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Drop shadow
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = Color(0x66000000)
            ),
            modifier = Modifier.offset(x = 0.dp, y = 3.dp)
        )
        // Thick outline
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = outlineColor,
                drawStyle = Stroke(width = strokeWidth, join = StrokeJoin.Round)
            )
        )
        // Gradient fill
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                brush = fillBrush
            )
        )
    }
}

private fun DrawScope.drawLeaf(center: Offset, length: Float, angle: Float) {
    rotate(angle, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y - length * 0.5f)
            quadraticTo(center.x + length * 0.45f, center.y, center.x, center.y + length * 0.5f)
            quadraticTo(center.x - length * 0.45f, center.y, center.x, center.y - length * 0.5f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(listOf(Color(0xFF7AF04B), Color(0xFF1F9A29)))
        )
        drawPath(path, color = Color(0xFF115717), style = Stroke(width = 2.5f))
    }
}

private fun DrawScope.drawLogoStrawberry(center: Offset, r: Float) {
    val body = Path().apply {
        moveTo(center.x, center.y + r * 1.15f)
        cubicTo(
            center.x - r * 1.1f, center.y + r * 0.45f,
            center.x - r * 1.0f, center.y - r * 0.75f,
            center.x, center.y - r * 0.72f
        )
        cubicTo(
            center.x + r * 1.0f, center.y - r * 0.75f,
            center.x + r * 1.1f, center.y + r * 0.45f,
            center.x, center.y + r * 1.15f
        )
        close()
    }
    drawPath(
        path = body,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF5975), Color(0xFFD90429)),
            center = Offset(center.x - r * 0.25f, center.y - r * 0.2f),
            radius = r * 1.3f
        )
    )
    drawPath(body, color = Color(0xFF7A0416), style = Stroke(width = 2.5f))

    // Green cap
    val cap = Path().apply {
        moveTo(center.x, center.y - r * 0.62f)
        lineTo(center.x - r * 0.65f, center.y - r * 0.92f)
        lineTo(center.x - r * 0.2f, center.y - r * 0.78f)
        lineTo(center.x, center.y - r * 1.08f)
        lineTo(center.x + r * 0.2f, center.y - r * 0.78f)
        lineTo(center.x + r * 0.65f, center.y - r * 0.92f)
        close()
    }
    drawPath(cap, color = Color(0xFF2DC653))
    // Seeds
    val seedColor = Color(0xFFFFE169)
    val seeds = listOf(
        Offset(-0.35f, -0.25f), Offset(0.35f, -0.25f),
        Offset(0f, 0.02f),
        Offset(-0.40f, 0.26f), Offset(0.40f, 0.26f),
        Offset(0f, 0.56f)
    )
    seeds.forEach { (dx, dy) ->
        drawCircle(seedColor, radius = r * 0.09f, center = Offset(center.x + dx * r, center.y + dy * r))
    }
}

private fun DrawScope.drawWhiteBlossom(center: Offset, r: Float) {
    for (k in 0 until 5) {
        val a = (k * 72.0) * Math.PI / 180.0
        drawCircle(
            color = Color.White,
            radius = r * 0.62f,
            center = Offset(
                center.x + cos(a).toFloat() * r * 0.75f,
                center.y + sin(a).toFloat() * r * 0.75f
            )
        )
    }
    drawCircle(color = Color(0xFFFFC300), radius = r * 0.48f, center = center)
}
