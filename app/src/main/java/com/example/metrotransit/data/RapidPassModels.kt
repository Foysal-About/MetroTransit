package com.example.metrotransit.data

data class MRTPassCard(
    val cardName: String,
    val cardNumber: String,
    val status: String,
    val balance: Double,
    val type: String = "MRT" // "MRT" or "RAPID"
)

data class UserProfile(
    val name: String,
    val email: String,
    val cards: List<MRTPassCard>
)

data class PaymentMethod(
    val name: String,
    val type: String,
    val iconRes: Int? = null
)

data class RechargeTransaction(
    val sl: Int,
    val cardNumber: String,
    val paymentId: String,
    val dateTime: String,
    val amount: String,
    val paymentStatus: String, // "Successful", "Canceled"
    val rechargeStatus: String? = null, // "Successful", null
    val refund: String? = null
)
