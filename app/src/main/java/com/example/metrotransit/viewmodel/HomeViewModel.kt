package com.example.metrotransit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData

class HomeViewModel : ViewModel() {
    var fromStation by mutableStateOf<MetroStation?>(StationData.stations.first())
    var toStation by mutableStateOf<MetroStation?>(StationData.stations.last())

    fun swapStations() {
        val temp = fromStation
        fromStation = toStation
        toStation = temp
    }

    fun setFrom(station: MetroStation) {
        fromStation = station
    }

    fun setTo(station: MetroStation) {
        toStation = station
    }
}
