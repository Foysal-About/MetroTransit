package com.example.metrotransit

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.metrotransit.navigation.NavGraph
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.HomeViewModel

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null

    // Share the ViewModel between Activity (NFC) and Compose (UI)
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        enableEdgeToEdge()
        setContent {
            MetroTransitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // Pass the *same* ViewModel instance into the nav graph
                    NavGraph(
                        navController = navController,
                        homeViewModel = homeViewModel
                    )
                }
            }
        }
    }

    // Enable NFC reader mode whenever the app is in the foreground.
    // FLAG_READER_NFC_F is the FeliCa flag — required for Dhaka MRT / Rapid Pass cards.
    // The other flags are included so the app also detects other card types in future.
    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or   // ← FeliCa (MRT / Rapid Pass)
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    // Called on a background thread by the NFC stack.
    // BUG FIX: previously this stored the tag but never forwarded it to the ViewModel.
    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        Log.d("NFC", "Tag discovered: ${tag.id.joinToString("") { "%02X".format(it) }}")

        // Forward directly to ViewModel — it handles its own threading internally
        // (connect/transceive must happen on a non-main thread, which this already is)
        homeViewModel.onTagScanned(tag)
    }
}