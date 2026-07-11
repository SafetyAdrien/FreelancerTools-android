package com.freelancertools.app.ui.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.Transaction
import com.freelancertools.app.data.local.db.entity.TransactionType
import com.freelancertools.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

data class DayBar(val day: Int, val income: Double, val expense: Double)

data class FinancesUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val marginPercent: Double = 0.0,
    val dailyBars: List<DayBar> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
)

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    val uiState: StateFlow<FinancesUiState> = combine(
        transactionRepository.observeAll(),
        monthTransactionsFlow(),
    ) { all, monthTx ->
        val balance = all.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
        val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val margin = if (income > 0) ((income - expense) / income) * 100 else 0.0

        val zone = ZoneId.systemDefault()
        val daysInMonth = LocalDate.now(zone).lengthOfMonth()
        val bars = (1..daysInMonth).map { day ->
            val dayTx = monthTx.filter {
                Instant.ofEpochMilli(it.date).atZone(zone).dayOfMonth == day
            }
            DayBar(
                day = day,
                income = dayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = dayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            )
        }

        FinancesUiState(
            balance = balance,
            totalIncome = income,
            totalExpense = expense,
            marginPercent = margin,
            dailyBars = bars,
            recentTransactions = all.take(10),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancesUiState())

    private fun monthTransactionsFlow(): kotlinx.coroutines.flow.Flow<List<Transaction>> {
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)
        val start = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return transactionRepository.observeBetween(start, end)
    }

    fun addTransaction(label: String, amount: Double, type: String, date: Long) {
        if (label.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            transactionRepository.save(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    label = label.trim(),
                    amount = amount,
                    type = type,
                    date = date,
                    clientId = null,
                ),
            )
        }
    }
}
