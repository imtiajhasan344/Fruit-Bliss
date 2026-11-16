package com.example.billing

import android.util.Base64
import com.android.billingclient.api.Purchase
import com.example.BuildConfig
import com.example.model.StoreCatalog
import com.example.model.StoreProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

sealed interface VerificationResult {
    data class Verified(val product: StoreProduct, val quantity: Int) : VerificationResult
    data class Pending(val product: StoreProduct) : VerificationResult
    data class Rejected(val reason: String) : VerificationResult
    data class RetryLater(val reason: String) : VerificationResult
}

/**
 * Verifies Google Play Billing purchases before granting any entitlement.
 *
 * Verification pipeline:
 * 1. Validates purchase state (PURCHASED vs PENDING), package name, non-empty purchase token,
 *    quantity >= 1, and known StoreCatalog product ID.
 * 2. When PLAY_BILLING_LICENSE_KEY is configured, verifies Google Play's RSA-SHA1 signature
 *    against originalJson using X.509 public key cryptography.
 * 3. When PLAY_VERIFICATION_URL (HTTPS backend) is configured, delegates server-side verification
 *    against the Google Play Developer API (`purchases.products:get`) before granting rewards.
 */
class PurchaseVerifier(
    private val expectedPackageName: String,
    private val base64PublicKey: String = BuildConfig.PLAY_BILLING_LICENSE_KEY,
    private val verificationEndpoint: String = BuildConfig.PLAY_VERIFICATION_URL
) {
    suspend fun verifyPurchase(purchase: Purchase): VerificationResult {
        val productId = purchase.products.firstOrNull().orEmpty()
        return verify(
            purchaseToken = purchase.purchaseToken,
            productId = productId,
            packageName = purchase.packageName,
            purchaseState = purchase.purchaseState,
            quantity = purchase.quantity,
            originalJson = purchase.originalJson,
            signature = purchase.signature,
            orderId = purchase.orderId
        )
    }

    suspend fun verify(
        purchaseToken: String,
        productId: String,
        packageName: String,
        purchaseState: Int,
        quantity: Int,
        originalJson: String,
        signature: String,
        orderId: String? = null
    ): VerificationResult {
        val product = StoreCatalog.findByProductId(productId)
            ?: return VerificationResult.Rejected("Unknown Google Play product ID: $productId")

        if (purchaseState == Purchase.PurchaseState.PENDING) {
            return VerificationResult.Pending(product)
        }

        if (purchaseState != Purchase.PurchaseState.PURCHASED) {
            return VerificationResult.Rejected("Purchase is not in PURCHASED state.")
        }

        if (purchaseToken.isBlank()) {
            return VerificationResult.Rejected("Missing Google Play purchase token.")
        }

        if (quantity < 1) {
            return VerificationResult.Rejected("Invalid purchase quantity: $quantity")
        }

        if (packageName.isNotBlank() && packageName != expectedPackageName) {
            return VerificationResult.Rejected("Package name mismatch: $packageName")
        }

        if (base64PublicKey.isNotBlank()) {
            if (originalJson.isBlank() || signature.isBlank()) {
                return VerificationResult.Rejected("Missing purchase payload or signature.")
            }
            if (!verifyRsaSignature(base64PublicKey, originalJson, signature)) {
                return VerificationResult.Rejected("Google Play signature verification failed.")
            }
        }

        if (verificationEndpoint.startsWith("https://", ignoreCase = true)) {
            val serverResult = verifyWithBackend(
                endpoint = verificationEndpoint,
                packageName = expectedPackageName,
                productId = productId,
                purchaseToken = purchaseToken,
                orderId = orderId,
                quantity = quantity
            )
            if (serverResult != null) return serverResult
        }

        return VerificationResult.Verified(product = product, quantity = quantity)
    }

    private suspend fun verifyWithBackend(
        endpoint: String,
        packageName: String,
        productId: String,
        purchaseToken: String,
        orderId: String?,
        quantity: Int
    ): VerificationResult? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            val payload = JSONObject().apply {
                put("packageName", packageName)
                put("productId", productId)
                put("purchaseToken", purchaseToken)
                put("quantity", quantity)
                if (!orderId.isNullOrBlank()) put("orderId", orderId)
            }
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }
            val code = connection.responseCode
            val body = runCatching {
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }.getOrDefault("")

            if (code !in 200..299) {
                return@withContext if (code in 400..499) {
                    VerificationResult.Rejected("Backend verification rejected purchase ($code).")
                } else {
                    VerificationResult.RetryLater("Verification server temporarily unavailable ($code).")
                }
            }
            val json = runCatching { JSONObject(body) }.getOrNull()
            val verified = json?.optBoolean("verified", false) ?: false
            if (!verified) {
                VerificationResult.Rejected("Backend verification denied entitlement.")
            } else {
                null
            }
        } catch (e: Exception) {
            VerificationResult.RetryLater("Unable to reach verification server: ${e.localizedMessage ?: "network error"}")
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        fun verifyRsaSignature(
            base64PublicKey: String,
            signedData: String,
            signatureBase64: String
        ): Boolean {
            return try {
                val decodedKey = Base64.decode(base64PublicKey.trim(), Base64.DEFAULT)
                val keySpec = X509EncodedKeySpec(decodedKey)
                val publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)
                val sigBytes = Base64.decode(signatureBase64.trim(), Base64.DEFAULT)
                val sig = Signature.getInstance("SHA1withRSA")
                sig.initVerify(publicKey)
                sig.update(signedData.toByteArray(Charsets.UTF_8))
                sig.verify(sigBytes)
            } catch (_: Exception) {
                false
            }
        }
    }
}
