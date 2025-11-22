package com.cycb.chat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.cycb.chat.MainActivity
import com.cycb.chat.R
import com.cycb.chat.data.storage.TokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.cycb.chat.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID_MESSAGES = "messages"
        private const val CHANNEL_ID_SOCIAL = "social"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tokenManager = TokenManager(applicationContext)
                tokenManager.token.collect { authToken ->
                    if (authToken != null) {
                        com.cycb.chat.data.api.RetrofitClient.setToken(authToken)
                        val apiService = com.cycb.chat.data.api.RetrofitClient.apiService
                        val response = apiService.registerFCMToken(mapOf("fcmToken" to token))

                        if (response.isSuccessful) {
                            Log.d(TAG, "FCM token registered successfully")
                        } else {
                            Log.e(TAG, "Failed to register FCM token: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering FCM token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        val settingsPreferences = SettingsPreferences(applicationContext)
        val shouldShowNotification = runBlocking {
            settingsPreferences.notificationsEnabled.first()
        }

        if (!shouldShowNotification) {
            Log.d(TAG, "Notifications are disabled globally")
            return
        }

        val notificationType = message.data["type"] ?: return

        when (notificationType) {
            "new_message" -> {
                val messagesEnabled = runBlocking { settingsPreferences.messagesNotif.first() }
                if (messagesEnabled) {
                    handleNewMessageNotification(message)
                } else {
                    Log.d(TAG, "Message notifications are disabled")
                }
            }
            "friend_request" -> {
                val friendRequestsEnabled = runBlocking { settingsPreferences.friendRequestsNotif.first() }
                if (friendRequestsEnabled) {
                    handleFriendRequestNotification(message)
                } else {
                    Log.d(TAG, "Friend request notifications are disabled")
                }
            }
            "friend_request_accepted" -> {
                val friendRequestsEnabled = runBlocking { settingsPreferences.friendRequestsNotif.first() }
                if (friendRequestsEnabled) {
                    handleFriendRequestAcceptedNotification(message)
                } else {
                    Log.d(TAG, "Friend request notifications are disabled")
                }
            }
            "chat_invite" -> {
                val chatInvitesEnabled = runBlocking { settingsPreferences.chatInvitesNotif.first() }
                if (chatInvitesEnabled) {
                    handleChatInviteNotification(message)
                } else {
                    Log.d(TAG, "Chat invite notifications are disabled")
                }
            }
        }
    }

    private fun handleNewMessageNotification(message: RemoteMessage) {
        val chatId = message.data["chatId"] ?: return
        val chatName = message.data["chatName"] ?: "New Message"
        val messageContent = message.notification?.body ?: message.data["body"] ?: ""
        val senderName = message.data["senderName"] ?: chatName
        val timestamp = message.data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigateTo", "chat")
            putExtra("chatId", chatId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val person = Person.Builder()
            .setName(senderName)
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_notification))
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle(chatName)
            .addMessage(messageContent, timestamp, person)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getNotificationColor())
            .setColorized(true)
            .setLights(Color.BLUE, 1000, 500)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setShowWhen(true)
            .setWhen(timestamp)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(chatId.hashCode(), notification)
    }

    private fun handleFriendRequestNotification(message: RemoteMessage) {
        val senderName = message.data["senderDisplayName"] ?: "Someone"
        val senderId = message.data["senderId"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigateTo", "friends")
            senderId?.let { putExtra("userId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("$senderName wants to connect with you")
            .setBigContentTitle("👋 New Friend Request")
            .setSummaryText("Tap to view")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_SOCIAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("👋 New Friend Request")
            .setContentText("$senderName wants to be your friend")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getNotificationColor())
            .setColorized(false)
            .setLights(Color.GREEN, 1000, 500)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setShowWhen(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleFriendRequestAcceptedNotification(message: RemoteMessage) {
        val userName = message.data["displayName"] ?: "Someone"
        val userId = message.data["userId"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigateTo", if (userId != null) "profile" else "friends")
            userId?.let { putExtra("userId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("You and $userName are now friends! Start chatting now.")
            .setBigContentTitle("🎉 $userName accepted your request")
            .setSummaryText("Tap to message")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_SOCIAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎉 Friend Request Accepted")
            .setContentText("$userName accepted your friend request")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getNotificationColor())
            .setColorized(false)
            .setLights(Color.CYAN, 1000, 500)
            .setVibrate(longArrayOf(0, 150, 100, 150, 100, 150))
            .setShowWhen(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleChatInviteNotification(message: RemoteMessage) {
        val chatName = message.data["chatName"] ?: "a group"
        val inviterName = message.data["inviterName"] ?: "Someone"
        val chatId = message.data["chatId"]
        val memberCount = message.data["memberCount"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigateTo", if (chatId != null) "chat" else "chats")
            chatId?.let { putExtra("chatId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId?.hashCode() ?: System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("💬 Group Chat Invite")
            .addLine("Group: $chatName")
            .addLine("Invited by: $inviterName")

        if (memberCount != null) {
            inboxStyle.addLine("Members: $memberCount")
        }

        inboxStyle.setSummaryText("Tap to join")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_SOCIAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💬 Chat Invite")
            .setContentText("$inviterName invited you to '$chatName'")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getNotificationColor())
            .setColorized(false)
            .setLights(Color.MAGENTA, 1000, 500)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setShowWhen(true)
            .setGroup("chat_invites")
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(chatId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val messagesChannel = NotificationChannel(
                CHANNEL_ID_MESSAGES,
                "💬 Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages from your chats"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                enableLights(true)
                lightColor = Color.BLUE
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }

            val socialChannel = NotificationChannel(
                CHANNEL_ID_SOCIAL,
                "👥 Social",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Friend requests, acceptances, and group invites"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                enableLights(true)
                lightColor = Color.GREEN
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(messagesChannel)
            notificationManager.createNotificationChannel(socialChannel)
        }
    }

    private fun getNotificationColor(): Int {

        return Color.parseColor("#6200EE")
    }
}
