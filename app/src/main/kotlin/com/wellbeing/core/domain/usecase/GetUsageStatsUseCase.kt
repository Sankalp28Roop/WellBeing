package com.wellbeing.core.domain.usecase

import com.wellbeing.core.data.repository.UsageRepository
import javax.inject.Inject

class GetUsageStatsUseCase @Inject constructor(
    private val repository: UsageRepository
) {
    operator fun invoke(date: Long) = repository.getUsageStatsForDate(date)
}
