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
