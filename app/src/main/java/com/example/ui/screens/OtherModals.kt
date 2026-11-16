package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.LevelProgressEntity
import com.example.data.PlayerProfileEntity
import com.example.model.FruitType
import com.example.model.StoreCatalog
import com.example.ui.components.CartoonCatCanvas
import com.example.ui.components.FruitTileArtwork
import com.example.ui.components.GlossyMenuButton
import com.example.ui.components.GoldCoinIcon
import com.example.ui.components.GoldStarIcon
import com.example.ui.components.OutlinedCartoonText
import com.example.ui.components.RedCloseCircleButton
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnRim
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.CardCream
import com.example.ui.theme.CardCreamBorder
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
import com.example.ui.theme.StorePurpleDark
import com.example.ui.theme.StorePurpleDeep
import com.example.ui.theme.StorePurpleMid
import com.example.ui.theme.TextBrownDark

@Composable
private fun BaseModalFrame(
    title: String,
    closeTestTag: String,
    onClose: () -> Unit,
    scrollable: Boolean = true,
    content: @Composable () -> Unit
) {
    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88140624))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .shadow(16.dp, RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(StorePurpleMid, StorePurpleDeep, StorePurpleDark)))
                    .border(3.dp, GoldBorder, RoundedCornerShape(26.dp))
                    .then(
                        if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
                    )
                    .padding(top = 34.dp, start = 14.dp, end = 14.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }

            // Title Plaque
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.verticalGradient(listOf(OrangeTop, OrangeBottom)))
                    .border(2.5.dp, Color(0xFFFFF59D), RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedCartoonText(
                    text = title,
                    fontSize = 22.sp,
                    fillBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF8E1))),
                    outlineColor = Color(0xFF5D2408),
                    strokeWidth = 5f
                )
            }

            RedCloseCircleButton(
                onClick = onClose,
                testTag = closeTestTag,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = 4.dp)
            )
        }
    }
}

@Composable
fun ProfileDialog(
    profile: PlayerProfileEntity,
    onSaveProfile: (String, Int) -> Unit,
    onClose: () -> Unit
) {
    var nameInput by remember(profile.playerName) { mutableStateOf(profile.playerName) }
    var selectedAvatar by remember(profile.avatarId) { mutableIntStateOf(profile.avatarId) }

    BaseModalFrame(
        title = "Profile",
        closeTestTag = "profile_close_button",
        onClose = onClose
    ) {
        AvatarBadge(avatarId = selectedAvatar, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (idx in 0..5) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (selectedAvatar == idx) 2.5.dp else 1.dp,
                            color = if (selectedAvatar == idx) GoldBright else Color.White,
                            shape = CircleShape
                        )
                        .clickable { selectedAvatar = idx }
                        .padding(3.dp)
                ) {
                    AvatarBadge(avatarId = idx, modifier = Modifier.fillMaxSize())
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = nameInput,
            onValueChange = { raw ->
                val sanitized = raw.replace("\n", "").replace("\r", "")
                if (sanitized.length <= 18) nameInput = sanitized
            },
            label = { Text("Player Name", fontFamily = NunitoFontFamily) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardCream)
                .testTag("profile_name_input")
        )

        Spacer(Modifier.height(10.dp))

        // Stats Summary Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardCream)
                .border(1.5.dp, CardCreamBorder, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProfileStatRow("Virtual Coins", profile.coins.toString())
            ProfileStatRow("Total Stars", profile.stars.toString())
            ProfileStatRow("Highest Level", "Level ${profile.highestUnlockedLevel}")
            ProfileStatRow("Best Level Score", profile.bestLevelScore.toString())
            ProfileStatRow("Total Score", profile.totalScore.toString())
            ProfileStatRow("Games Won", "${profile.gamesWon} / ${profile.gamesPlayed}")
        }

        Spacer(Modifier.height(12.dp))
        GlossyMenuButton(
            text = "Save Profile",
            topColor = GreenTop,
            bottomColor = GreenBottom,
            rimColor = GreenRim,
            testTag = "save_profile_button",
            onClick = {
                onSaveProfile(nameInput, selectedAvatar)
                onClose()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextBrownDark.copy(alpha = 0.78f)
        )
        Text(
            text = value,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextBrownDark
        )
    }
}

