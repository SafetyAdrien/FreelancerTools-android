package com.freelancertools.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.datastore.PreferencesManager
import com.freelancertools.app.data.local.db.entity.TransactionType
import com.freelancertools.app.data.repository.ClientRepository
import com.freelancertools.app.data.repository.PaletteRepository
import com.freelancertools.app.data.repository.ProjectRepository
import com.freelancertools.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DashboardUiState(
    val accountUsername: String = "",
    val billingName: String = "",
    val revenueThisMonth: Double = 0.0,
    val activeClientsCount: Int = 0,
    val activeProjectsCount: Int = 0,
    val palettesCount: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    clientRepository: ClientRepository,
    projectRepository: ProjectRepository,
    paletteRepository: PaletteRepository,
    preferencesManager: PreferencesManager,
) : ViewModel() {

    // Names are combined into one Pair flow first so the outer combine() stays within the
    // 5-flow typed overload — the 6-flow vararg overload only supports a single common element
    // type and would force unsafe casts back to Double/Int/String.
    private val names = combine(
        preferencesManager.accountUsername,
        preferencesManager.userName,
    ) { accountUsername, billingName -> accountUsername to billingName }

    val uiState: StateFlow<DashboardUiState> = combine(
        monthRevenueFlow(transactionRepository),
        clientRepository.observeActiveCount(),
        projectRepository.observeActiveCount(),
        paletteRepository.observeCount(),
        names,
    ) { revenue, clients, projects, palettes, (accountUsername, billingName) ->
        DashboardUiState(
            accountUsername = accountUsername,
            billingName = billingName,
            revenueThisMonth = revenue,
            activeClientsCount = clients,
            activeProjectsCount = projects,
            palettesCount = palettes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private fun monthRevenueFlow(repository: TransactionRepository): Flow<Double> {
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)
        val start = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return repository.observeBetween(start, end).map { transactions ->
            transactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
        }
    }
}
