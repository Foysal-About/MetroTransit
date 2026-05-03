package com.example.metrotransit.viewmodel

import android.nfc.Tag
import android.nfc.tech.NfcF
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData

private const val TAG = "MRT_NFC"

// FeliCa service codes for the Dhaka MRT / Rapid Pass card
private const val HISTORY_SERVICE_CODE = 0x090F  // trip history log & balance

// Station codes → human-readable names (Source: MRT Buddy)
private val STATION_MAP = mapOf(
    0x06 to "Uttara North",
    0x07 to "Uttara Center",
    0x08 to "Uttara South",
    0x09 to "Pallabi",
    0x0A to "Mirpur 11",
    0x0B to "Mirpur 10",
    0x0C to "Kazipara",
    0x0D to "Shewrapara",
    0x0E to "Agargaon",
    0x0F to "Bijoy Sarani",
    0x10 to "Farmgate",
    0x11 to "Karwan Bazar",
    0x12 to "Shahbagh",
    0x13 to "Dhaka University",
    0x14 to "Secretariat",
    0x15 to "Motijheel",
    0x16 to "Kamalapur"
)

private fun stationName(code: Int) = STATION_MAP[code] ?: "Station #$code"

data class Transaction(
    val route: String,
    val date: String,
    val amount: Int,         // negative = fare deducted, positive = top-up
    val balanceAfter: Int    // balance in paisa after this transaction
)

class HomeViewModel : ViewModel() {

    var fromStation by mutableStateOf<MetroStation?>(StationData.stations.first())
    var toStation   by mutableStateOf<MetroStation?>(StationData.stations.last())

    var scannedBalance     by mutableStateOf<Double?>(null)
    var recentTransactions by mutableStateOf<List<Transaction>>(emptyList())
    var isScanning         by mutableStateOf(false)
    var scanError          by mutableStateOf<String?>(null)

    // -------------------------------------------------------------------------
    // Entry point called from MainActivity.onTagDiscovered
    // -------------------------------------------------------------------------
    fun onTagScanned(tag: Tag) {
        isScanning  = true
        scanError   = null

        val nfcF = NfcF.get(tag)
        if (nfcF == null) {
            scanError = "Not a FeliCa card. Make sure you're scanning an MRT / Rapid Pass."
            isScanning = false
            return
        }

        try {
            nfcF.connect()
            val idm = tag.id  // 8-byte card identifier

            // In Suica/Cybernetics standard (used by Dhaka MRT), 
            // history and balance are both in 0x090F.
            val transactions = readHistory(nfcF, idm)
            
            if (transactions.isNotEmpty()) {
                scannedBalance = transactions.first().balanceAfter / 100.0 // paisa -> taka
            } else {
                scannedBalance = 0.0
            }
            recentTransactions = transactions
            Log.d(TAG, "Scan OK — balance: $scannedBalance taka, ${transactions.size} trips")

        } catch (e: Exception) {
            Log.e(TAG, "Scan failed", e)
            scanError = "Scan failed: ${e.message}. Hold the card flat and still."
        } finally {
            try { nfcF.close() } catch (_: Exception) {}
            isScanning = false
        }
    }

    // -------------------------------------------------------------------------
    // Read trip history from service 0x090F, blocks 0–19 (up to 20 records)
    // Each block is 16 bytes. Empty blocks are all-zero and are skipped.
    // -------------------------------------------------------------------------
    private fun readHistory(nfcF: NfcF, idm: ByteArray): List<Transaction> {
        val blockIndices = (0 until 20).toList()
        val response = felicaRead(nfcF, idm, HISTORY_SERVICE_CODE, blockIndices)

        // Byte 9 of the response = number of blocks actually returned
        if (response.size < 10) throw Exception("History response too short")
        val numBlocks = response[9].toInt() and 0xFF
        Log.d(TAG, "History blocks returned: $numBlocks")

        val result = mutableListOf<Transaction>()

        for (i in 0 until numBlocks) {
            val offset = 10 + i * 16
            if (offset + 16 > response.size) break

            val block = response.copyOfRange(offset, offset + 16)

            // Skip completely empty blocks
            if (block.all { it == 0.toByte() }) continue

            result.add(parseHistoryBlock(block))
        }

        return result
    }

