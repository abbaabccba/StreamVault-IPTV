package com.streamvault.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.streamvault.domain.model.ProviderType
import com.streamvault.domain.repository.ProviderRepository
import com.streamvault.domain.usecase.SyncProvider
import com.streamvault.domain.usecase.SyncProviderCommand
import com.streamvault.domain.usecase.SyncProviderResult
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class AutoM3uRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun providerRepository(): ProviderRepository
        fun syncProvider(): SyncProvider
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(
            applicationContext,
            Dependencies::class.java
        )

        val providers = deps.providerRepository()
            .getProviders()
            .first()
            .filter { it.type == ProviderType.M3U }

        var failed = false

        providers.forEach { provider ->
            val result = deps.syncProvider()(
                SyncProviderCommand(
                    providerId = provider.id,
                    force = true
                )
            )

            if (result is SyncProviderResult.Error) {
                failed = true
            }
        }

        return if (failed) Result.retry() else Result.success()
    }
}
