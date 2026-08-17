package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.metrotransit.utils.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val QrCardCorner = 24.dp
private val QrCardBorder = Color(0x14000000)

/**
 * The QR as a rider presents it: black code on a white card, identical at the entry gate
 * and the exit gate, and deliberately independent of the app theme so a dark-mode ticket
 * is still readable by a station scanner.
 *
 * [overlay] draws on top of the code — used for the EXPIRED stamp and the scanning spinner.
 */
@Composable
fun TicketQrCard(
    data: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.border(1.dp, QrCardBorder, RoundedCornerShape(QrCardCorner)),
        shape = RoundedCornerShape(QrCardCorner),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(18.dp)) {
            TicketQrCode(data = data, modifier = Modifier.fillMaxSize(), alpha = alpha)
            overlay()
        }
    }
}

/** Bare QR bitmap, drawn in its own colours. Prefer [TicketQrCard] for anything on screen. */
@Composable
fun TicketQrCode(
    data: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    var qrBitmap by remember(data) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            qrBitmap = QRCodeGenerator.generateQRCode(data, 512)
        }
    }

    val bitmap = qrBitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Ticket QR code",
            modifier = modifier,
            alpha = alpha
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}
