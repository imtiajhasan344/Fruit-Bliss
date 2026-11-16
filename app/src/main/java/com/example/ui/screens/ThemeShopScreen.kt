package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.model.BackgroundTheme
import com.example.model.BoardTheme
import com.example.model.CatSkin
import com.example.model.FruitSkin
import com.example.model.FruitType
import com.example.model.ThemeCategory
import com.example.model.ThemeShopCatalog
import com.example.model.ThemeShopItem
import com.example.ui.components.BackgroundThemePreview
import com.example.ui.components.BoardThemePreview
import com.example.ui.components.CartoonCatCanvas
import com.example.ui.components.FruitTileArtwork
import com.example.ui.components.GlossyMenuButton
import com.example.ui.components.GoldCoinIcon
import com.example.ui.components.OutlinedCartoonText
import com.example.ui.components.RedCloseCircleButton
import com.example.ui.theme.AwningDark
import com.example.ui.theme.AwningLight
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
import com.example.ui.theme.OrangeTop
import com.example.ui.theme.StorePurpleDark
import com.example.ui.theme.StorePurpleDeep
import com.example.ui.theme.StorePurpleMid
import com.example.ui.theme.TextBrownDark
import com.example.ui.theme.WoodDark
import com.example.ui.theme.WoodLight

@Composable
fun ThemeShopScreen(
    profile: PlayerProfileEntity,
    ownedThemeIds: Set<String>,
    selectedCategory: ThemeCategory?,
    pendingPurchase: ThemeShopItem?,
    errorMessage: String?,
    hasPurchaseSuccess: Boolean,
    onSelectCategory: (ThemeCategory?) -> Unit,
    onRequestPurchase: (ThemeShopItem) -> Unit,
    onConfirmPurchase: () -> Unit,
    onCancelPurchase: () -> Unit,
    onEquipItem: (ThemeShopItem) -> Unit,
    onResetDefaultBackground: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler(
        enabled = pendingPurchase == null && !hasPurchaseSuccess,
        onBack = onClose
    )

    val categoriesToDisplay = if (selectedCategory == null) {
        ThemeCategory.entries
    } else {
        listOf(selectedCategory)
    }

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
            .padding(horizontal = 10.dp, vertical = 12.dp)
            .testTag("theme_shop_screen"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 26.dp)
                    .shadow(16.dp, RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(StorePurpleMid, StorePurpleDeep, StorePurpleDark)
                        )
                    )
                    .border(3.dp, GoldBorder, RoundedCornerShape(26.dp))
            ) {
                ThemeShopAwningHeader()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Current Coin Balance Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x991F0638))
                            .border(1.5.dp, Color(0x88FFB703), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoldCoinIcon(modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Balance : ${"%,d".format(profile.coins)} Virtual Coins",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFFFE082),
                            modifier = Modifier.testTag("theme_shop_balance_text")
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Category Filter Strip
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            ThemeCategoryChip(
                                label = "All",
                                selected = selectedCategory == null,
                                testTag = "theme_filter_all",
                                onClick = { onSelectCategory(null) }
                            )
                        }
                        items(ThemeCategory.entries) { category ->
                            val shortLabel = when (category) {
                                ThemeCategory.BACKGROUND -> "Backgrounds"
                                ThemeCategory.CAT_SKIN -> "Cat Skins"
                                ThemeCategory.BOARD -> "Boards"
                                ThemeCategory.FRUIT_SKIN -> "Fruit Skins"
                            }
                            ThemeCategoryChip(
                                label = shortLabel,
                                selected = selectedCategory == category,
                                testTag = "theme_filter_${category.name}",
                                onClick = { onSelectCategory(category) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Scrollable Theme Shop Sections & Cards
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("theme_shop_list")
                    ) {
                        categoriesToDisplay.forEach { category ->
                            val categoryItems = ThemeShopCatalog.itemsForCategory(category)
                            val equippedId = when (category) {
                                ThemeCategory.BACKGROUND -> profile.equippedBackgroundId
                                ThemeCategory.CAT_SKIN -> profile.equippedCatSkinId
                                ThemeCategory.BOARD -> profile.equippedBoardThemeId
                                ThemeCategory.FRUIT_SKIN -> profile.equippedFruitSkinId
                            }

                            item(key = "header_${category.name}") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.title,
                                        fontFamily = FredokaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = GoldBright,
                                        modifier = Modifier.testTag("theme_section_${category.name}")
                                    )
                                    if (category == ThemeCategory.BACKGROUND &&
                                        profile.equippedBackgroundId != BackgroundTheme.CLASSIC_MEADOW.id
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 32.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Color(0xFF6D28D9))
                                                .border(1.dp, GoldBorder, RoundedCornerShape(50))
                                                .testTag("reset_default_background_button")
                                                .clickable(role = Role.Button, onClick = onResetDefaultBackground)
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = "Classic Meadow",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Classic Meadow",
                                                fontFamily = FredokaFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            val rows = categoryItems.chunked(2)
                            items(
                                items = rows,
                                key = { row -> "${category.name}_${row.joinToString("_") { it.id }}" }
                            ) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        val isOwned = item.id in ownedThemeIds
                                        val isEquipped = equippedId == item.id
                                        ThemeShopItemCard(
                                            item = item,
                                            isOwned = isOwned,
                                            isEquipped = isEquipped,
                                            onBuyClick = { onRequestPurchase(item) },
                                            onEquipClick = { onEquipItem(item) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Wooden Signboard Floating on Top Center
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(214.dp)
                    .height(50.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(WoodLight, WoodDark)))
                    .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = Color(0xFFFFE082),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    OutlinedCartoonText(
                        text = "Theme Shop",
                        fontSize = 23.sp,
                        fillBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFFE082))),
                        outlineColor = Color(0xFF5D2408),
                        strokeWidth = 6f
                    )
                }
            }

            RedCloseCircleButton(
                onClick = onClose,
                testTag = "theme_shop_close_button",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = 12.dp)
            )
        }

        // Virtual Coin Purchase Confirmation Popup
        if (pendingPurchase != null) {
            ThemePurchaseConfirmationDialog(
                item = pendingPurchase,
                currentCoins = profile.coins,
                errorMessage = errorMessage,
                onConfirm = onConfirmPurchase,
                onCancel = onCancelPurchase
            )
        }
    }
}

