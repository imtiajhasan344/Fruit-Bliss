package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.BoostIconArtwork
import com.example.ui.components.CoinPackArtwork
import com.example.ui.components.GlossyMenuButton
import com.example.ui.components.OutlinedCartoonText
import com.example.ui.theme.CardCream
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GreenBottom
import com.example.ui.theme.GreenRim
import com.example.ui.theme.GreenTop
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.OrangeBottom
import com.example.ui.theme.OrangeTop
import com.example.ui.theme.StorePurpleDark
import com.example.ui.theme.StorePurpleDeep
import com.example.ui.theme.StorePurpleMid
import com.example.ui.theme.TextBrownDark
import com.example.viewmodel.PurchaseSuccessReward
import kotlinx.coroutines.launch

@Composable
fun PurchaseSuccessfulPopup(
    reward: PurchaseSuccessReward,
    onDismiss: () -> Unit
) {
    // Only system Back button or the GREAT! button may close this popup
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB8140624))
            .pointerInput(Unit) {
                detectTapGestures { /* Intentionally block outside taps & background interaction */ }
            }
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("purchase_success_backdrop"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(22.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFCF3), CardCream, Color(0xFFFFEBC2))
                    )
                )
                .border(4.dp, GoldBorder, RoundedCornerShape(30.dp))
                .testTag("purchase_success_dialog")
                .pointerInput(Unit) {
                    detectTapGestures { /* Consume non-button card taps */ }
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Green Checkmark Icon
            AnimatedGreenCheckmark(
                modifier = Modifier
                    .size(96.dp)
                    .testTag("purchase_success_checkmark")
            )

            Spacer(Modifier.height(14.dp))

            // Heading: "Purchase Successful!"
            OutlinedCartoonText(
                text = "Purchase Successful!",
                fontSize = 24.sp,
                fillBrush = Brush.verticalGradient(
                    listOf(Color(0xFFFFF59D), GoldBright, OrangeTop)
                ),
                outlineColor = Color(0xFF4A1800),
                strokeWidth = 7f,
                modifier = Modifier.testTag("purchase_success_heading")
            )

            val isDemoReward = BuildConfig.DEBUG &&
                BuildConfig.DEMO_PURCHASES_ENABLED &&
                when (reward) {
                    is PurchaseSuccessReward.CoinPackReward -> reward.isDemoTransaction
                    is PurchaseSuccessReward.BoostReward -> reward.isDemoTransaction
                    is PurchaseSuccessReward.ThemeItemReward -> false
                }

            if (isDemoReward) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF92400E))
                        .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("purchase_success_test_mode_badge")
                ) {
                    Text(
                        text = "TEST MODE — NO REAL PAYMENT",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFFEF3C7)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Reward Showcase Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(StorePurpleMid, StorePurpleDeep, StorePurpleDark)
                        )
                    )
                    .border(2.5.dp, GoldBright, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Reward Added!" Badge with Attractive Reward Icon
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(OrangeTop, OrangeBottom)))
                        .border(1.5.dp, Color(0xFFFFF59D), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CardGiftcard,
                        contentDescription = "Reward Added",
                        tint = Color(0xFFFFF8E1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Reward Added!",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Purchased Coin Pack or Boost Icon & Name
                when (reward) {
                    is PurchaseSuccessReward.CoinPackReward -> {
                        val totalCoins = reward.pack.coins * reward.quantity
                        val rewardTitle = if (reward.quantity > 1) {
                            "${"%,d".format(totalCoins)} Coins"
                        } else {
                            reward.pack.title
                        }
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFF8E1), Color(0xFFFFE082), Color(0xFFE7C78C))
                                    )
                                )
                                .border(3.dp, GoldBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CoinPackArtwork(
                                tier = reward.pack.tier,
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = rewardTitle,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFFFFE082),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("purchase_success_reward_name")
                        )
                        Text(
                            text = "Added to Treasure Vault Balance",
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFE9D5FF),
                            textAlign = TextAlign.Center
                        )
                    }

                    is PurchaseSuccessReward.BoostReward -> {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFF8E1), Color(0xFFFFE082), Color(0xFFE7C78C))
                                    )
                                )
                                .border(3.dp, GoldBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            BoostIconArtwork(
                                boost = reward.boost,
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = reward.boost.title,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFFFFE082),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("purchase_success_reward_name")
                        )
                        Text(
                            text = "x${reward.quantity} Added to Boost Forge",
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFE9D5FF),
                            textAlign = TextAlign.Center
                        )
                    }

                    is PurchaseSuccessReward.ThemeItemReward -> {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(3.dp, GoldBorder, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            ThemeItemPreviewArtwork(
                                item = reward.item,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = reward.item.title,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFFFFE082),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("purchase_success_reward_name")
                        )
                        Text(
                            text = "Unlocked in Theme Shop (${reward.item.category.title})",
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFE9D5FF),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Short confirmation message
            Text(
                text = "Your purchase was completed successfully.",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = TextBrownDark,
                textAlign = TextAlign.Center
            )

            if (isDemoReward) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Demo test reward added locally — not a real Google Play transaction.",
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF92400E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("purchase_success_demo_disclaimer")
                )
            }

            Spacer(Modifier.height(20.dp))

            // Bottom Center Green "GREAT!" Button
            GlossyMenuButton(
                text = "GREAT!",
                topColor = GreenTop,
                bottomColor = GreenBottom,
                rimColor = GreenRim,
                testTag = "purchase_success_great_button",
                height = 58.dp,
                fontSize = 22.sp,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(0.84f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedGreenCheckmark(modifier: Modifier = Modifier) {
    val badgeScale = remember { Animatable(0.35f) }
    val checkProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            badgeScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            checkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 480,
                    delayMillis = 90,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = badgeScale.value
                    scaleY = badgeScale.value
                }
        ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer soft celebratory glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x664ADE80), Color(0x004ADE80)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Green glossy badge body
        val badgeRadius = radius * 0.84f
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(GreenTop, GreenBottom)
            ),
            radius = badgeRadius,
            center = center
        )

        // Rim border
        drawCircle(
            color = GreenRim,
            radius = badgeRadius,
            center = center,
            style = Stroke(width = size.minDimension * 0.045f)
        )

        // Inner highlight crescent
        drawCircle(
            color = Color(0x3DFFFFFF),
            radius = badgeRadius * 0.84f,
            center = Offset(center.x, center.y - badgeRadius * 0.08f)
        )

        // Animated checkmark stroke
        val fullPath = Path().apply {
            moveTo(size.width * 0.29f, size.height * 0.51f)
            lineTo(size.width * 0.44f, size.height * 0.66f)
            lineTo(size.width * 0.72f, size.height * 0.36f)
        }
        val measured = PathMeasure().apply { setPath(fullPath, false) }
        val animatedPath = Path()
        measured.getSegment(0f, measured.length * checkProgress.value, animatedPath, true)

        drawPath(
            path = animatedPath,
            color = Color.White,
            style = Stroke(
                width = size.minDimension * 0.11f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        }
    }
}

@Composable
fun PurchaseCancelledTopToast(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 12.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xF03A0E63), Color(0xF0250642))
                    )
                )
                .border(2.dp, GoldBright, RoundedCornerShape(50))
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("purchase_cancelled_toast"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Purchase Cancelled",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}