@Composable
fun SettingsDialog(
    profile: PlayerProfileEntity,
    onUpdateSettings: (sound: Boolean, haptics: Boolean, vibration: Boolean) -> Unit,
    onResetProgress: () -> Unit,
    onClose: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    BaseModalFrame(
        title = "Settings",
        closeTestTag = "settings_close_button",
        onClose = onClose
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardCream)
                .border(2.dp, CardCreamBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingsToggleRow(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                title = "Sound Effects",
                subtitle = "Clicks, fruit pops, power-ups & rewards",
                checked = profile.soundEnabled,
                testTag = "settings_sound_switch",
                onCheckedChange = {
                    onUpdateSettings(it, profile.hapticsEnabled, profile.vibrationEnabled)
                }
            )
            SettingsToggleRow(
                icon = Icons.Rounded.Vibration,
                title = "Vibration",
                subtitle = "Vibrate on verified purchases",
                checked = profile.vibrationEnabled,
                testTag = "settings_vibration_switch",
                onCheckedChange = {
                    onUpdateSettings(profile.soundEnabled, profile.hapticsEnabled, it)
                }
            )
            SettingsToggleRow(
                icon = Icons.Rounded.Vibration,
                title = "Vibration Feedback",
                subtitle = "Haptic pop on combos",
                checked = profile.hapticsEnabled,
                testTag = "settings_haptics_switch",
                onCheckedChange = {
                    onUpdateSettings(profile.soundEnabled, it, profile.vibrationEnabled)
                }
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "Background Music: OFF  •  Offline Mode Active",
            fontFamily = NunitoFontFamily,
            fontSize = 12.sp,
            color = Color(0xFFFFE082),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))

        GlossyMenuButton(
            text = "Privacy Policy & Data",
            topColor = BlueBtnTop,
            bottomColor = BlueBtnBottom,
            rimColor = BlueBtnRim,
            testTag = "settings_privacy_button",
            height = 48.dp,
            fontSize = 15.sp,
            onClick = { showPrivacyPolicy = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Policy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.height(10.dp))

        GlossyMenuButton(
            text = "Reset Save Data",
            topColor = OrangeTop,
            bottomColor = OrangeBottom,
            rimColor = OrangeRim,
            testTag = "settings_reset_button",
            height = 48.dp,
            fontSize = 15.sp,
            onClick = { confirmReset = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(onClose = { showPrivacyPolicy = false })
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = CardCream,
            title = { Text("Reset Progress?", fontFamily = FredokaFontFamily, color = TextBrownDark) },
            text = {
                Text(
                    "This resets coins (0), stars (0), and level progress.",
                    fontFamily = NunitoFontFamily,
                    color = TextBrownDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmReset = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset", fontFamily = FredokaFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Cancel", fontFamily = FredokaFontFamily, color = TextBrownDark)
                }
            }
        )
    }
}

@Composable
private fun PrivacyPolicyDialog(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val policyUrl = BuildConfig.PRIVACY_POLICY_URL.trim()
    val hasExternalPolicy = policyUrl.startsWith("https://", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = CardCream,
        title = {
            Text(
                text = "Privacy Policy & Data Safety",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextBrownDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .testTag("privacy_policy_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Local Game Storage: Your nickname, avatar choice, coin and star balances, boosts, and level scores are stored locally on your device. You can reset progress anytime via Settings -> Reset Save Data.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = TextBrownDark
                )
                Text(
                    text = "In-App Purchases: Optional Coin Packs and Boosts are processed securely through Google Play Billing. Fruit Bliss never collects credit card details and records only Google Play purchase tokens locally to verify rewards and prevent duplicate grants.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = TextBrownDark
                )
                Text(
                    text = "No Ads or Sensitive Data: Fruit Bliss contains no third-party advertising SDKs and does not access your location, contacts, photos, camera, or microphone.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = TextBrownDark
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = GreenBottom),
                modifier = Modifier.testTag("privacy_policy_close_button")
            ) {
                Text("Close", fontFamily = FredokaFontFamily)
            }
        },
        dismissButton = if (hasExternalPolicy) {
            {
                TextButton(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl)))
                        }
                    }
                ) {
                    Text("Full Policy", fontFamily = FredokaFontFamily, color = TextBrownDark)
                }
            }
        } else null
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = StorePurpleMid,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextBrownDark
            )
            Text(
                text = subtitle,
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                color = TextBrownDark.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = GreenBottom),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun LevelSelectDialog(
    highestUnlockedLevel: Int,
    levelProgress: List<LevelProgressEntity>,
    onSelectLevel: (Int) -> Unit,
    onClose: () -> Unit
) {
    val progressMap = levelProgress.associateBy { it.levelNumber }

    BaseModalFrame(
        title = "Select Level",
        closeTestTag = "level_select_close_button",
        onClose = onClose,
        scrollable = false
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 320.dp)
                .testTag("level_select_grid")
        ) {
            items(StoreCatalog.levels, key = { it.levelNumber }) { cfg ->
                val progress = progressMap[cfg.levelNumber]
                val unlocked = cfg.levelNumber <= highestUnlockedLevel || progress?.unlocked == true
                val stars = progress?.starsEarned ?: 0

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (unlocked) CardCream else Color(0xFF47126B))
                        .border(
                            width = 2.dp,
                            color = if (unlocked) GoldBorder else Color(0xFF7B2CBF),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("level_tile_${cfg.levelNumber}")
                        .clickable(enabled = unlocked) { onSelectLevel(cfg.levelNumber) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (unlocked) {
                        Text(
                            text = cfg.levelNumber.toString(),
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextBrownDark
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (s in 1..3) {
                                Box(modifier = Modifier.size(12.dp)) {
                                    if (s <= stars) {
                                        GoldStarIcon(modifier = Modifier.fillMaxSize())
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color(0x33000000))
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xAAFFFFFF),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = cfg.levelNumber.toString(),
                            fontFamily = FredokaFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xAAFFFFFF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarBadge(avatarId: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(CardCream)
            .border(2.dp, GoldBorder, CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (avatarId == 0) {
            CartoonCatCanvas(modifier = Modifier.fillMaxSize())
        } else {
            val fruit = FruitType.entries[(avatarId - 1).coerceIn(0, FruitType.entries.lastIndex)]
            FruitTileArtwork(fruit = fruit, modifier = Modifier.fillMaxSize())
        }
    }
}
