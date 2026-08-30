package com.example.metrotransit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.metrotransit.data.MRTPassCard
import com.example.metrotransit.data.PaymentMethod
import com.example.metrotransit.data.RechargeTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The MRT Pass portal's own state. Signing in is not part of it — the app has one account,
 * handled by [com.example.metrotransit.viewmodel.AuthViewModel] before the home dashboard —
 * so the portal opens straight onto the rider's cards.
 */
class MRTPassViewModel : ViewModel() {

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

    /**
     * How a top-up is paid for: the gateway, and nothing beside it.
     *
     * The wallets and the card form used to be rows of their own here, each with a screen
     * behind it collecting the money itself. A top-up is collected by SSLCOMMERZ, so the
     * channels belong on the gateway's page rather than on ours — listing bKash next to the
     * gateway that also carries bKash is the same channel offered twice, by two different
     * parties, and only one of them is really taking the money.
     */
    val paymentMethods: List<PaymentMethod> = listOf(
        PaymentMethod("SSLCOMMERZ", "Gateway")
    )

    /**
     * Credit the selected card with the amount the rider asked for.
     *
     * [paymentId] is the gateway's own reference for the payment that funded it — `tran_id` on
     * an SSLCOMMERZ top-up. Given one, the top-up is also written into the recharge history,
     * so a rider looking for what they just paid finds it by the reference the gateway showed
     * them rather than by the amount and the time of day.
     */
    fun recharge(paymentId: String? = null): Boolean {
        val amount = rechargeAmount.toDoubleOrNull() ?: 0.0
        val card = selectedCard ?: return false
        if (amount <= 0) return false

        val updatedCards = _cards.value.map {
            if (it.cardNumber == card.cardNumber) {
                it.copy(balance = it.balance + amount)
            } else {
                it
            }
        }
        _cards.value = updatedCards
        selectedCard = updatedCards.find { it.cardNumber == card.cardNumber }

        if (paymentId != null) {
            val entry = RechargeTransaction(
                sl = 1,
                cardNumber = card.cardNumber,
                paymentId = paymentId,
                dateTime = SimpleDateFormat("d MMM yyyy, hh:mm a", Locale.US).format(Date()),
                amount = amount.toInt().toString(),
                paymentStatus = "Payment Successful",
                rechargeStatus = "Recharge Successful"
            )
            // The list runs newest first and its serial numbers run with it, so everything
            // below the new row shifts down one rather than the new row taking a number
            // above the top of the list.
            _rechargeHistory.value = listOf(entry) +
                _rechargeHistory.value.mapIndexed { index, row -> row.copy(sl = index + 2) }
        }

        rechargeAmount = ""
        return true
    }
}
