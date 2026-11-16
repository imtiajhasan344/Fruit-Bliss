package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Inventory2
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.billing.DemoPurchaseOutcome
import com.example.model.BoostType
import com.example.model.CoinPack
import com.example.model.StoreCatalog
import com.example.model.StoreTab
import com.example.ui.components.BoostIconArtwork
import com.example.ui.components.CoinPackArtwork
import com.example.ui.components.GlossyMenuButton
import com.example.ui.components.GoldCoinIcon
import com.example.ui.components.GreenPriceButton
import com.example.ui.components.OutlinedCartoonText
import com.example.ui.components.RedCloseCircleButton
import com.example.ui.theme.AwningDark
import com.example.ui.theme.AwningLight
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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun StoreDialog(
    coins: Int,
    selectedTab: StoreTab,
    boostInventory: Map<BoostType, Int>,
    pendingCoinPack: CoinPack?,
    pendingBoostPurchase: BoostType?,
    playFormattedPrices: Map<String, String> = emptyMap(),
    billingInProgress: Boolean = false,
    purchaseErrorMessage: String? = null,
    hasPurchaseSuccess: Boolean = false,
    feedbackMessage: String?,
    isDemoPurchaseMode: Boolean = false,
    onSelectTab: (StoreTab) -> Unit,
    onRequestCoinPack: (CoinPack) -> Unit,
    onRequestBoost: (BoostType) -> Unit,
    onLaunchBilling: (Activity?) -> Unit,
    onDemoOutcome: (DemoPurchaseOutcome) -> Unit = {},
    onDismissPending: () -> Unit,
    onDismissFeedback: () -> Unit,
    onClose: () -> Unit
) {
    val showDemoModeUi = isDemoPurchaseMode &&
        BuildConfig.DEBUG &&
        BuildConfig.DEMO_PURCHASES_ENABLED

    BackHandler(
        enabled = pendingCoinPack == null && pendingBoostPurchase == null && !hasPurchaseSuccess,
        onBack = onClose
    )

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
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Main Royal Purple Store Container
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
                // Striped Purple Awning Header
                StoreAwningHeader()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Virtual Coin Balance Pill
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
                            text = "Balance : $coins Virtual Coins",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFFFE082),
                            modifier = Modifier.testTag("store_balance_text")
                        )
                    }

                    if (showDemoModeUi) {
                        Spacer(Modifier.height(6.dp))
                        DemoTestModeBanner(
                            testTag = "store_test_mode_banner",
                            subtitle = null
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Tab Selector (Treasure Vault vs Boost Forge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StoreTabButton(
                            title = "Treasure Vault",
                            selected = selectedTab == StoreTab.TREASURE_VAULT,
                            testTag = "tab_treasure_vault",
                            onClick = { onSelectTab(StoreTab.TREASURE_VAULT) },
                            modifier = Modifier.weight(1f),
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Inventory2,
                                    contentDescription = "Treasure Vault",
                                    tint = if (selectedTab == StoreTab.TREASURE_VAULT) Color(0xFF9C2D00) else Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        StoreTabButton(
                            title = "Boost Forge",
                            selected = selectedTab == StoreTab.BOOST_FORGE,
                            testTag = "tab_boost_forge",
                            onClick = { onSelectTab(StoreTab.BOOST_FORGE) },
                            modifier = Modifier.weight(1f),
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = "Boost Forge",
                                    tint = if (selectedTab == StoreTab.BOOST_FORGE) Color(0xFF9C2D00) else Color(0xFFFFD54F),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )
                    }

                    if (feedbackMessage != null) {
                        val isErrorFeedback =
                            feedbackMessage.contains("unavailable", ignoreCase = true) ||
                                feedbackMessage.contains("failed", ignoreCase = true) ||
                                feedbackMessage.contains("error", ignoreCase = true) ||
                                feedbackMessage.contains("Not enough", ignoreCase = true) ||
                                feedbackMessage.contains("Unable", ignoreCase = true)
                        val isPendingFeedback =
                            feedbackMessage.contains("pending", ignoreCase = true)
                        val bannerBg = when {
                            isErrorFeedback -> Color(0xCCB91C1C)
                            isPendingFeedback -> Color(0xCCB45309)
                            else -> Color(0xCC15803D)
                        }
                        val bannerBorder = when {
                            isErrorFeedback -> Color(0xFFFCA5A5)
                            isPendingFeedback -> Color(0xFFFDE68A)
                            else -> Color(0xFF86EFAC)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bannerBg)
                                .border(
                                    1.dp,
                                    bannerBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .testTag("store_feedback_banner")
                                .clickable { onDismissFeedback() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = feedbackMessage,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Virtual Coins Disclaimer above Store items
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x661F0638))
                            .border(1.dp, Color(0x55FFB703), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("store_virtual_coins_disclaimer")
                    ) {
                        Text(
                            text = "Disclaimer: All coins are virtual and for in-game use only. They have no real-world value and cannot be withdrawn or exchanged for real money.",
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = Color(0xFFFFF3CC),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Content Grid
                    if (selectedTab == StoreTab.TREASURE_VAULT) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("treasure_vault_grid")
                        ) {
                            items(StoreCatalog.coinPacks, key = { it.id }) { pack ->
                                CoinPackCard(
                                    pack = pack,
                                    displayPrice = playFormattedPrices[pack.productId] ?: pack.priceText,
                                    onClick = { onRequestCoinPack(pack) }
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("boost_forge_grid")
                        ) {
                            items(StoreCatalog.boostItems, key = { it.id }) { boost ->
                                BoostForgeCard(
                                    boost = boost,
                                    displayPrice = playFormattedPrices[boost.productId] ?: boost.priceText,
                                    ownedCount = boostInventory[boost] ?: 0,
                                    onClick = { onRequestBoost(boost) }
                                )
                            }
                        }
                    }
                }
            }

            // Wooden "Store" Signboard Floating on Top Center
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(176.dp)
                    .height(50.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(WoodLight, WoodDark)))
                    .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                OutlinedCartoonText(
                    text = "Store",
                    fontSize = 28.sp,
                    fillBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFFE082))),
                    outlineColor = Color(0xFF5D2408),
                    strokeWidth = 6f
                )
            }

            // Glossy Red X Close Button Top-Right
            RedCloseCircleButton(
                onClick = onClose,
                testTag = "store_close_button",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = 12.dp)
            )
        }
    }

    // Full-Screen Coin Pack Purchase Page
    if (pendingCoinPack != null) {
        CoinPackPurchaseScreen(
            pack = pendingCoinPack,
            displayPrice = playFormattedPrices[pendingCoinPack.productId] ?: pendingCoinPack.priceText,
            currentCoins = coins,
            billingInProgress = billingInProgress,
            errorMessage = purchaseErrorMessage,
            showDemoModeUi = showDemoModeUi,
            onBack = onDismissPending,
            onBuyCoins = onLaunchBilling,
            onDemoOutcome = onDemoOutcome
        )
    }

    // Full-Screen Boost Forge Purchase Page
    if (pendingBoostPurchase != null) {
        BoostPurchaseScreen(
            boost = pendingBoostPurchase,
            displayPrice = playFormattedPrices[pendingBoostPurchase.productId] ?: pendingBoostPurchase.priceText,
            ownedCount = boostInventory[pendingBoostPurchase] ?: 0,
            currentCoins = coins,
            billingInProgress = billingInProgress,
            errorMessage = purchaseErrorMessage,
            showDemoModeUi = showDemoModeUi,
            onBack = onDismissPending,
            onBuyBoost = onLaunchBilling,
            onDemoOutcome = onDemoOutcome
        )
    }
}

