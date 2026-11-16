package com.example.billing

import android.app.Activity
import com.example.BuildConfig
import com.example.data.FruitBlissRepository
import com.example.model.StoreCatalog
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * DEBUG-ONLY Demo Purchase Mode billing gateway.
 *
 * This class lives strictly under src/debug/ and is never compiled into RELEASE APKs.
 * It simulates Store purchases locally without contacting Google Play Billing, never claims
 * transactions are real Google Play purchases, and prevents duplicate rewards per transaction token.
 */
class DemoBillingManager(
    private val repository: FruitBlissRepository
) : BillingGateway, DemoBillingCapable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 32)
    override val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

    private val _formattedPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    override val formattedPrices: StateFlow<Map<String, String>> = _formattedPrices.asStateFlow()

    private val processingMutex = Mutex()
    private val processedTokens = mutableSetOf<String>()

    override fun start() = Unit

    override fun refreshPurchases() = Unit

    override fun launchPurchase(activity: Activity, productId: String) {
        simulateDemoPurchase(
            productId = productId,
            outcome = DemoPurchaseOutcome.SUCCESS
        )
    }

    override fun simulateDemoPurchase(
        productId: String,
        outcome: DemoPurchaseOutcome,
        demoToken: String?
    ) {
        if (!BuildConfig.DEBUG || !BuildConfig.DEMO_PURCHASES_ENABLED) {
            _events.tryEmit(
                BillingEvent.BillingError("Demo Purchase Mode is only available in DEBUG builds.")
            )
            return
        }

        val product = StoreCatalog.findByProductId(productId)
        if (product == null) {
            _events.tryEmit(BillingEvent.BillingError("Unknown product ID: $productId"))
            return
        }

        when (outcome) {
            DemoPurchaseOutcome.CANCELLED -> {
                _events.tryEmit(BillingEvent.PurchaseCancelled)
            }

            DemoPurchaseOutcome.FAILED -> {
                _events.tryEmit(
                    BillingEvent.BillingError(
                        "Test Mode: Simulated payment failed — no real payment or reward was processed."
                    )
                )
            }

            DemoPurchaseOutcome.SUCCESS -> {
                val token = demoToken?.takeIf { it.isNotBlank() }
                    ?: "demo_tx_${UUID.randomUUID()}"
                scope.launch {
                    processingMutex.withLock {
                        if (token in processedTokens) {
                            _events.tryEmit(
                                BillingEvent.BillingError(
                                    "Duplicate test purchase ignored — reward already granted for this test transaction."
                                )
                            )
                            return@withLock
                        }

                        val granted = repository.grantDemoPurchaseOnce(
                            purchaseToken = token,
                            product = product,
                            orderId = "DEMO-TEST-${token.takeLast(8).uppercase()}",
                            quantity = 1,
                            purchaseTimeMillis = System.currentTimeMillis()
                        )
                        processedTokens.add(token)

                        if (granted) {
                            _events.tryEmit(
                                BillingEvent.PurchaseGranted(
                                    product = product,
                                    quantity = 1,
                                    purchaseToken = token,
                                    isDemoTransaction = true
                                )
                            )
                        } else {
                            _events.tryEmit(
                                BillingEvent.BillingError(
                                    "Duplicate test purchase ignored — reward already granted for this test transaction."
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun destroy() {
        scope.cancel()
    }
}
