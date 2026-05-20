package com.example.metrotransit.data

data class MetroStation(
    val id: Int,
    val name: String,
    val code: String,
    val latitude: Double,
    val longitude: Double
)

data class TrainSchedule(
    val destination: String,
    val platform: Int,
    val departureTime: String
)

data class QRTicket(
    val id: String,
    val fromStation: String,
    val toStation: String,
    val dateTime: String,
    val fare: String,
    val status: String,
    val validityMinutes: Int = 30
)
