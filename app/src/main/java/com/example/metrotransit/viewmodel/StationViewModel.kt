package com.example.metrotransit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData

class StationViewModel : ViewModel() {
    var searchQuery by mutableStateOf("")
    
    val filteredStations: List<MetroStation>
        get() = if (searchQuery.isEmpty()) {
            StationData.stations
        } else {
            StationData.stations.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.code.contains(searchQuery, ignoreCase = true)
            }
        }
}
