package com.moneymate.app.di

import android.content.Context
import androidx.room.Room
import com.moneymate.app.data.local.AppDatabase
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.repository.DefaultPersonRepository
import com.moneymate.app.data.repository.MaintenanceRepository
import com.moneymate.app.utils.AppPreferences
import com.moneymate.app.utils.FirestorePathProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "moneymate_db"
        ).addMigrations(
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11,
            AppDatabase.MIGRATION_11_12
        ).build()
    }

    @Provides fun provideFileDao(db: AppDatabase): FileDao = db.fileDao()
    @Provides fun providePersonDao(db: AppDatabase): PersonDao = db.personDao()
    @Provides fun providePaymentDao(db: AppDatabase): PaymentDao = db.paymentDao()
    @Provides fun provideEditRequestDao(db: AppDatabase): EditRequestDao = db.editRequestDao()
    @Provides fun provideBookAdjustmentDao(db: AppDatabase): BookAdjustmentDao = db.bookAdjustmentDao()

    // DefaultPersonDao / DefaultPersonRepository are kept in the DI graph so any
    // existing injection sites compile without changes. The table is now always empty.
    @Provides fun provideDefaultPersonDao(db: AppDatabase): DefaultPersonDao = db.defaultPersonDao()

    @Provides
    @Singleton
    fun provideDefaultPersonRepository(dao: DefaultPersonDao): DefaultPersonRepository =
        DefaultPersonRepository(dao)

    @Provides
    @Singleton
    fun provideMaintenanceRepository(
        fileDao: FileDao,
        personDao: PersonDao,
        paymentDao: PaymentDao,
        paths: FirestorePathProvider
    ): MaintenanceRepository =
        MaintenanceRepository(fileDao, personDao, paymentDao, paths)

    // ─── Firestore path provider ──────────────────────────────────────────────

    /**
     * Provides [FirestorePathProvider] — the single source of truth for all
     * Firestore collection paths in the app. Inject this into any ViewModel
     * that reads from or writes to Firestore instead of hard-coding paths.
     */
    @Provides
    @Singleton
    fun provideFirestorePathProvider(prefs: AppPreferences): FirestorePathProvider =
        FirestorePathProvider(prefs)
}