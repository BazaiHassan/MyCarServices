package com.hbazai.mycarservices.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.hbazai.mycarservices.MainActivity
import com.hbazai.mycarservices.MyCarServicesApp.Companion.CHANNEL_ID
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.data.repository.CarRepository
import com.hbazai.mycarservices.data.repository.ServiceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class ServiceCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val serviceRepository: ServiceRepository,
    private val carRepository: CarRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val now           = System.currentTimeMillis()
            val sevenDaysAhead = now + TimeUnit.DAYS.toMillis(7)

            val dueServices = serviceRepository.getDueServices(
                thresholdDate  = sevenDaysAhead,
                currentMileage = Int.MAX_VALUE
            )

            val cars = carRepository.getAllCars().first()

            dueServices.forEach { service ->
                val car = cars.find { it.id == service.carId }
                if (car != null) {
                    sendNotification(
                        notificationId = service.id,
                        carName        = car.name,
                        serviceType    = service.serviceType
                    )
                    serviceRepository.markAsNotified(service.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(
        notificationId: Int,
        carName: String,
        serviceType: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(
                context.getString(R.string.notification_body, carName, serviceType)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(notificationId, notification)
    }

    companion object {
        const val WORK_NAME = "ServiceCheckWork"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ServiceCheckWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}