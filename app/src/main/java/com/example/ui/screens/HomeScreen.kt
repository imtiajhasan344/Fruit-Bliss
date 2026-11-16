package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.model.CatSkin
import com.example.model.HomeModal
import com.example.model.StoreTab
import com.example.ui.components.CartoonCatCanvas
import com.example.ui.components.CoinCurrencyPill
import com.example.ui.components.FruitBlissLogo
import com.example.ui.components.GlossyMenuButton
import com.example.ui.components.HeroPlayButton
import com.example.ui.components.StorefrontMiniIcon
import com.example.ui.components.TopBarShortcutTile
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnRim
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GreenBottom
import com.example.ui.theme.GreenRim
import com.example.ui.theme.GreenTop
import com.example.ui.theme.OrangeBottom
import com.example.ui.theme.OrangeRim
import com.example.ui.theme.OrangeTop
import com.example.ui.theme.WoodDark
import com.example.ui.theme.WoodLight

@Composable
fun HomeScreen(
    profile: PlayerProfileEntity,
    onPlayClick: () -> Unit,
    onOpenModal: (HomeModal, StoreTab?) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isCompactHeight = maxHeight < 680.dp
        val isUltraCompact = maxHeight < 560.dp

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = if (isUltraCompact) 4.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top HUD Bar (Coin Pill & Settings shortcut)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                CoinCurrencyPill(
                    amount = profile.coins,
                    onAddClick = { onOpenModal(HomeModal.STORE, StoreTab.TREASURE_VAULT) }
                )

                TopBarShortcutTile(
                    label = "Settings",
                    testTag = "top_settings_button",
                    onClick = { onOpenModal(HomeModal.SETTINGS, null) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(if (isCompactHeight) 2.dp else 8.dp))

            // 2. Hero Fruit Bliss Wooden Logo
            FruitBlissLogo(
                width = when {
                    isUltraCompact -> 184.dp
                    isCompactHeight -> 238.dp
                    else -> 276.dp
                },
                height = when {
                    isUltraCompact -> 80.dp
                    isCompactHeight -> 124.dp
                    else -> 148.dp
                }
            )

            // Current Level Pill
            Row(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xBB3E1F0D))
                    .border(1.5.dp, Color(0xFFFFCA28), RoundedCornerShape(50))
                    .testTag("level_select_chip")
                    .clickable { onOpenModal(HomeModal.LEVEL_SELECT, null) }
                    .padding(horizontal = 14.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = "Levels",
                    tint = GoldBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Level ${profile.highestUnlockedLevel}",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            // 3. Main Stage: Cute Cartoon Cat (Left) + Wooden Signpost Menu Stack (Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Cute Tabby Cat on Wooden Stump (Bottom-Left) with Equipped Skin
                CartoonCatCanvas(
                    skin = CatSkin.fromId(profile.equippedCatSkinId),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.50f)
                        .fillMaxHeight(0.92f)
                        .offset(x = (-6).dp, y = 4.dp)
                )

                // Wooden Signpost + Right Side Menu Stack
                val menuBtnHeight = if (isUltraCompact) 48.dp else 48.dp
                val storeBtnHeight = if (isUltraCompact) 48.dp else 52.dp
                val stackSpacing = if (isUltraCompact) 3.dp else 6.dp

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(0.55f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Vertical Wooden Signpost Beam behind the buttons
                    Canvas(
                        modifier = Modifier
                            .width(24.dp)
                            .height(if (isUltraCompact) 176.dp else 212.dp)
                    ) {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(WoodDark, WoodLight, WoodDark)),
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(stackSpacing),
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Store button with yellow accent rays on left
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                drawLine(
                                    Color(0xFFFFEE58),
                                    Offset(size.width * 0.1f, size.height * 0.25f),
                                    Offset(size.width * 0.8f, size.height * 0.42f),
                                    strokeWidth = 4f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    Color(0xFFFFEE58),
                                    Offset(0f, size.height * 0.55f),
                                    Offset(size.width * 0.78f, size.height * 0.55f),
                                    strokeWidth = 4f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    Color(0xFFFFEE58),
                                    Offset(size.width * 0.15f, size.height * 0.86f),
                                    Offset(size.width * 0.8f, size.height * 0.68f),
                                    strokeWidth = 4f,
                                    cap = StrokeCap.Round
                                )
                            }
                            Spacer(Modifier.width(2.dp))
                            GlossyMenuButton(
                                text = "Store",
                                topColor = OrangeTop,
                                bottomColor = OrangeBottom,
                                rimColor = OrangeRim,
                                testTag = "home_store_button",
                                height = storeBtnHeight,
                                fontSize = if (isUltraCompact) 19.sp else 24.sp,
                                onClick = { onOpenModal(HomeModal.STORE, StoreTab.TREASURE_VAULT) },
                                modifier = Modifier.weight(1f)
                            ) {
                                StorefrontMiniIcon(modifier = Modifier.size(26.dp))
                            }
                        }

                        GlossyMenuButton(
                            text = "THEME SHOP",
                            topColor = Color(0xFFF43F5E),
                            bottomColor = Color(0xFFBE123C),
                            rimColor = Color(0xFF881337),
                            testTag = "home_theme_shop_button",
                            height = menuBtnHeight,
                            fontSize = if (isUltraCompact) 13.sp else 15.sp,
                            onClick = { onOpenModal(HomeModal.THEME_SHOP, null) },
                            modifier = Modifier.fillMaxWidth(0.90f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = "THEME SHOP",
                                tint = Color(0xFFFFE082),
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        GlossyMenuButton(
                            text = "Profile",
                            topColor = BlueBtnTop,
                            bottomColor = BlueBtnBottom,
                            rimColor = BlueBtnRim,
                            testTag = "home_profile_button",
                            height = menuBtnHeight,
                            fontSize = if (isUltraCompact) 16.sp else 18.sp,
                            onClick = { onOpenModal(HomeModal.PROFILE, null) },
                            modifier = Modifier.fillMaxWidth(0.90f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        GlossyMenuButton(
                            text = "Settings",
                            topColor = GreenTop,
                            bottomColor = GreenBottom,
                            rimColor = GreenRim,
                            testTag = "home_settings_button",
                            height = menuBtnHeight,
                            fontSize = if (isUltraCompact) 16.sp else 18.sp,
                            onClick = { onOpenModal(HomeModal.SETTINGS, null) },
                            modifier = Modifier.fillMaxWidth(0.90f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 4. Large Green Hero Play Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isUltraCompact) 2.dp else 6.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroPlayButton(
                    onClick = onPlayClick,
                    height = if (isUltraCompact) 54.dp else 68.dp,
                    modifier = Modifier.fillMaxWidth(0.60f)
                )
                Spacer(Modifier.width(10.dp))
                // Compact Levels picker pill beside Play
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isUltraCompact) 50.dp else 54.dp)
                        .shadow(6.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(OrangeTop, OrangeBottom)))
                        .border(2.dp, Color(0xCCFFFFFF), RoundedCornerShape(50))
                        .testTag("home_levels_button")
                        .clickable { onOpenModal(HomeModal.LEVEL_SELECT, null) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Levels",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
