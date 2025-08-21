package com.selfgrowthfund.sgf.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.AppDatabase
import com.selfgrowthfund.sgf.data.local.Migrations
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.repository.*
import com.selfgrowthfund.sgf.utils.Dates
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /* Database */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "self_growth_fund.db"
        )
            .addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3,
                Migrations.MIGRATION_3_4
            )
            .addCallback(AppDatabase.DatabaseCallback())
            .build()
    }

    /* DAOs */
    @Provides fun provideShareholderDao(db: AppDatabase): ShareholderDao = db.shareholderDao()
    @Provides fun provideDepositDao(db: AppDatabase): DepositDao = db.depositDao()
    @Provides fun provideBorrowingDao(db: AppDatabase): BorrowingDao = db.borrowingDao()
    @Provides fun provideRepaymentDao(db: AppDatabase): RepaymentDao = db.repaymentDao()
    @Provides fun provideInvestmentDao(db: AppDatabase): InvestmentDao = db.investmentDao()
    @Provides fun provideDepositEntryDao(db: AppDatabase): DepositEntryDao = db.depositEntryDao()
    @Provides fun provideInvestmentReturnsDao(db: AppDatabase): InvestmentReturnsDao = db.investmentReturnsDao()

    /* Utilities */
    @Provides @Singleton fun provideDates(): Dates = Dates

    /* Repositories */
    @Provides @Singleton
    fun provideShareholderRepository(
        shareholderDao: ShareholderDao,
        dates: Dates,
        firestore: FirebaseFirestore
    ): ShareholderRepository = ShareholderRepository(shareholderDao, dates, firestore)

    @Provides @Singleton
    fun provideDepositRepository(
        depositDao: DepositDao,
        depositEntryDao: DepositEntryDao,
        dates: Dates
    ): DepositRepository = DepositRepository(depositDao, depositEntryDao, dates)

    @Provides @Singleton
    fun provideBorrowingRepository(
        borrowingDao: BorrowingDao,
        shareholderDao: ShareholderDao,
        dates: Dates
    ): BorrowingRepository = BorrowingRepository(borrowingDao, shareholderDao, dates)

    @Provides @Singleton
    fun provideInvestmentRepository(
        investmentDao: InvestmentDao,
        dates: Dates
    ): InvestmentRepository = InvestmentRepository(investmentDao, dates)

    @Provides @Singleton
    fun provideInvestmentReturnsRepository(
        returnsDao: InvestmentReturnsDao,
        investmentDao: InvestmentDao,
        dates: Dates
    ): InvestmentReturnsRepository = InvestmentReturnsRepository(returnsDao, investmentDao, dates)
}