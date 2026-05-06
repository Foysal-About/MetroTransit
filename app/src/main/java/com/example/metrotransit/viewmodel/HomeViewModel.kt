package com.example.metrotransit.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData
import com.example.metrotransit.nfc.NfcCardParser
import kotlin.math.*

private const val TAG = "MRT_NFC"

data class Transaction(
    val route: String,
    val date: String,
    val amount: Int,         // negative = fare deducted, positive = top-up
    val balanceAfter: Int    // balance in taka
)

class HomeViewModel : ViewModel() {

    var fromStation by mutableStateOf<MetroStation?>(StationData.stations.first())
    var toStation   by mutableStateOf<MetroStation?>(StationData.stations.last())

    var scannedBalance     by mutableStateOf<Double?>(null)
    var recentTransactions by mutableStateOf<List<Transaction>>(emptyList())
    var isScanning         by mutableStateOf(false)
    var scanError          by mutableStateOf<String?>(null)
    var showScanSheet      by mutableStateOf(false)
    var isLocating         by mutableStateOf(false)

    fun processNfcResponse(response: ByteArray) {
        try {
            // Response index 12 is number of blocks. Data starts at index 13.
            if (response.size >= 13 && response[10] == 0.toByte()) {
                val numBlocks = response[12].toInt() and 0xFF
                val tempTransactions = mutableListOf<Transaction>()
                
                for (i in 0 until numBlocks) {
                    val offset = 13 + i * 16
                    if (offset + 16 <= response.size) {
                        val block = response.copyOfRange(offset, offset + 16)
                        if (block.all { it == 0.toByte() }) continue
                        
                        val balance = NfcCardParser.parseBalance(block)
                        val timestamp = NfcCardParser.decodeTimestamp(block)
                        
                        val entryCode = block[8].toInt() and 0xFF
                        val exitCode = block[10].toInt() and 0xFF
                        
                        val route = if (entryCode != 0 || exitCode != 0) {
                            "${NfcCardParser.getStationName(entryCode)} → ${NfcCardParser.getStationName(exitCode)}"
                        } else {
                            "" // Mark as empty to fill later
                        }

                        tempTransactions.add(Transaction(
                            route = route,
                            date = timestamp,
                            amount = 0,
                            balanceAfter = balance
                        ))
                    }
                }

                // Calculate amounts and update route names
                val finalTransactions = mutableListOf<Transaction>()
                for (i in tempTransactions.indices) {
                    val current = tempTransactions[i]
                    val amount = if (i + 1 < tempTransactions.size) {
                        current.balanceAfter - tempTransactions[i+1].balanceAfter
                    } else {
                        0 
                    }
                    
                    var finalRoute = current.route
                    var finalDate = current.date
                    
                    if (amount > 0) {
                        finalRoute = "Balance Update"
                    } else if (finalRoute.isEmpty()) {
                        // If no station info, use the date as the primary title
                        finalRoute = current.date
                        finalDate = ""
                    }

                    finalTransactions.add(Transaction(
                        route = finalRoute,
                        date = finalDate,
                        amount = amount,
                        balanceAfter = current.balanceAfter
                    ))
                }

                if (finalTransactions.isNotEmpty()) {
                    scannedBalance = finalTransactions.first().balanceAfter.toDouble()
                    recentTransactions = finalTransactions
                } else {
                    scannedBalance = 0.0
                }
                Log.d(TAG, "Scan OK — balance: $scannedBalance")
            } else {
                scanError = "Card read failed: Invalid status or length"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parsing failed", e)
            scanError = "Parsing failed: ${e.message}"
        }
    }

    fun resetScan() {
        scannedBalance     = null
        recentTransactions = emptyList()
        isScanning         = false
        scanError          = null
    }

    fun setFrom(station: MetroStation) { fromStation = station }
    fun setTo(station: MetroStation)   { toStation   = station }
    fun swapStations() {
        val tmp = fromStation; fromStation = toStation; toStation = tmp
    }

    fun findNearestStation(lat: Double, lon: Double) {
        var nearestStation: MetroStation? = null
        var minDistance = Double.MAX_VALUE

        for (station in StationData.stations) {
            val distance = calculateDistance(lat, lon, station.latitude, station.longitude)
            if (distance < minDistance) {
                minDistance = distance
                nearestStation = station
            }
        }

        if (nearestStation != null) {
            fromStation = nearestStation
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
