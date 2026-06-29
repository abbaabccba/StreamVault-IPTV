package com.streamvault.app.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AutoM3uRefreshScheduler {
    private const val UNIQUE_PERIODIC_WORK_NAME = "auto_m3u_refresh_periodic"
    private const val UNIQUE_TEST_WORK_NAME = "auto_m3u_refresh_test_once"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 测试用：App 启动后约 3 分钟执行一次
        val testRequest = OneTimeWorkRequestBuilder<AutoM3uRefreshWorker>()
            .setInitialDelay(3, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_TEST_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            testRequest
        )

        // 正式周期任务：WorkManager 最短 15 分钟，不支持 3 分钟周期
        val periodicRequest = PeriodicWorkRequestBuilder<AutoM3uRefreshWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }
}
