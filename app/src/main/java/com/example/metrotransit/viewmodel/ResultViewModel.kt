package com.example.metrotransit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData
import com.example.metrotransit.data.TrainSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ResultViewModel : ViewModel() {
    private val _currentTime = MutableStateFlow(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    var trains by mutableStateOf<List<TrainSchedule>>(emptyList())
    var fromStation by mutableStateOf<MetroStation?>(null)
    var toStation by mutableStateOf<MetroStation?>(null)
    var stationCount by mutableStateOf(0)
    var estimatedTime by mutableStateOf(0)

    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                delay(1000)
            }
        }
    }

    fun initData(fromId: Int, toId: Int) {
        val from = StationData.stations.find { it.id == fromId }
        val to = StationData.stations.find { it.id == toId }
        fromStation = from
        toStation = to

        if (from != null && to != null) {
            stationCount = kotlin.math.abs(StationData.stations.indexOf(from) - StationData.stations.indexOf(to))
            estimatedTime = stationCount * 2
            trains = generateNextTrains(from, to)
        }
    }

    private fun generateNextTrains(from: MetroStation, to: MetroStation): List<TrainSchedule> {
        val now = LocalTime.now()
        val destination = to.name
        val platform = if (to.id > from.id) 1 else 2 // 1 for Motijheel direction, 2 for Uttara direction

        return listOf(
            TrainSchedule(destination, platform, now.plusMinutes(2).format(DateTimeFormatter.ofPattern("HH:mm"))),
            TrainSchedule(destination, platform, now.plusMinutes(6).format(DateTimeFormatter.ofPattern("HH:mm"))),
            TrainSchedule(destination, platform, now.plusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm")))
        )
    }
}
