package com.example

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Base64
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.Purchase
import com.example.billing.PurchaseVerifier
import com.example.billing.VerificationResult
import com.example.data.FruitBlissDatabase
import com.example.data.FruitBlissRepository
import com.example.model.BoostType
import com.example.model.StoreCatalog
import com.example.model.StoreProduct
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlayBillingIntegrationTest {

    private lateinit var database: FruitBlissDatabase
    private lateinit var repository: FruitBlissRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FruitBlissDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FruitBlissRepository(database.fruitBlissDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun generateRsaKeyPair(): KeyPair {
        return KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    }

    private fun signPayload(keyPair: KeyPair, payload: String): String {
        val signature = Signature.getInstance("SHA1withRSA")
        signature.initSign(keyPair.private)
        signature.update(payload.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    @Test
    fun manifestComplianceLeastPrivilegePermissionsAndDisabledBackup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val requested = pkgInfo.requestedPermissions?.toSet() ?: emptySet()
        val allowedPermissions = setOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.VIBRATE",
            "com.android.vending.BILLING",
            "${context.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
        )
        assertTrue(
            "Unexpected permissions found: ${requested - allowedPermissions}",
            requested.all { it in allowedPermissions }
        )
        assertFalse("android.permission.READ_EXTERNAL_STORAGE" in requested)
        assertFalse("android.permission.READ_MEDIA_IMAGES" in requested)
        assertFalse("android.permission.ACCESS_FINE_LOCATION" in requested)
        assertFalse("android.permission.CAMERA" in requested)

        val appInfo = context.applicationInfo
        assertEquals(0, appInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP)

        val appTitle = context.getString(R.string.app_name)
        assertTrue(appTitle.length <= 30)
        assertFalse(appTitle.contains("Free", ignoreCase = true))
        assertFalse(appTitle.contains("#1"))
    }

    @Test
    fun all22ProductsHaveUniqueGooglePlayProductIdsAndExactPrices() {
        val allIds = StoreCatalog.allPlayProductIds
        assertEquals(22, allIds.size)
        assertEquals(22, allIds.toSet().size)

        assertEquals("$0.10", StoreCatalog.coinPacks.first().priceText)
        assertEquals("$100.01", StoreCatalog.coinPacks.last().priceText)
        assertEquals("$0.10", BoostType.MAGIC_HAMMER.priceText)
        assertEquals("$10.00", BoostType.MEGA_BONUS.priceText)
    }

    @Test
    fun purchaseVerifierChecksRsaSignaturePendingPackageAndProduct() = runTest {
        val keyPair = generateRsaKeyPair()
        val publicKeyBase64 = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        val packageName = "com.aistudio.fruitbliss.k9m2xp"
        val verifier = PurchaseVerifier(
            expectedPackageName = packageName,
            base64PublicKey = publicKeyBase64,
            verificationEndpoint = ""
        )

        val originalJson = """{"orderId":"GPA.1234-5678","packageName":"$packageName","productId":"fruit_bliss_coins_100","purchaseToken":"tok_valid_1"}"""
        val validSig = signPayload(keyPair, originalJson)

        val validResult = verifier.verify(
            purchaseToken = "tok_valid_1",
            productId = "fruit_bliss_coins_100",
            packageName = packageName,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            quantity = 1,
            originalJson = originalJson,
            signature = validSig,
            orderId = "GPA.1234-5678"
        )
        assertTrue(validResult is VerificationResult.Verified)
        assertEquals("fruit_bliss_coins_100", (validResult as VerificationResult.Verified).product.productId)

        // Tampered payload fails RSA verification
        val tamperedResult = verifier.verify(
            purchaseToken = "tok_valid_1",
            productId = "fruit_bliss_coins_100",
            packageName = packageName,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            quantity = 1,
            originalJson = originalJson.replace("fruit_bliss_coins_100", "fruit_bliss_coins_10000"),
            signature = validSig
        )
        assertTrue(tamperedResult is VerificationResult.Rejected)

        // Pending purchase returns Pending (never Verified)
        val pendingResult = verifier.verify(
            purchaseToken = "tok_pending_1",
            productId = "fruit_bliss_boost_rocket",
            packageName = packageName,
            purchaseState = Purchase.PurchaseState.PENDING,
            quantity = 1,
            originalJson = "",
            signature = ""
        )
        assertTrue(pendingResult is VerificationResult.Pending)

        // Package mismatch rejected
        val mismatchPackage = verifier.verify(
            purchaseToken = "tok_valid_2",
            productId = "fruit_bliss_coins_10",
            packageName = "com.evil.clone",
            purchaseState = Purchase.PurchaseState.PURCHASED,
            quantity = 1,
            originalJson = originalJson,
            signature = validSig
        )
        assertTrue(mismatchPackage is VerificationResult.Rejected)

        // Unknown SKU rejected
        val unknownProduct = verifier.verify(
            purchaseToken = "tok_valid_3",
            productId = "unknown_sku",
            packageName = packageName,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            quantity = 1,
            originalJson = originalJson,
            signature = validSig
        )
        assertTrue(unknownProduct is VerificationResult.Rejected)
    }

    @Test
    fun repositoryGrantsVerifiedCoinPacksAndBoostsIdempotentlyWithoutDeductingVirtualCoins() = runTest {
        repository.ensureInitialized()

        val initialProfile = repository.profileFlow.first()
        assertEquals(0, initialProfile.coins)
        assertEquals(0, initialProfile.stars)

        val pack100 = StoreCatalog.coinPacks.first { it.coins == 100 }
        val packProduct = StoreProduct.CoinPackProduct(pack100)

        // Grant verified Coin Pack once (quantity 2 -> 200 coins)
        val grantedFirst = repository.grantVerifiedPurchaseOnce(
            purchaseToken = "tok_coins_100",
            product = packProduct,
            orderId = "GPA.100",
            quantity = 2,
            purchaseTimeMillis = 1_700_000_000_000L
        )
        assertTrue(grantedFirst)
        assertEquals(200, repository.profileFlow.first().coins)
        assertTrue(repository.isPurchaseGranted("tok_coins_100"))

        // Duplicate token must NOT grant coins again
        val grantedDuplicate = repository.grantVerifiedPurchaseOnce(
            purchaseToken = "tok_coins_100",
            product = packProduct,
            orderId = "GPA.100",
            quantity = 2,
            purchaseTimeMillis = 1_700_000_000_000L
        )
        assertFalse(grantedDuplicate)
        assertEquals(200, repository.profileFlow.first().coins)

        // Pending Boost Forge item records PENDING and does NOT increment inventory or touch coins
        val megaBonusProduct = StoreProduct.BoostProduct(BoostType.MEGA_BONUS)
        val beforeBoostCount = repository.boostInventoryFlow.first()[BoostType.MEGA_BONUS] ?: 0
        repository.recordPendingPurchase(
            purchaseToken = "tok_boost_pending",
            product = megaBonusProduct,
            orderId = "GPA.200",
            quantity = 1,
            purchaseTimeMillis = 1_700_000_001_000L
        )
        assertEquals(beforeBoostCount, repository.boostInventoryFlow.first()[BoostType.MEGA_BONUS] ?: 0)
        assertFalse(repository.isPurchaseGranted("tok_boost_pending"))

        // When pending purchase transitions to verified PURCHASED, it grants boost and leaves coins/stars untouched
        val grantedBoost = repository.grantVerifiedPurchaseOnce(
            purchaseToken = "tok_boost_pending",
            product = megaBonusProduct,
            orderId = "GPA.200",
            quantity = 1,
            purchaseTimeMillis = 1_700_000_002_000L
        )
        assertTrue(grantedBoost)
        assertEquals(beforeBoostCount + 1, repository.boostInventoryFlow.first()[BoostType.MEGA_BONUS])
        assertEquals(200, repository.profileFlow.first().coins)
        assertEquals(0, repository.profileFlow.first().stars)

        val history = repository.purchaseHistoryFlow.first()
        assertEquals(2, history.size)
        assertTrue(history.all { it.status == "GRANTED" && it.acknowledged && it.verified })
    }
}