    // -------------------------------------------------------------------------
    // Build and send a FeliCa "Read Without Encryption" command (code 0x04).
    //
    // Command layout:
    //   [0]     = total length of command (1 byte)
    //   [1]     = 0x04 (Read Without Encryption request code)
    //   [2..9]  = IDm (8 bytes, card identifier)
    //   [10]    = number of services (1)
    //   [11]    = service code low byte
    //   [12]    = service code high byte
    //   [13]    = number of blocks
    //   [14+]   = block list: 0x80 (2-byte element, mode=0) + block number, per block
    //
    // Response layout:
    //   [0]     = response length
    //   [1]     = 0x05 (response code for Read Without Encryption)
    //   [2..9]  = IDm
    //   [9]     = status flag 1 (0x00 = success)
    //   [10]    = status flag 2 (0x00 = success)  ← NOTE: some docs say [9],[10]
    //   [10]    = number of blocks
    //   [11+]   = block data (16 bytes × numBlocks)
    // -------------------------------------------------------------------------
    private fun felicaRead(
        nfcF: NfcF,
        idm: ByteArray,
        serviceCode: Int,
        blockNumbers: List<Int>
    ): ByteArray {

        // Build command dynamically — size depends on number of blocks
        val cmd = mutableListOf<Byte>()
        cmd.add(0x00)                                       // [0] length placeholder
        cmd.add(0x04)                                       // [1] command code
        idm.forEach { cmd.add(it) }                         // [2..9] IDm
        cmd.add(0x01)                                       // [10] service count = 1
        cmd.add((serviceCode and 0xFF).toByte())            // [11] service code low
        cmd.add(((serviceCode shr 8) and 0xFF).toByte())   // [12] service code high
        cmd.add(blockNumbers.size.toByte())                 // [13] block count
        for (b in blockNumbers) {
            cmd.add(0x80.toByte())  // 2-byte block descriptor, access mode normal
            cmd.add(b.toByte())     // block number
        }
        cmd[0] = cmd.size.toByte() // fill in total length

        val command = cmd.toByteArray()
        Log.d(TAG, "CMD → ${command.toHex()}")

        val response = nfcF.transceive(command)
        Log.d(TAG, "RSP ← ${response.toHex()}")

        // Status bytes are at [9] and [10] (after IDm)
        if (response.size < 11) throw Exception("Response too short: ${response.size} bytes")
        val status1 = response[9].toInt() and 0xFF
        val status2 = response[10].toInt() and 0xFF
        if (status1 != 0x00) {
            throw Exception("FeliCa error — status1=0x${status1.toString(16).uppercase()}, status2=0x${status2.toString(16).uppercase()}")
        }

        return response
    }

    // -------------------------------------------------------------------------
    // Parse a single 16-byte history block into a Transaction.
    //
    // Byte layout (Cybernetics standard as used in Dhaka MRT):
    //   [0]      device code / ???
    //   [1]      transaction type (0x01=Ride, 0x05=Topup)
    //   [2-3]    ???
    //   [4-5]    date (packed: 7 bits year since 2000, 4 bits month, 5 bits day)
    //   [6]      entry station id
    //   [7]      exit station id
    //   [8-9]    ???
    //   [10-11]  balance after, little-endian, in Taka (Dhaka MRT stores balance directly in Taka)
    //   [12-15]  ???
    // -------------------------------------------------------------------------
    private fun parseHistoryBlock(block: ByteArray): Transaction {
        val txType      = block[1].toInt() and 0xFF
        val entryCode   = block[6].toInt() and 0xFF
        val exitCode    = block[7].toInt() and 0xFF

        // Balance after: little-endian at bytes 10–11. 
        // Note: Dhaka MRT actually stores balance as a 16-bit int in Taka, not paisa.
        val balanceTaka = (block[10].toInt() and 0xFF) or ((block[11].toInt() and 0xFF) shl 8)

        val route = when (txType) {
            0x05 -> "Top-up"
            else -> "${stationName(entryCode)} → ${stationName(exitCode)}"
        }

        // Date: bits 15-9 (year), 8-5 (month), 4-0 (day)
        val dateRaw = ((block[5].toInt() and 0xFF) shl 8) or (block[4].toInt() and 0xFF)
        val year    = (dateRaw shr 9) + 2000
        val month   = (dateRaw shr 5) and 0x0F
        val day     = dateRaw and 0x1F
        
        val date = if (day in 1..31 && month in 1..12) "$day/${getMonthName(month)}/$year" else "Recent"

        return Transaction(
            route        = route,
            date         = date,
            amount       = 0, // In this standard, we don't have the fare directly in the block
            balanceAfter = balanceTaka * 100 // convert to paisa for internal model consistency
        )
    }

    private fun getMonthName(m: Int) = when(m) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"
        7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> ""
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------
    private fun ByteArray.toHex() = joinToString(" ") { "%02X".format(it) }

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
}
