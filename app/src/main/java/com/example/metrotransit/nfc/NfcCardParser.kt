package com.example.metrotransit.nfc

import java.util.Locale

object NfcCardParser {
    fun parseBalance(block: ByteArray): Int {
        return (block[13].toInt() and 0xFF shl 16) or
               (block[12].toInt() and 0xFF shl 8) or
               (block[11].toInt() and 0xFF)
    }

    fun decodeTimestamp(block: ByteArray): String {
        val value = ((block[4].toInt() and 0xFF) shl 16) or
                    ((block[5].toInt() and 0xFF) shl 8) or
                    (block[6].toInt() and 0xFF)
        
        val hour = (value shr 3) and 0x1F
        val day = (value shr 8) and 0x1F
        val month = (value shr 13) and 0x0F
        val year = 2000 + ((value shr 17) and 0x1F)
        
        val monthName = when (month) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"
            7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
            else -> "???"
        }

        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return String.format(Locale.US, "%02d %s %d, %02d:00 %s", day, monthName, year, displayHour, amPm)
    }
    
    fun getStationName(code: Int): String {
        return when(code) {
            90 -> "Uttara North"
            85 -> "Uttara Center"
            80 -> "Uttara South"
            75 -> "Pallabi"
            70 -> "Mirpur 11"
            65 -> "Mirpur 10"
            60 -> "Kazipara"
            55 -> "Shewrapara"
            50 -> "Agargaon"
            45 -> "Bijoy Sarani"
            40 -> "Farmgate"
            35 -> "Karwan Bazar"
            30 -> "Shahbagh"
            25 -> "Dhaka University"
            20 -> "Secretariat"
            15 -> "Motijheel"
            10 -> "Kamalapur"
            else -> "Unknown ($code)"
        }
    }
}
