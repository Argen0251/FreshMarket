package com.example.freshmarket.view.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCMService", "From: ${remoteMessage.from}")

        // Если сообщение содержит уведомление, обрабатываем его
        remoteMessage.notification?.let {
            Log.d("FCMService", "Message Notification Title: ${it.title}")
            Log.d("FCMService", "Message Notification Body: ${it.body}")
            // Здесь можно создать локальное уведомление для пользователя,
            // используя NotificationManager.
        }

        // Если есть дополнительные данные, их тоже можно обработать
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCMService", "Message data payload: ${remoteMessage.data}")
            // Дополнительная логика обработки данных
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCMService", "Refreshed token: $token")
        // Отправь новый токен на свой сервер, если необходимо.
    }
}
