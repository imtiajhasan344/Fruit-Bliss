package com.example.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.example.data.FruitBlissRepository
import com.example.model.StoreCatalog
import com.example.model.StoreProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

sealed interface BillingEvent {
    data class PurchaseGranted(
        val product: StoreProduct,
        val quantity: Int,
        val purchaseToken: String? = null,
        val isDemoTransaction: Boolean = false
    ) : BillingEvent
    data class PurchasePending(val product: StoreProduct) : BillingEvent
    data object PurchaseCancelled : BillingEvent
    data class BillingError(val message: String) : BillingEvent
}

enum class DemoPurchaseOutcome {
    SUCCESS,
    CANCELLED,
    FAILED
}

interface DemoBillingCapable {
    fun simulateDemoPurchase(
        productId: String,
        outcome: DemoPurchaseOutcome = DemoPurchaseOutcome.SUCCESS,
        demoToken: String? = null
    )
}

interface BillingGateway {
    val events: SharedFlow<BillingEvent>
    val formattedPrices: StateFlow<Map<String, String>>
    fun start()
    fun refreshPurchases()
    fun launchPurchase(activity: Activity, productId: String)
    fun destroy()
}

/**
 * Real Google Play Billing Library manager supporting:
 * - One-time consumable products for all 10 Treasure Vault Coin Packs & 12 Boost Forge items
 * - Automatic service reconnection and connection readiness gating
 * - Pending transactions (cash / delayed payment methods)
 * - Cryptographic / backend verification before entitlement
 * - Google Play consumption + acknowledgement before atomic Room grant
 * - Duplicate transaction protection via Mutex + purchaseToken Room primary key
 */