@Composable
private fun StoreAwningHeader() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
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
            // Scallop bottom
            drawCircle(
                color = col,
                radius = stripeW * 0.52f,
                center = Offset(i * stripeW + stripeW * 0.5f, size.height * 0.70f)
            )
        }
    }
}

@Composable
private fun StoreTabButton(
    title: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val bgBrush = if (selected) {
        Brush.verticalGradient(listOf(GoldBright, OrangeTop, OrangeBottom))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF7B2CBF), Color(0xFF4C1D95)))
    }
    Row(
        modifier = modifier
            .height(44.dp)
            .shadow(if (selected) 5.dp else 1.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(bgBrush)
            .border(
                width = 2.dp,
                color = if (selected) Color(0xFFFFF59D) else Color(0xFF9D4EDD),
                shape = RoundedCornerShape(50)
            )
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (selected) Color(0xFF4E1A00) else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CoinPackCard(
    pack: CoinPack,
    displayPrice: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(CardCream)
            .border(2.dp, CardCreamBorder, RoundedCornerShape(14.dp))
            .testTag("coin_pack_${pack.id}")
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Coin Pack",
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = TextBrownDark,
            maxLines = 1
        )
        Text(
            text = pack.title,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFB45309),
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        CoinPackArtwork(
            tier = pack.tier,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(6.dp))
        GreenPriceButton(
            priceText = displayPrice,
            testTag = "buy_coin_pack_${pack.id}",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BoostForgeCard(
    boost: BoostType,
    displayPrice: String,
    ownedCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(CardCream)
            .border(1.8.dp, CardCreamBorder, RoundedCornerShape(12.dp))
            .testTag("boost_card_${boost.id}")
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = boost.title,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                color = TextBrownDark,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(24.dp)
            )
            Spacer(Modifier.height(2.dp))
            BoostIconArtwork(
                boost = boost,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(5.dp))
            GreenPriceButton(
                priceText = displayPrice,
                testTag = "buy_boost_${boost.id}",
                onClick = onClick,
                height = 28.dp,
                fontSize = 11.sp,
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (ownedCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF7B2CBF))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "x$ownedCount",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CoinPackPurchaseScreen(
    pack: CoinPack,
    displayPrice: String,
    currentCoins: Int,
    billingInProgress: Boolean,
    errorMessage: String?,
    showDemoModeUi: Boolean,
    onBack: () -> Unit,
    onBuyCoins: (Activity?) -> Unit,
    onDemoOutcome: (DemoPurchaseOutcome) -> Unit
) {
    BackHandler(enabled = !billingInProgress, onBack = onBack)

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(StorePurpleMid, StorePurpleDeep, StorePurpleDark)
                )
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .testTag("coin_pack_purchase_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Striped Store Awning Accent across the top
            StoreAwningHeader()

            // Top Navigation Row with Left-Arrow Back Button & Current Balance Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(OrangeTop, OrangeBottom)))
                        .border(2.dp, Color(0xFFFFF59D), CircleShape)
                        .testTag("coin_purchase_back_button")
                        .clickable {
                            if (!billingInProgress) onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to Store",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x991F0638))
                        .border(1.5.dp, Color(0x88FFB703), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoldCoinIcon(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$currentCoins Coins",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFFFE082)
                    )
                }
            }

            // Center Scrollable Content Area (Adapts cleanly across all mobile screen heights)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(WoodLight, WoodDark)))
                        .border(2.dp, GoldBright, RoundedCornerShape(50))
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Treasure Vault Pack",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFFFF8E1)
                    )
                }

                if (showDemoModeUi) {
                    Spacer(Modifier.height(10.dp))
                    DemoTestModeBanner(
                        testTag = "purchase_test_mode_banner",
                        subtitle = "Demo purchase only — not a real Google Play transaction."
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Prominent Center Coin Pack Artwork Showcase
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .shadow(14.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFF8E1), Color(0xFFFFE082), Color(0xFFE7C78C))
                            )
                        )
                        .border(4.dp, GoldBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CoinPackArtwork(
                        tier = pack.tier,
                        modifier = Modifier.size(150.dp)
                    )
                }

                Spacer(Modifier.height(22.dp))

                // Clear Selected Coin Amount
                OutlinedCartoonText(
                    text = pack.title,
                    fontSize = 36.sp,
                    fillBrush = Brush.verticalGradient(
                        listOf(Color(0xFFFFF59D), GoldBright, OrangeTop)
                    ),
                    outlineColor = Color(0xFF3E1200),
                    strokeWidth = 8f
                )

                Spacer(Modifier.height(10.dp))

                // Exact Selected Pack Price Pill
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(CardCream)
                        .border(2.5.dp, GoldBorder, RoundedCornerShape(50))
                        .padding(horizontal = 26.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = displayPrice,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = GreenBottom,
                        modifier = Modifier.testTag("coin_purchase_price_text")
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Short Description Below the Price
                Text(
                    text = "Add ${pack.title} directly to your Fruit Bliss virtual coin balance to unlock power-ups in Boost Forge.",
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFFFFF8E7),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xCCB91C1C))
                            .border(1.5.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                            .testTag("purchase_error_banner")
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (showDemoModeUi) {
                DemoOutcomeControlsRow(
                    enabled = !billingInProgress,
                    onTestCancelled = { onDemoOutcome(DemoPurchaseOutcome.CANCELLED) },
                    onTestFailed = { onDemoOutcome(DemoPurchaseOutcome.FAILED) }
                )
            }

            // Single Large Green "BUY COINS" Button at Bottom Center (No Cancel Button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                GlossyMenuButton(
                    text = if (billingInProgress) {
                        if (showDemoModeUi) "PROCESSING..." else "OPENING PLAY..."
                    } else {
                        "BUY COINS"
                    },
                    topColor = GreenTop,
                    bottomColor = GreenBottom,
                    rimColor = GreenRim,
                    testTag = "confirm_purchase_button",
                    height = 64.dp,
                    fontSize = 24.sp,
                    onClick = {
                        if (!billingInProgress) {
                            onBuyCoins(context.findActivity())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoldCoinIcon(modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

@Composable
private fun BoostPurchaseScreen(
    boost: BoostType,
    displayPrice: String,
    ownedCount: Int,
    currentCoins: Int,
    billingInProgress: Boolean,
    errorMessage: String?,
    showDemoModeUi: Boolean,
    onBack: () -> Unit,
    onBuyBoost: (Activity?) -> Unit,
    onDemoOutcome: (DemoPurchaseOutcome) -> Unit
) {
    BackHandler(enabled = !billingInProgress, onBack = onBack)

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(StorePurpleMid, StorePurpleDeep, StorePurpleDark)
                )
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .testTag("boost_purchase_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Striped Store Awning Accent across the top
            StoreAwningHeader()

            // Top Navigation Row with Left-Arrow Back Button & Current Balance Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(OrangeTop, OrangeBottom)))
                        .border(2.dp, Color(0xFFFFF59D), CircleShape)
                        .testTag("boost_purchase_back_button")
                        .clickable {
                            if (!billingInProgress) onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to Boost Forge",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x991F0638))
                        .border(1.5.dp, Color(0x88FFB703), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoldCoinIcon(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$currentCoins Coins",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFFFE082)
                    )
                }
            }

            // Center Scrollable Content Area (Adapts cleanly across all mobile screen heights)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(WoodLight, WoodDark)))
                        .border(2.dp, GoldBright, RoundedCornerShape(50))
                        .padding(horizontal = 22.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (ownedCount > 0) "Boost Forge  •  Owned: x$ownedCount" else "Boost Forge Power-Up",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFFFF8E1)
                    )
                }

                if (showDemoModeUi) {
                    Spacer(Modifier.height(10.dp))
                    DemoTestModeBanner(
                        testTag = "purchase_test_mode_banner",
                        subtitle = "Demo purchase only — not a real Google Play transaction."
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Prominent Center Boost Icon Showcase
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .shadow(14.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFF8E1), Color(0xFFFFE082), Color(0xFFE7C78C))
                            )
                        )
                        .border(4.dp, GoldBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    BoostIconArtwork(
                        boost = boost,
                        modifier = Modifier.size(144.dp)
                    )
                }

                Spacer(Modifier.height(22.dp))

                // Clear Selected Boost Name
                OutlinedCartoonText(
                    text = boost.title,
                    fontSize = 32.sp,
                    fillBrush = Brush.verticalGradient(
                        listOf(Color(0xFFFFF59D), GoldBright, OrangeTop)
                    ),
                    outlineColor = Color(0xFF3E1200),
                    strokeWidth = 8f
                )

                Spacer(Modifier.height(10.dp))

                // Exact Selected Boost Price Pill
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(CardCream)
                        .border(2.5.dp, GoldBorder, RoundedCornerShape(50))
                        .padding(horizontal = 26.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = displayPrice,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = GreenBottom,
                        modifier = Modifier.testTag("boost_purchase_price_text")
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Short Description Below the Price
                Text(
                    text = boost.description,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFFFFF8E7),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xCCB91C1C))
                            .border(1.5.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                            .testTag("purchase_error_banner")
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (showDemoModeUi) {
                DemoOutcomeControlsRow(
                    enabled = !billingInProgress,
                    onTestCancelled = { onDemoOutcome(DemoPurchaseOutcome.CANCELLED) },
                    onTestFailed = { onDemoOutcome(DemoPurchaseOutcome.FAILED) }
                )
            }

            // Single Large Green "BUY" Button at Bottom Center (No Cancel Button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                GlossyMenuButton(
                    text = if (billingInProgress) {
                        if (showDemoModeUi) "PROCESSING..." else "OPENING PLAY..."
                    } else {
                        "BUY"
                    },
                    topColor = GreenTop,
                    bottomColor = GreenBottom,
                    rimColor = GreenRim,
                    testTag = "confirm_purchase_button",
                    height = 64.dp,
                    fontSize = 24.sp,
                    onClick = {
                        if (!billingInProgress) {
                            onBuyBoost(context.findActivity())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoTestModeBanner(
    testTag: String,
    subtitle: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xE678350F))
            .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TEST MODE — NO REAL PAYMENT",
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFFEF3C7),
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFFFDE68A),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DemoOutcomeControlsRow(
    enabled: Boolean,
    onTestCancelled: () -> Unit,
    onTestFailed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xCC4C1D95))
                .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                .testTag("demo_cancel_button")
                .clickable(enabled = enabled, onClick = onTestCancelled),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Test Cancelled",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFFEF3C7)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xCC991B1B))
                .border(1.5.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                .testTag("demo_fail_button")
                .clickable(enabled = enabled, onClick = onTestFailed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Test Failed",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}


