package com.example.metrotransit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.metrotransit.R
import com.example.metrotransit.data.MRTPassCard
import com.example.metrotransit.data.PaymentMethod
import com.example.metrotransit.data.RechargeTransaction
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
        MRTPassCard("SYED FOYSAL", "MP31C23112300999", "Active", 120.00, "MRT")
    ))
    val cards = _cards.asStateFlow()

    private val _rechargeHistory = MutableStateFlow(listOf(
        RechargeTransaction(1, "MP31C23112300869", "PA7DDCACAA3", "6 May 2026, 12:21 pm", "100", "Payment Canceled"),
        RechargeTransaction(2, "MP31C23112300869", "P1CC9F655B8", "6 May 2026, 12:21 pm", "100", "Payment Canceled"),
        RechargeTransaction(3, "MP31C23112300869", "P18575980E8", "4 May 2026, 10:52 am", "200", "Payment Canceled"),
        RechargeTransaction(4, "MP31C23112300869", "P9A27981083", "1 May 2026, 12:53 am", "100", "Payment Canceled"),
        RechargeTransaction(5, "MP31C23112300869", "PF97A3999D3", "1 May 2026, 12:46 am", "100", "Payment Canceled"),
        RechargeTransaction(6, "MP31C23112300869", "P52CFBDE68F", "4 Apr 2026, 06:41 pm", "100", "Payment Canceled"),
        RechargeTransaction(7, "MP31C23112300869", "PE5D8AE5E5F", "11 Mar 2026, 04:44 pm", "100", "Payment Successful", "Recharge Successful"),
        RechargeTransaction(8, "MP31C23112300869", "PE110B50AAF", "18 Feb 2026, 04:46 pm", "200", "Payment Successful", "Recharge Successful"),
        RechargeTransaction(9, "MP31C23112300869", "P6D916A358D", "30 Jan 2026, 03:09 pm", "200", "Payment Successful", "Recharge Successful"),
        RechargeTransaction(10, "MP31C23112300869", "PDESED9A2AE", "28 Dec 2025, 06:25 pm", "200", "Payment Successful", "Recharge Successful"),
        RechargeTransaction(11, "MP31C23112300869", "PBEA814BDE0", "28 Nov 2025, 04:29 pm", "200", "Payment Successful", "Recharge Successful")
    ))
    val rechargeHistory = _rechargeHistory.asStateFlow()

    var selectedCard by mutableStateOf<MRTPassCard?>(null)
    var rechargeAmount by mutableStateOf("")
    var paymentMethod by mutableStateOf("Bkash")

    val paymentMethods: List<PaymentMethod> = listOf(
        PaymentMethod("bKash", "Mobile Banking", R.drawable.bkash_logo),
        PaymentMethod("Nagad", "Mobile Banking", R.drawable.nagad_logo),
        PaymentMethod("Rocket", "Mobile Banking", R.drawable.rocket_logo),
        PaymentMethod("Upay", "Mobile Banking", R.drawable.upay_logo),
        // Visa and Mastercard were separate rows for the same card form; one row now, with
        // the two marks shown on it.
        PaymentMethod("Debit/Credit Card", "Card"),
    )

    fun login(): Boolean {
        return if (email == "1" && password == "1") {
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
