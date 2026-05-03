package com.example.metrotransit.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF

class NfcManager(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val commandGenerator = NfcCommandGenerator()

    fun startScanning(
        onScanningStatusChange: (Boolean) -> Unit,
        onResponseRead: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        nfcAdapter?.enableReaderMode(activity, { tag ->
            onScanningStatusChange(true)
            val nfcF = NfcF.get(tag)
            try {
                nfcF.connect()
                val command = commandGenerator.generateReadCommand(tag.id)
                val response = nfcF.transceive(command)
                
                // Response index 10-11 are status flags (0,0 is success)
                // Index 12 is number of blocks. Data starts at index 13.
                if (response.size >= 12 && response[10] == 0.toByte()) {
                    onResponseRead(response)
                } else if (response.size < 12) {
                    onError("Invalid response length")
                } else {
                    onError("Card returned error status: ${response[10]}, ${response[11]}")
                }
                nfcF.close()
            } catch (e: Exception) {
                onError("Card read failed: ${e.message}")
            } finally {
                onScanningStatusChange(false)
            }
        }, NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, null)
    }

    fun stopScanning() {
        nfcAdapter?.disableReaderMode(activity)
    }
}
