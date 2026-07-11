package com.freelancertools.app.di

import android.content.Context
import androidx.room.Room
import com.freelancertools.app.data.local.db.AppDatabase
import com.freelancertools.app.data.local.db.dao.ClientDao
import com.freelancertools.app.data.local.db.dao.InvoiceDao
import com.freelancertools.app.data.local.db.dao.PaletteDao
import com.freelancertools.app.data.local.db.dao.ProjectDao
import com.freelancertools.app.data.local.db.dao.PromptDao
import com.freelancertools.app.data.local.db.dao.TimerSessionDao
import com.freelancertools.app.data.local.db.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()
    }

    @Provides
    fun provideClientDao(db: AppDatabase): ClientDao = db.clientDao()

    @Provides
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideInvoiceDao(db: AppDatabase): InvoiceDao = db.invoiceDao()

    @Provides
    fun provideTimerSessionDao(db: AppDatabase): TimerSessionDao = db.timerSessionDao()

    @Provides
    fun providePaletteDao(db: AppDatabase): PaletteDao = db.paletteDao()

    @Provides
    fun providePromptDao(db: AppDatabase): PromptDao = db.promptDao()
}
