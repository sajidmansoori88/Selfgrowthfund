package com.selfgrowthfund.sgf.di

import android.content.Context
import androidx.room.Room
import com.selfgrowthfund.sgf.data.local.AppDatabase
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "self_growth_fund.db"
        )
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }

    /* DAOs */
    @Provides
    fun provideShareholderDao(db: AppDatabase): ShareholderDao = db.shareholderDao()

    @Provides
    fun provideDepositDao(db: AppDatabase): DepositDao = db.depositDao()

    @Provides
    fun provideBorrowingDao(db: AppDatabase): BorrowingDao = db.borrowingDao()

    @Provides
    fun provideRepaymentDao(db: AppDatabase): RepaymentDao = db.repaymentDao()

    @Provides
    fun provideInvestmentDao(db: AppDatabase): InvestmentDao = db.investmentDao()

    /* Utilities */
    @Provides
    @Singleton
    fun provideDates(): Dates = Dates // <-- Fixed: don't call Dates()

    /* Repositories */
    @Provides
    @Singleton
    fun provideShareholderRepository(
        shareholderDao: ShareholderDao,
        dates: Dates
    ): ShareholderRepository {
        return ShareholderRepository(shareholderDao, dates)
    }

    @Provides
    @Singleton
    fun provideDepositRepository(
        depositDao: DepositDao,
        dates: Dates
    ): DepositRepository {
        return DepositRepository(depositDao, dates)
    }

    @Provides
    @Singleton
    fun provideBorrowingRepository(
        borrowingDao: BorrowingDao,
        shareholderDao: ShareholderDao,
        dates: Dates
    ): BorrowingRepository {
        return BorrowingRepository(borrowingDao, shareholderDao, dates)
    }

    // Add more repositories as needed...
}
