package com.freelancertools.app.data.backup

import com.freelancertools.app.data.repository.ClientRepository
import com.freelancertools.app.data.repository.InvoiceRepository
import com.freelancertools.app.data.repository.PaletteRepository
import com.freelancertools.app.data.repository.ProjectRepository
import com.freelancertools.app.data.repository.PromptRepository
import com.freelancertools.app.data.repository.TimerSessionRepository
import com.freelancertools.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val transactionRepository: TransactionRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paletteRepository: PaletteRepository,
    private val promptRepository: PromptRepository,
    private val timerSessionRepository: TimerSessionRepository,
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportJson(): String {
        val backup = AppBackup(
            exportedAt = System.currentTimeMillis(),
            clients = clientRepository.observeAll().first(),
            projects = projectRepository.observeAll().first(),
            transactions = transactionRepository.observeAll().first(),
            invoices = invoiceRepository.observeAll().first(),
            palettes = paletteRepository.observeAll().first(),
            prompts = promptRepository.observeAll().first(),
            timerSessions = timerSessionRepository.observeAll().first(),
        )
        return json.encodeToString(AppBackup.serializer(), backup)
    }

    suspend fun importJson(content: String) {
        val backup = json.decodeFromString(AppBackup.serializer(), content)
        backup.clients.forEach { clientRepository.save(it) }
        backup.projects.forEach { projectRepository.save(it) }
        backup.transactions.forEach { transactionRepository.save(it) }
        backup.invoices.forEach { invoiceRepository.save(it) }
        backup.palettes.forEach { paletteRepository.save(it) }
        backup.prompts.forEach { promptRepository.save(it) }
        backup.timerSessions.forEach { timerSessionRepository.save(it) }
    }
}
