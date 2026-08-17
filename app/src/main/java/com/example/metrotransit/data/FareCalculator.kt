package com.example.metrotransit.data

import kotlin.math.abs
import kotlin.math.sign

/**
 * Single source of truth for MRT Line-6 fare rules.
 *
 * Fare is always measured from the entry station, which is what makes journey
 * extension work: when a rider decides to travel further, the extra amount is
 * simply the fare to the new exit station minus what has already been paid.
 */
object FareCalculator {
    const val BASE_FARE = 20
    const val FARE_PER_HOP = 5

    /** Minutes of travel between two adjacent stations — used for ETA hints. */
    private const val MINUTES_PER_HOP = 2

    /** How long a bought ticket stays valid for entry before it must be validated at a gate. */
    const val ENTRY_WINDOW_MINUTES = 60

    /** Slack added on top of the travel time so a rider is never rushed at the exit gate. */
    private const val EXIT_BUFFER_MINUTES = 15

    fun station(stationId: Int): MetroStation? =
        StationData.stations.find { it.id == stationId }

    private fun indexOf(stationId: Int): Int =
        StationData.stations.indexOfFirst { it.id == stationId }

    fun hops(fromId: Int, toId: Int): Int {
        val from = indexOf(fromId)
        val to = indexOf(toId)
        return if (from < 0 || to < 0) 0 else abs(from - to)
    }

    fun fare(fromId: Int, toId: Int): Int = BASE_FARE + hops(fromId, toId) * FARE_PER_HOP

    fun fare(from: MetroStation?, to: MetroStation?): Int =
        if (from == null || to == null) BASE_FARE else fare(from.id, to.id)

    fun travelMinutes(fromId: Int, toId: Int): Int = hops(fromId, toId) * MINUTES_PER_HOP

    /**
     * Exit window a rider gets once the gate has validated the ticket: the ride itself
     * plus a buffer. Because it depends on the destination, extending the journey
     * automatically extends the time available.
     */
    fun journeyMinutes(fromId: Int, toId: Int): Int =
        travelMinutes(fromId, toId) + EXIT_BUFFER_MINUTES

    fun format(amount: Int): String = "৳$amount"

    /** Every station the rider passes through, entry and exit included, in travel order. */
    fun route(fromId: Int, toId: Int): List<MetroStation> {
        val from = indexOf(fromId)
        val to = indexOf(toId)
        if (from < 0 || to < 0) return emptyList()
        return if (from <= to) {
            StationData.stations.subList(from, to + 1)
        } else {
            StationData.stations.subList(to, from + 1).reversed()
        }
    }

    /**
     * Additional fare for moving the exit station from [currentToId] to [newToId].
     * Never negative — a shorter journey is not refunded at the gate.
     */
    fun extensionFare(entryId: Int, currentToId: Int, newToId: Int): Int =
        (fare(entryId, newToId) - fare(entryId, currentToId)).coerceAtLeast(0)

    /**
     * Stations the rider can still travel to without turning around, i.e. everything
     * past [currentToId] in the direction already being travelled. Empty when the
     * current destination is the end of the line.
     */
    fun onwardStations(entryId: Int, currentToId: Int): List<MetroStation> {
        val entry = indexOf(entryId)
        val destination = indexOf(currentToId)
        if (entry < 0 || destination < 0) return emptyList()

        val direction = (destination - entry).sign.let { if (it == 0) 1 else it }
        return if (direction > 0) {
            StationData.stations.drop(destination + 1)
        } else {
            StationData.stations.take(destination).reversed()
        }
    }
}
