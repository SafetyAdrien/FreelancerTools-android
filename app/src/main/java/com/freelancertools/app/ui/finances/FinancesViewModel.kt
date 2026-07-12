package com.freelancertools.app.ui.finances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.local.db.entity.Transaction
import com.freelancertools.app.data.local.db.entity.TransactionType
import com.freelancertools.app.data.repository.ClientRepository
import com.freelancertools.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

data class TransactionRow(val transaction: Transaction, val clientName: String?)

enum class TypeFilter(val label: String) { ALL("Tous"), INCOME("Revenus"), EXPENSE("Dépenses") }

enum class PeriodFilter(val label: String) {
    THIS_MONTH("Ce mois"), LAST_MONTH("Mois dernier"), THIS_YEAR("Cette année"),
    CUSTOM("Personnalisé"), ALL("Toutes"),
}

data class TransactionFilters(
    val type: TypeFilter = TypeFilter.ALL,
    val period: PeriodFilter = PeriodFilter.ALL,
    val clientId: String? = null,
    val customStart: Long? = null,
    val customEnd: Long? = null,
)

data class FinancesUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val marginPercent: Double = 0.0,
    val dailyBars: List<DayBar> = emptyList(),
    val query: String = "",
    val filters: TransactionFilters = TransactionFilters(),
    val filteredTransactions: List<TransactionRow> = emptyList(),
    val clients: List<Client> = emptyList(),
)

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val clientRepository: ClientRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filters = MutableStateFlow(TransactionFilters())

    val uiState: StateFlow<FinancesUiState> = combine(
        transactionRepository.observeAll(),
        monthTransactionsFlow(),
        clientRepository.observeAll(),
        query,
        filters,
    ) { all, monthTx, clients, q, f ->
        val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = all.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
        val margin = if (income > 0) ((income - expense) / income) * 100 else 0.0

        val zone = ZoneId.systemDefault()
        val daysInMonth = LocalDate.now(zone).lengthOfMonth()
        val bars = (1..daysInMonth).map { day ->
            val dayTx = monthTx.filter { Instant.ofEpochMilli(it.date).atZone(zone).dayOfMonth == day }
            DayBar(
                day = day,
                income = dayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = dayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            )
        }

        val clientById = clients.associateBy { it.id }
        val (periodStart, periodEnd) = periodRange(f)
        val filtered = all.asSequence()
            .filter { tx -> periodStart == null || (tx.date in periodStart..(periodEnd ?: Long.MAX_VALUE)) }
            .filter { tx ->
                when (f.type) {
                    TypeFilter.ALL -> true
                    TypeFilter.INCOME -> tx.type == TransactionType.INCOME
                    TypeFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                }
            }
            .filter { tx -> f.clientId == null || tx.clientId == f.clientId }
            .filter { tx ->
                if (q.isBlank()) {
                    true
                } else {
                    val clientName = tx.clientId?.let { clientById[it]?.name }.orEmpty()
                    tx.label.contains(q, ignoreCase = true) || clientName.contains(q, ignoreCase = true)
                }
            }
            .map { tx -> TransactionRow(tx, tx.clientId?.let { clientById[it]?.name }) }
            .toList()

        FinancesUiState(
            balance = balance,
            totalIncome = income,
            totalExpense = expense,
            marginPercent = margin,
            dailyBars = bars,
            query = q,
            filters = f,
            filteredTransactions = filtered,
            clients = clients,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancesUiState())

    private fun periodRange(filters: TransactionFilters): Pair<Long?, Long?> {
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)
        return when (filters.period) {
            PeriodFilter.ALL -> null to null
            PeriodFilter.THIS_MONTH -> {
                val start = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
                start to end
            }
            PeriodFilter.LAST_MONTH -> {
                val lastMonth = now.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
                start to end
            }
            PeriodFilter.THIS_YEAR -> {
                val start = LocalDate.of(now.year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = LocalDate.of(now.year, 12, 31).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
                start to end
            }
            PeriodFilter.CUSTOM -> filters.customStart to filters.customEnd
        }
    }

    private fun monthTransactionsFlow(): kotlinx.coroutines.flow.Flow<List<Transaction>> {
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)
        val start = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return transactionRepository.observeBetween(start, end)
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilters(value: TransactionFilters) {
        filters.value = value
    }

    fun addTransaction(label: String, amount: Double, type: String, date: Long, clientId: String?) {
        if (label.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            transactionRepository.save(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    label = label.trim(),
                    amount = amount,
                    type = type,
                    date = date,
                    clientId = clientId,
                ),
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { transactionRepository.delete(transaction) }
    }
}
