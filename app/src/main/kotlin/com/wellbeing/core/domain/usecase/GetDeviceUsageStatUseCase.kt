package com.wellbeing.core.domain.usecase

import com.wellbeing.core.data.repository.UsageRepository
import javax.inject.Inject

class GetDeviceUsageStatUseCase @Inject constructor(
    private val repository: UsageRepository
) {
    operator fun invoke(date: Long) = repository.getDeviceUsageStat(date)
}
