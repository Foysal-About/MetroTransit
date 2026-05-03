package com.example.metrotransit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.MRTPassCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MRTPassViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _cards = MutableStateFlow(listOf(
        MRTPassCard("SYED FOYSAL", "MP31C23112300869", "Active", 35.00, "MRT"),
        MRTPassCard("Syed Foysal", "MP31C23112300999", "Active", 141.00, "RAPID")
    ))
    val cards = _cards.asStateFlow()

    var selectedCard by mutableStateOf<MRTPassCard?>(null)
    var rechargeAmount by mutableStateOf("")
    var paymentMethod by mutableStateOf("Bkash")

    fun login(): Boolean {
        return if (email == "demo" && password == "1234") {
            _isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        email = ""
        password = ""
    }

    fun recharge(): Boolean {
        val amount = rechargeAmount.toDoubleOrNull() ?: 0.0
        if (amount <= 0 || selectedCard == null) return false
        
        val updatedCards = _cards.value.map {
            if (it.cardNumber == selectedCard?.cardNumber) {
                it.copy(balance = it.balance + amount)
            } else {
                it
            }
        }
        _cards.value = updatedCards
        selectedCard = updatedCards.find { it.cardNumber == selectedCard?.cardNumber }
        rechargeAmount = ""
        return true
    }
}
