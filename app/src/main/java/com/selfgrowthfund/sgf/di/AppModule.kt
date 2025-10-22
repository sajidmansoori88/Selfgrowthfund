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

    // ============================================================
    // ===============  DATABASE & DAOs  ==========================
    // ============================================================

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "self_growth_fund.db")
            .addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3,
                Migrations.MIGRATION_3_4,
                Migrations.MIGRATION_4_5,
                Migrations.MIGRATION_5_6
            )
            .addCallback(AppDatabase.DatabaseCallback())
            .build()

    @Provides fun provideShareholderDao(db: AppDatabase): ShareholderDao = db.shareholderDao()
    @Provides fun provideDepositDao(db: AppDatabase): DepositDao = db.depositDao()
    @Provides fun provideBorrowingDao(db: AppDatabase): BorrowingDao = db.borrowingDao()
    @Provides fun provideRepaymentDao(db: AppDatabase): RepaymentDao = db.repaymentDao()
    @Provides fun provideInvestmentDao(db: AppDatabase): InvestmentDao = db.investmentDao()
    @Provides fun provideInvestmentReturnsDao(db: AppDatabase): InvestmentReturnsDao = db.investmentReturnsDao()
    @Provides fun provideActionItemDao(db: AppDatabase): ActionItemDao = db.actionItemDao()
    @Provides fun providePenaltyDao(db: AppDatabase): PenaltyDao = db.penaltyDao()
    @Provides fun provideIncomeDao(db: AppDatabase): OtherIncomeDao = db.incomeDao()
    @Provides fun provideExpenseDao(db: AppDatabase): OtherExpenseDao = db.expenseDao()
    @Provides fun provideApprovalFlowDao(db: AppDatabase): ApprovalFlowDao = db.approvalFlowDao()
    @Provides fun provideUserSessionDao(db: AppDatabase): UserSessionDao = db.userSessionDao()

    // ============================================================
    // ===============  UTILITIES  ================================
    // ============================================================

    @Provides
    @Singleton
    fun provideDates(): Dates = Dates

    // ============================================================
    // ===============  REPOSITORIES  ==============================
    // ============================================================

    @Provides
    @Singleton
    fun provideShareholderRepository(
        shareholderDao: ShareholderDao,
        dates: Dates,
        firestore: FirebaseFirestore
    ): ShareholderRepository = ShareholderRepository(shareholderDao, dates, firestore)

    @Provides
    @Singleton
    fun provideApprovalFlowRepository(
        dao: ApprovalFlowDao
    ): ApprovalFlowRepository = ApprovalFlowRepository(dao)

    @Provides
    @Singleton
    fun provideDepositRepository(
        depositDao: DepositDao,
        firestore: FirebaseFirestore,
        dates: Dates
    ): DepositRepository = DepositRepository(depositDao, firestore, dates)

    @Provides
    @Singleton
    fun provideBorrowingRepository(
        borrowingDao: BorrowingDao,
        shareholderDao: ShareholderDao,
        approvalFlowRepository: ApprovalFlowRepository,
        dates: Dates
    ): BorrowingRepository = BorrowingRepository(
        borrowingDao = borrowingDao,
        shareholderDao = shareholderDao,
        approvalFlowRepository = approvalFlowRepository,
        dates = dates
    )

    @Provides
    @Singleton
    fun provideInvestmentRepository(
        investmentDao: InvestmentDao,
        dates: Dates
    ): InvestmentRepository = InvestmentRepository(investmentDao, dates)

    @Provides
    @Singleton
    fun provideInvestmentReturnsRepository(
        returnsDao: InvestmentReturnsDao,
        investmentDao: InvestmentDao,
        dates: Dates
    ): InvestmentReturnsRepository = InvestmentReturnsRepository(returnsDao, investmentDao, dates)

    @Provides
    @Singleton
    fun provideRepaymentRepository(
        repaymentDao: RepaymentDao,
        borrowingRepository: BorrowingRepository,
        firestore: FirebaseFirestore
    ): RepaymentRepository = RepaymentRepository(repaymentDao, borrowingRepository, firestore)
}
