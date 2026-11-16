package com.example.billing

import android.content.Context
import com.example.data.FruitBlissRepository

object BillingGatewayFactory {
    const val isDemoPurchaseModeAvailable: Boolean = false

    fun create(context: Context, repository: FruitBlissRepository): BillingGateway {
        return PlayBillingManager(context, repository)
    }
}
