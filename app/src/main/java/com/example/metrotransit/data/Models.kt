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

object TicketStatus {
    const val ACTIVE = "Active"
    const val IN_TRANSIT = "In Transit"
    const val COMPLETED = "Completed"
    const val EXPIRED = "Expired"
}

/**
 * One "add another destination" step taken mid-journey. The rider keeps the same
 * QR ticket; the exit station moves forward and the difference in fare is charged.
 */
data class JourneyExtension(
    val fromStationId: Int,
    val toStationId: Int,
    val fromStationName: String,
    val toStationName: String,
    val extraFare: Int,
    val paymentMethod: String,
    val addedAt: String,
    /** Exit window gained by riding further — derived from the added travel time. */
    val extraMinutes: Int
)

data class QRTicket(
    val id: String,
    val fromStation: String,
    val toStation: String,
    val dateTime: String,
    val status: String,
    val fromStationId: Int = 0,
    val toStationId: Int = 0,
    val baseFare: Int = FareCalculator.BASE_FARE,
    val entryWindowMinutes: Int = FareCalculator.ENTRY_WINDOW_MINUTES,
    val createdAtMillis: Long = System.currentTimeMillis(),
    /** Set the moment the gate reader accepts the QR code — the journey has begun. */
    val validatedAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val extensions: List<JourneyExtension> = emptyList()
) {
    /** The one code the ticket lives by — read at the entry gate and again at the exit gate. */
    val qrData: String get() = "MT_TICKET_$id"

    val totalFare: Int get() = baseFare + extensions.sumOf { it.extraFare }

    /** Displayed everywhere a fare is shown, so it always follows the extensions. */
    val fare: String get() = FareCalculator.format(totalFare)

    /**
     * Time available for the ride itself, granted at the gate. Derived from the current
     * destination, so buying an extension lengthens it on its own.
     */
    val journeyMinutes: Int get() = FareCalculator.journeyMinutes(fromStationId, toStationId)

    /**
     * The ticket lives through two windows: [entryWindowMinutes] to reach a gate and
     * validate, then [journeyMinutes] to complete the ride and tap out.
     */
    val validityMinutes: Int get() = if (isValidated) journeyMinutes else entryWindowMinutes

    val isValidated: Boolean get() = validatedAtMillis != null
    val isInTransit: Boolean get() = status == TicketStatus.IN_TRANSIT
    val isCompleted: Boolean get() = status == TicketStatus.COMPLETED
    val isExpired: Boolean get() = status == TicketStatus.EXPIRED

    /** The exit station printed on the ticket before any extension was bought. */
    val originalDestinationId: Int
        get() = extensions.firstOrNull()?.fromStationId ?: toStationId

    /** When the current window runs out: the entry deadline, or the exit deadline once validated. */
    val deadlineMillis: Long
        get() = (validatedAtMillis ?: createdAtMillis) + validityMinutes * 60_000L

    fun remainingSeconds(nowMillis: Long = System.currentTimeMillis()): Int {
        if (isExpired) return 0
        return ((deadlineMillis - nowMillis) / 1000L).coerceAtLeast(0L).toInt()
    }
}