class PlayBillingManager(
    context: Context,
    private val repository: FruitBlissRepository,
    private val verifier: PurchaseVerifier = PurchaseVerifier(context.applicationContext.packageName)
) : BillingGateway, PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 32)
    override val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

    private val _formattedPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    override val formattedPrices: StateFlow<Map<String, String>> = _formattedPrices.asStateFlow()

    private val productDetailsCache = ConcurrentHashMap<String, ProductDetails>()
    private val processingMutex = Mutex()
    private val activeTokens = mutableSetOf<String>()

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    override fun start() {
        if (billingClient.isReady) {
            scope.launch {
                queryCatalogProducts()
                queryAndProcessExistingPurchases(notifyPending = false)
            }
            return
        }
        startBillingConnection(onConnected = {
            scope.launch {
                queryCatalogProducts()
                queryAndProcessExistingPurchases(notifyPending = false)
            }
        })
    }

    override fun refreshPurchases() {
        scope.launch {
            if (ensureConnected()) {
                queryAndProcessExistingPurchases(notifyPending = false)
            }
        }
    }

    override fun launchPurchase(activity: Activity, productId: String) {
        scope.launch {
            if (!ensureConnected()) {
                _events.tryEmit(
                    BillingEvent.BillingError(
                        "Google Play Billing is unavailable on this device. Please sign in to Google Play Store and try again."
                    )
                )
                return@launch
            }

            val productDetails = productDetailsCache[productId] ?: querySingleProduct(productId)
            if (productDetails == null) {
                _events.tryEmit(
                    BillingEvent.BillingError(
                        "Product '$productId' was not found in Google Play Console. Ensure the app is uploaded and the in-app product is Active."
                    )
                )
                return@launch
            }

            val offerToken = productDetails.oneTimePurchaseOfferDetailsList
                ?.firstOrNull { it.rentalDetails == null }
                ?.offerToken
                ?: productDetails.oneTimePurchaseOfferDetails?.offerToken

            val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
            if (!offerToken.isNullOrEmpty()) {
                productDetailsParamsBuilder.setOfferToken(offerToken)
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
                .build()

            val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)
            when (launchResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> Unit
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    _events.tryEmit(BillingEvent.PurchaseCancelled)
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    queryAndProcessExistingPurchases(notifyPending = true, notifyIfAlreadyConsumed = true)
                }
                else -> {
                    _events.tryEmit(BillingEvent.BillingError(formatBillingError(launchResult)))
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    scope.launch {
                        processPurchases(purchases, notifyPending = true, notifyIfAlreadyConsumed = false)
                    }
                } else {
                    _events.tryEmit(BillingEvent.BillingError("No purchase details were returned by Google Play."))
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _events.tryEmit(BillingEvent.PurchaseCancelled)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                scope.launch {
                    queryAndProcessExistingPurchases(notifyPending = true, notifyIfAlreadyConsumed = true)
                }
            }
            else -> {
                _events.tryEmit(BillingEvent.BillingError(formatBillingError(billingResult)))
            }
        }
    }

    override fun destroy() {
        billingClient.endConnection()
        scope.cancel()
    }

    private fun startBillingConnection(onConnected: (() -> Unit)? = null) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onConnected?.invoke()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Automatic reconnection is enabled via enableAutoServiceReconnection().
            }
        })
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return withTimeoutOrNull(12_000L) {
            suspendCancellableCoroutine { cont ->
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (cont.isActive) {
                            cont.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        if (cont.isActive) {
                            cont.resume(false)
                        }
                    }
                })
            }
        } ?: false
    }

    private suspend fun queryCatalogProducts() {
        val productList = StoreCatalog.allPlayProductIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val discoveredPrices = mutableMapOf<String, String>()
            result.productDetailsList?.forEach { details ->
                productDetailsCache[details.productId] = details
                extractFormattedPrice(details)?.let { price ->
                    discoveredPrices[details.productId] = price
                }
            }
            if (discoveredPrices.isNotEmpty()) {
                _formattedPrices.update { it + discoveredPrices }
            }
        }
    }

    private suspend fun querySingleProduct(productId: String): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val details = result.productDetailsList?.firstOrNull { it.productId == productId }
            if (details != null) {
                productDetailsCache[details.productId] = details
                extractFormattedPrice(details)?.let { price ->
                    _formattedPrices.update { it + (details.productId to price) }
                }
                return details
            }
        }
        return null
    }

    private fun extractFormattedPrice(details: ProductDetails): String? {
        val multiOfferPrice = details.oneTimePurchaseOfferDetailsList
            ?.firstOrNull { it.rentalDetails == null }
            ?.formattedPrice
            ?.takeIf { it.isNotBlank() }
        @Suppress("DEPRECATION")
        return multiOfferPrice ?: details.oneTimePurchaseOfferDetails?.formattedPrice?.takeIf { it.isNotBlank() }
    }

    private suspend fun queryAndProcessExistingPurchases(
        notifyPending: Boolean,
        notifyIfAlreadyConsumed: Boolean = false
    ) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = withContext(Dispatchers.IO) {
            billingClient.queryPurchasesAsync(params)
        }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            if (result.purchasesList.isEmpty() && notifyIfAlreadyConsumed) {
                _events.tryEmit(
                    BillingEvent.BillingError("Item state refreshed. Please try your purchase again.")
                )
            } else {
                processPurchases(
                    purchases = result.purchasesList,
                    notifyPending = notifyPending,
                    notifyIfAlreadyConsumed = notifyIfAlreadyConsumed
                )
            }
        } else if (notifyIfAlreadyConsumed) {
            _events.tryEmit(BillingEvent.BillingError(formatBillingError(result.billingResult)))
        }
    }

    private suspend fun processPurchases(
        purchases: List<Purchase>,
        notifyPending: Boolean,
        notifyIfAlreadyConsumed: Boolean = false
    ) {
        for (purchase in purchases) {
            val token = purchase.purchaseToken
            if (token.isBlank()) continue

            val acquired = processingMutex.withLock {
                if (activeTokens.contains(token)) {
                    false
                } else {
                    activeTokens.add(token)
                    true
                }
            }
            if (!acquired) continue

            try {
                handleSinglePurchase(purchase, notifyPending, notifyIfAlreadyConsumed)
            } finally {
                processingMutex.withLock {
                    activeTokens.remove(token)
                }
            }
        }
    }

    private suspend fun handleSinglePurchase(
        purchase: Purchase,
        notifyPending: Boolean,
        notifyIfAlreadyConsumed: Boolean = false
    ) {
        val productId = purchase.products.firstOrNull().orEmpty()
        val verification = verifier.verifyPurchase(purchase)

        when (verification) {
            is VerificationResult.Pending -> {
                repository.recordPendingPurchase(
                    purchaseToken = purchase.purchaseToken,
                    product = verification.product,
                    orderId = purchase.orderId,
                    quantity = purchase.quantity,
                    purchaseTimeMillis = purchase.purchaseTime
                )
                if (notifyPending) {
                    _events.tryEmit(BillingEvent.PurchasePending(verification.product))
                }
            }

            is VerificationResult.Rejected -> {
                repository.recordRejectedPurchase(
                    purchaseToken = purchase.purchaseToken,
                    productId = productId,
                    orderId = purchase.orderId,
                    quantity = purchase.quantity,
                    purchaseTimeMillis = purchase.purchaseTime
                )
                _events.tryEmit(BillingEvent.BillingError(verification.reason))
            }

            is VerificationResult.RetryLater -> {
                _events.tryEmit(BillingEvent.BillingError(verification.reason))
            }

            is VerificationResult.Verified -> {
                // Prevent duplicate reward granting if this token was already processed locally
                val alreadyGranted = repository.isPurchaseGranted(purchase.purchaseToken)
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                val consumeResult = withContext(Dispatchers.IO) {
                    billingClient.consumePurchase(consumeParams)
                }

                if (alreadyGranted) {
                    if (notifyIfAlreadyConsumed) {
                        _events.tryEmit(
                            BillingEvent.BillingError("Previous item refreshed. Please tap Buy again.")
                        )
                    }
                    return
                }

                if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val granted = repository.grantVerifiedPurchaseOnce(
                        purchaseToken = purchase.purchaseToken,
                        product = verification.product,
                        orderId = purchase.orderId,
                        quantity = verification.quantity,
                        purchaseTimeMillis = purchase.purchaseTime
                    )
                    if (granted) {
                        _events.tryEmit(
                            BillingEvent.PurchaseGranted(
                                product = verification.product,
                                quantity = verification.quantity,
                                purchaseToken = purchase.purchaseToken
                            )
                        )
                    }
                } else {
                    _events.tryEmit(
                        BillingEvent.BillingError(
                            formatBillingError(consumeResult.billingResult)
                        )
                    )
                }
            }
        }
    }

    private fun formatBillingError(result: BillingResult): String {
        return when (result.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                "Google Play Billing is unavailable. Make sure Google Play Store is active and signed in."
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.NETWORK_ERROR ->
                "Network error connecting to Google Play. Please check your connection and try again."
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                "This item is currently unavailable in Google Play."
            BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                "Google Play product configuration error. Verify your Play Console product IDs and app signing."
            else -> {
                val debug = result.debugMessage
                if (debug.isNotBlank()) "Payment failed: $debug"
                else "Unable to complete Google Play purchase (code ${result.responseCode})."
            }
        }
    }
}
