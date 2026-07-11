package com.freelancertools.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.freelancertools.app.data.local.db.dao.ClientDao
import com.freelancertools.app.data.local.db.dao.InvoiceDao
import com.freelancertools.app.data.local.db.dao.PaletteDao
import com.freelancertools.app.data.local.db.dao.ProjectDao
import com.freelancertools.app.data.local.db.dao.PromptDao
import com.freelancertools.app.data.local.db.dao.TimerSessionDao
import com.freelancertools.app.data.local.db.dao.TransactionDao
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.local.db.entity.Invoice
import com.freelancertools.app.data.local.db.entity.Palette
import com.freelancertools.app.data.local.db.entity.Project
import com.freelancertools.app.data.local.db.entity.Prompt
import com.freelancertools.app.data.local.db.entity.TimerSession
import com.freelancertools.app.data.local.db.entity.Transaction

@Database(
    entities = [
        Client::class,
        Project::class,
        Transaction::class,
        Invoice::class,
        TimerSession::class,
        Palette::class,
        Prompt::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun projectDao(): ProjectDao
    abstract fun transactionDao(): TransactionDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun paletteDao(): PaletteDao
    abstract fun promptDao(): PromptDao

    companion object {
        const val NAME = "freelancer_tools.db"
    }
}
