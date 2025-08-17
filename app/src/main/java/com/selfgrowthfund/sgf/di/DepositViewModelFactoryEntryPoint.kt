package com.selfgrowthfund.sgf.di

import com.selfgrowthfund.sgf.ui.deposits.DepositViewModelFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DepositViewModelFactoryEntryPoint {
    fun depositViewModelFactory(): DepositViewModelFactory
}