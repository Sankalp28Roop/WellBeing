package com.wellbeing.core.di

import com.wellbeing.core.domain.usecase.GetUsageStatsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    // Use cases can just be injected if they have @Inject constructor,
    // but we can provide them here if we want to be explicit or if they have interfaces.
}
