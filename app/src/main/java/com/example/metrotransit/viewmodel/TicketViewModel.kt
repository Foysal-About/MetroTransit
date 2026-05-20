package com.example.metrotransit.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.QRTicket
import java.text.SimpleDateFormat
import java.util.*

class TicketViewModel : ViewModel() {
    private val _tickets = mutableStateListOf<QRTicket>()
    val tickets: List<QRTicket> get() = _tickets

    fun addTicket(from: String, to: String, fare: String) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val currentDateTime = sdf.format(Date())
        val ticketId = "MT-${(100000..999999).random()}"
        
        val newTicket = QRTicket(
            id = ticketId,
            fromStation = from,
            toStation = to,
            dateTime = currentDateTime,
            fare = fare,
            status = "Active"
        )
        _tickets.add(0, newTicket)
    }

    init {
        // Add some dummy history
        _tickets.add(QRTicket("MT-582931", "Uttara North", "Motijheel", "20 May 2024, 10:30 AM", "৳100", "Expired"))
        _tickets.add(QRTicket("MT-129482", "Agargaon", "Farmgate", "18 May 2024, 04:15 PM", "৳20", "Expired"))
    }
}
