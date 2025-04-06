    package com.example.freshmarket.worker

    import android.content.Context
    import android.util.Log
    import androidx.work.Worker
    import androidx.work.WorkerParameters
    import com.example.freshmarket.repository.PersistentOrderHistoryRepository

    class OrderStatusWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

        companion object {
            private const val TAG = "OrderStatusWorker"
        }

        override fun doWork(): Result {
            return try {
                val repository = PersistentOrderHistoryRepository(applicationContext)
                val orders = repository.getOrders()
                val currentTime = System.currentTimeMillis()
                // Порог времени – 2 часа (2 * 60 * 60 * 1000 миллисекунд)
                val thresholdMillis = 2 * 60 * 60 * 1000L
                var updated = false
                val updatedOrders = orders.map { order ->
                    // Если заказ находится в состоянии "Курьер в пути" и прошло 2 часа, обновляем статус на "Доставлен"
                    if (order.status.contains("Курьер") && currentTime - order.date.time >= thresholdMillis) {
                        updated = true
                        order.copy(status = "Доставлен")
                    } else {
                        order
                    }
                }
                if (updated) {
                    repository.saveOrders(updatedOrders)
                    Log.d(TAG, "Статусы заказов обновлены.")
                } else {
                    Log.d(TAG, "Нет заказов для обновления.")
                }
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при обновлении статусов заказов", e)
                Result.failure()
            }
        }
    }
