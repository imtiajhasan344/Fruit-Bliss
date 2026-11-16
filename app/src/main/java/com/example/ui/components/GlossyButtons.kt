package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GreenBottom
import com.example.ui.theme.GreenRim
import com.example.ui.theme.GreenTop
import com.example.ui.theme.HudPillBg
import com.example.ui.theme.RedCloseBottom
import com.example.ui.theme.RedCloseTop
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CoinCurrencyPill(
    amount: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .background(HudPillBg)
            .border(1.5.dp, Color(0x88FFD60A), RoundedCornerShape(50))
            .clickable(onClick = onAddClick)
            .padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoldCoinIcon(modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = amount.toString(),
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
            maxLines = 1
        )
        Spacer(Modifier.width(6.dp))
        PlusCircleBadge(onClick = onAddClick, testTag = "add_coins_button")
    }
}

@Composable
private fun PlusCircleBadge(onClick: () -> Unit, testTag: String) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(GreenTop, GreenBottom)))
            .border(1.5.dp, Color.White, CircleShape)
            .testTag(testTag)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun GoldCoinIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFF176), Color(0xFFFFB300), Color(0xFFE65100))
            ),
            radius = r,
            center = c
        )
        drawCircle(
            color = Color(0xFFFFE082),
            radius = r * 0.74f,
            center = c,
            style = Stroke(width = r * 0.14f)
        )
        drawCircle(
            color = Color(0xFF9C4E00),
            radius = r * 0.96f,
            center = c,
            style = Stroke(width = 2f)
        )
        // "$" vertical bar
        drawLine(
            color = Color(0xFFB45309),
            start = Offset(c.x, c.y - r * 0.44f),
            end = Offset(c.x, c.y + r * 0.44f),
            strokeWidth = r * 0.14f
        )
    }
}

@Composable
fun GoldStarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension * 0.48f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val path = Path()
        for (i in 0 until 10) {
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            val rad = if (i % 2 == 0) r else r * 0.45f
            val x = cx + cos(angle).toFloat() * rad
            val y = cy + sin(angle).toFloat() * rad
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(
            path = path,
            brush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), GoldBright, Color(0xFFF57F17)))
        )
        drawPath(path, color = Color(0xFF8C4900), style = Stroke(width = 2f, join = StrokeJoin.Round))
    }
}

@Composable
fun TopBarShortcutTile(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .testTag(testTag)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF42B8FF), Color(0xFF1261C4))))
                .border(2.dp, Color(0xFF9BE7FF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Top shine
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(14.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x44FFFFFF))
            )
            iconContent()
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 11.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0xCC0D234A),
                    offset = Offset(1f, 2f),
                    blurRadius = 3f
                )
            )
        )
    }
}

@Composable
fun GlossyMenuButton(
    text: String,
    topColor: Color,
    bottomColor: Color,
    rimColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    fontSize: TextUnit = 20.sp,
    leadingIcon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .height(height)
            .shadow(6.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(rimColor)
            .padding(bottom = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .border(2.dp, Color(0x99FFFFFF), RoundedCornerShape(50))
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Gloss highlight
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(height * 0.42f)
                .padding(horizontal = 10.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x40FFFFFF))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon()
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0x88000000),
                        offset = Offset(0f, 2f),
                        blurRadius = 3f
                    )
                )
            )
        }
    }
}

@Composable
fun HeroPlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 68.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "play_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(height)
            .shadow(10.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(GreenRim)
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(GreenTop, GreenBottom)))
            .border(2.5.dp, Color(0xCCFFFFFF), RoundedCornerShape(50))
            .testTag("play_button")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(26.dp)
                .padding(horizontal = 14.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x45FFFFFF))
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Play",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = Color.White,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xAA084B0A),
                        offset = Offset(0f, 3f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

@Composable
fun GreenPriceButton(
    priceText: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 30.dp,
    fontSize: TextUnit = 13.sp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp)
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 30.dp)
            .height(height)
            .shadow(3.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(GreenRim)
            .padding(bottom = 2.dp)
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(GreenTop, GreenBottom)))
            .border(1.dp, Color(0x99FFFFFF), RoundedCornerShape(50))
            .testTag(testTag)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priceText,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = Color.White,
            maxLines = 1,
            style = TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0x88084B0A),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 2f
                )
            )
        )
    }
}

@Composable
fun RedCloseCircleButton(
    onClick: () -> Unit,
    testTag: String = "close_button",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .padding(4.dp)
            .shadow(5.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(RedCloseTop, RedCloseBottom)))
            .border(2.5.dp, Color.White, CircleShape)
            .testTag(testTag)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun StorefrontMiniIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Shop base
        drawRoundRect(
            color = Color(0xFFF3F4F6),
            topLeft = Offset(w * 0.14f, h * 0.42f),
            size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.48f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // Window + door
        drawRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.22f, h * 0.54f),
            size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.22f)
        )
        drawRect(
            color = Color(0xFF5B21B6),
            topLeft = Offset(w * 0.54f, h * 0.54f),
            size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.36f)
        )
        // Striped Awning
        val stripeCount = 5
        val stripeW = (w * 0.84f) / stripeCount
        for (i in 0 until stripeCount) {
            drawRoundRect(
                color = if (i % 2 == 0) Color(0xFFEC4899) else Color.White,
                topLeft = Offset(w * 0.08f + i * stripeW, h * 0.16f),
                size = androidx.compose.ui.geometry.Size(stripeW + 1f, h * 0.30f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
}
