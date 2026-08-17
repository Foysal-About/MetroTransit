package com.example.metrotransit.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.FareCalculator
import com.example.metrotransit.data.JourneyExtension
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.QRTicket
import com.example.metrotransit.data.StationData
import com.example.metrotransit.data.TicketStatus
import java.text.SimpleDateFormat
import java.util.*

class TicketViewModel : ViewModel() {
    private val _tickets = mutableStateListOf<QRTicket>()
    val tickets: List<QRTicket> get() = _tickets

    fun ticketById(ticketId: String): QRTicket? = _tickets.find { it.id == ticketId }

    fun addTicket(from: MetroStation?, to: MetroStation?): QRTicket {
        val ticket = QRTicket(
            id = "MT-${(100000..999999).random()}",
            fromStation = from?.name ?: "Unknown",
            toStation = to?.name ?: "Unknown",
            dateTime = timestamp(),
            status = TicketStatus.ACTIVE,
            fromStationId = from?.id ?: 0,
            toStationId = to?.id ?: 0,
            baseFare = FareCalculator.fare(from, to)
        )
        _tickets.add(0, ticket)
        return ticket
    }

    /** The gate reader has accepted the QR code — the rider is inside the station. */
    fun validateTicket(ticketId: String) = update(ticketId) { ticket ->
        if (ticket.isValidated) ticket
        else ticket.copy(
            status = TicketStatus.IN_TRANSIT,
            validatedAtMillis = System.currentTimeMillis()
        )
    }

    /**
     * Move the exit station further down the line and charge the fare difference.
     * The extra fare is computed here so the UI can never book a wrong amount.
     */
    fun extendJourney(ticketId: String, newDestination: MetroStation, paymentMethod: String) =
        update(ticketId) { ticket ->
            val extraFare = FareCalculator.extensionFare(
                entryId = ticket.fromStationId,
                currentToId = ticket.toStationId,
                newToId = newDestination.id
            )
            // Riding further also buys time: the exit window follows the new destination.
            val extraMinutes = FareCalculator.journeyMinutes(ticket.fromStationId, newDestination.id) -
                FareCalculator.journeyMinutes(ticket.fromStationId, ticket.toStationId)
            val extension = JourneyExtension(
                fromStationId = ticket.toStationId,
                toStationId = newDestination.id,
                fromStationName = ticket.toStation,
                toStationName = newDestination.name,
                extraFare = extraFare,
                paymentMethod = paymentMethod,
                addedAt = timestamp(),
                extraMinutes = extraMinutes
            )
            ticket.copy(
                toStation = newDestination.name,
                toStationId = newDestination.id,
                extensions = ticket.extensions + extension
            )
        }

    /** The 60-minute entry window lapsed before the rider reached a gate. */
    fun expireTicket(ticketId: String) = update(ticketId) { ticket ->
        if (ticket.isValidated || ticket.isCompleted) ticket
        else ticket.copy(status = TicketStatus.EXPIRED)
    }

    /** Rider has tapped out at the exit gate. */
    fun completeJourney(ticketId: String) = update(ticketId) { ticket ->
        ticket.copy(
            status = TicketStatus.COMPLETED,
            completedAtMillis = System.currentTimeMillis()
        )
    }

    private fun update(ticketId: String, transform: (QRTicket) -> QRTicket) {
        val index = _tickets.indexOfFirst { it.id == ticketId }
        if (index >= 0) _tickets[index] = transform(_tickets[index])
    }

    private fun timestamp(): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

    init {
        // Dummy history
        val station = { name: String -> StationData.stations.first { it.name == name } }
        _tickets.add(
            expiredTicket("MT-582931", station("Uttara North"), station("Motijheel"), "20 May 2024, 10:30 AM")
        )
        _tickets.add(
            expiredTicket("MT-129482", station("Agargaon"), station("Farmgate"), "18 May 2024, 04:15 PM")
        )
    }

    private fun expiredTicket(id: String, from: MetroStation, to: MetroStation, dateTime: String) =
        QRTicket(
            id = id,
            fromStation = from.name,
            toStation = to.name,
            dateTime = dateTime,
            status = TicketStatus.EXPIRED,
            fromStationId = from.id,
            toStationId = to.id,
            baseFare = FareCalculator.fare(from, to),
            createdAtMillis = 0L
        )
}
