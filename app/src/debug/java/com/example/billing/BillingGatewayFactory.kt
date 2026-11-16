package com.example.billing

import android.content.Context
import com.example.BuildConfig
import com.example.data.FruitBlissRepository

object BillingGatewayFactory {
    val isDemoPurchaseModeAvailable: Boolean
        get() = BuildConfig.DEBUG && BuildConfig.DEMO_PURCHASES_ENABLED

    fun create(context: Context, repository: FruitBlissRepository): BillingGateway {
        return if (isDemoPurchaseModeAvailable) {
            DemoBillingManager(repository)
        } else {
            PlayBillingManager(context, repository)
        }
    }
}