@Composable
private fun ThemeCategoryChip(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bgBrush = if (selected) {
        Brush.verticalGradient(listOf(GoldBright, OrangeTop, OrangeBottom))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF7B2CBF), Color(0xFF4C1D95)))
    }
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(RoundedCornerShape(50))
            .background(bgBrush)
            .border(
                width = 1.8.dp,
                color = if (selected) Color(0xFFFFF59D) else Color(0xFF9D4EDD),
                shape = RoundedCornerShape(50)
            )
            .testTag(testTag)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (selected) Color(0xFF4E1A00) else Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun ThemeShopItemCard(
    item: ThemeShopItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    onBuyClick: () -> Unit,
    onEquipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isEquipped) Color(0xFFFFF4D2) else CardCream)
            .border(
                width = if (isEquipped) 2.5.dp else 1.8.dp,
                color = if (isEquipped) GreenBottom else CardCreamBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("theme_card_${item.id}")
            .clickable {
                if (isOwned) {
                    if (!isEquipped) onEquipClick()
                } else {
                    onBuyClick()
                }
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2B0A47))
                .border(1.5.dp, GoldBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            ThemeItemPreviewArtwork(
                item = item,
                modifier = Modifier.fillMaxSize()
            )

            if (isOwned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isEquipped) GreenBottom else Color(0xFF6D28D9))
                        .border(1.dp, Color.White, RoundedCornerShape(50))
                        .testTag("theme_owned_badge_${item.id}")
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isEquipped) "OWNED • ACTIVE" else "OWNED",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = item.title,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextBrownDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (item.priceCoins == 0) "Free" else "${"%,d".format(item.priceCoins)} Coins",
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFFB45309),
            maxLines = 1
        )

        Spacer(Modifier.height(6.dp))

        when {
            isEquipped -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(Color(0xFF22C55E), GreenBottom)))
                        .border(1.5.dp, Color.White, RoundedCornerShape(50))
                        .semantics(mergeDescendants = true) {}
                        .testTag("theme_equipped_${item.id}")
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Equipped",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "EQUIPPED",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            isOwned -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 40.dp)
                        .shadow(3.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(BlueBtnRim)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom)))
                        .border(1.5.dp, Color(0xCCFFFFFF), RoundedCornerShape(50))
                        .testTag("theme_equip_${item.id}")
                        .clickable(role = Role.Button, onClick = onEquipClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EQUIP",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 40.dp)
                        .shadow(3.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(GreenRim)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(GreenTop, GreenBottom)))
                        .border(1.5.dp, Color(0xCCFFFFFF), RoundedCornerShape(50))
                        .testTag("theme_buy_${item.id}")
                        .clickable(role = Role.Button, onClick = onBuyClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoldCoinIcon(modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "%,d".format(item.priceCoins),
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeItemPreviewArtwork(
    item: ThemeShopItem,
    modifier: Modifier = Modifier
) {
    when (item.category) {
        ThemeCategory.BACKGROUND -> {
            BackgroundThemePreview(
                theme = BackgroundTheme.fromId(item.id),
                modifier = modifier
            )
        }
        ThemeCategory.CAT_SKIN -> {
            Box(
                modifier = modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF4CC9F0), Color(0xFF83E544))
                        )
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                CartoonCatCanvas(
                    skin = CatSkin.fromId(item.id),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        ThemeCategory.BOARD -> {
            Box(
                modifier = modifier
                    .background(Color(0xFF2B0A47))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                BoardThemePreview(
                    theme = BoardTheme.fromId(item.id),
                    modifier = Modifier.size(76.dp)
                )
            }
        }
        ThemeCategory.FRUIT_SKIN -> {
            val skin = FruitSkin.fromId(item.id)
            Box(
                modifier = modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5A189A), Color(0xFF240046))
                        )
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FruitTileArtwork(fruit = FruitType.APPLE, skin = skin, modifier = Modifier.size(34.dp))
                        FruitTileArtwork(fruit = FruitType.ORANGE, skin = skin, modifier = Modifier.size(34.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FruitTileArtwork(fruit = FruitType.STRAWBERRY, skin = skin, modifier = Modifier.size(34.dp))
                        FruitTileArtwork(fruit = FruitType.BANANA, skin = skin, modifier = Modifier.size(34.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePurchaseConfirmationDialog(
    item: ThemeShopItem,
    currentCoins: Int,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(onBack = onCancel)

    val canAfford = currentCoins >= item.priceCoins
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
            .testTag("theme_confirm_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFCF3), CardCream)))
                .border(3.5.dp, GoldBorder, RoundedCornerShape(26.dp))
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedCartoonText(
                text = "Confirm Purchase",
                fontSize = 24.sp,
                fillBrush = Brush.verticalGradient(listOf(Color(0xFFFFF59D), GoldBright, OrangeTop)),
                outlineColor = Color(0xFF4A1800),
                strokeWidth = 6f
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(2.5.dp, GoldBorder, RoundedCornerShape(18.dp))
            ) {
                ThemeItemPreviewArtwork(item = item, modifier = Modifier.fillMaxSize())
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = item.title,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextBrownDark
            )
            Text(
                text = item.category.title,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF7B2CBF)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.description,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextBrownDark.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF3C096C))
                    .border(1.5.dp, GoldBright, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldCoinIcon(modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Price: ${"%,d".format(item.priceCoins)} Coins",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFFFE082)
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your Balance: ${"%,d".format(currentCoins)} Virtual Coins",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (canAfford) Color(0xFF15803D) else Color(0xFFB91C1C)
            )

            val displayError = errorMessage ?: if (!canAfford) {
                "Not enough Virtual Coins! Need ${"%,d".format(item.priceCoins - currentCoins)} more."
            } else null

            if (displayError != null) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFB91C1C))
                        .testTag("theme_confirm_error")
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayError,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlossyMenuButton(
                    text = "Cancel",
                    topColor = Color(0xFF94A3B8),
                    bottomColor = Color(0xFF64748B),
                    rimColor = Color(0xFF475569),
                    testTag = "theme_confirm_cancel_button",
                    height = 50.dp,
                    fontSize = 15.sp,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {}

                GlossyMenuButton(
                    text = "Buy Now",
                    topColor = GreenTop,
                    bottomColor = GreenBottom,
                    rimColor = GreenRim,
                    testTag = "theme_confirm_buy_button",
                    height = 50.dp,
                    fontSize = 15.sp,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    GoldCoinIcon(modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeShopAwningHeader() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val stripes = 11
        val stripeW = size.width / stripes
        for (i in 0 until stripes) {
            val col = if (i % 2 == 0) AwningDark else AwningLight
            drawRect(
                color = col,
                topLeft = Offset(i * stripeW, 0f),
                size = Size(stripeW + 1f, size.height * 0.72f)
            )
            drawCircle(
                color = col,
                radius = stripeW * 0.52f,
                center = Offset(i * stripeW + stripeW * 0.5f, size.height * 0.70f)
            )
        }
    }
}
