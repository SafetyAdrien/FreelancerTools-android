package com.freelancertools.app.data.backup

import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.local.db.entity.Invoice
import com.freelancertools.app.data.local.db.entity.Palette
import com.freelancertools.app.data.local.db.entity.Project
import com.freelancertools.app.data.local.db.entity.Prompt
import com.freelancertools.app.data.local.db.entity.TimerSession
import com.freelancertools.app.data.local.db.entity.Transaction
import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val exportedAt: Long,
    val clients: List<Client> = emptyList(),
    val projects: List<Project> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val palettes: List<Palette> = emptyList(),
    val prompts: List<Prompt> = emptyList(),
    val timerSessions: List<TimerSession> = emptyList(),
)
