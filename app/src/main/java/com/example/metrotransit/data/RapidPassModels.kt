package com.example.metrotransit.data

data class RapidPassCard(
    val cardName: String,
    val cardNumber: String,
    val status: String,
    val balance: Double,
    val type: String = "MRT" // "MRT" or "RAPID"
)

data class UserProfile(
    val name: String,
    val email: String,
    val cards: List<RapidPassCard>
)
