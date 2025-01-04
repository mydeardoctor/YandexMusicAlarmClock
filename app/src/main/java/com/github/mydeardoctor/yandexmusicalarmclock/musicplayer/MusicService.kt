package com.github.mydeardoctor.yandexmusicalarmclock.musicplayer

import android.app.ForegroundServiceStartNotAllowedException
import android.app.InvalidForegroundServiceTypeException
import android.app.MissingForegroundServiceTypeException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.github.mydeardoctor.yandexmusicalarmclock.R
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import com.github.mydeardoctor.yandexmusicalarmclock.permissions.PermissionManager
import com.github.mydeardoctor.yandexmusicalarmclock.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MusicService : Service()
{
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        //Check notifications permission.
        //Check full screen intent permission.
        val isNotificationPermissionGranted: Boolean =
            PermissionManager.getIsNotificationPermissionGranted(
                context = applicationContext)
        val isFullScreenIntentPermissionGranted: Boolean =
            PermissionManager.getIsFullScreenIntentPermissionGranted(
                context = applicationContext)

        //Make this service a foreground service.
        val notification : Notification = createMusicServiceNotification(
            context = applicationContext)
        try
        {
            //https://developer.android.com/develop/background-work/services/fgs/launch
            //https://developer.android.com/reference/androidx/core/app/ServiceCompat#startForeground(android.app.Service,int,android.app.Notification,int)
            //https://developer.android.com/reference/android/app/Service.html#startForeground(int,%20android.app.Notification,%20int)
            ServiceCompat.startForeground(
                this,
                MUSIC_SERVICE_NOTIFICATION_ID,
                notification,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                }
                else
                {
                    0
                })
        }
        catch(e: Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = applicationContext,
                    errorMessage = e.stackTraceToString())
            }

            if((e !is IllegalArgumentException) && (e !is SecurityException))
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                {
                    if(e !is ForegroundServiceStartNotAllowedException)
                    {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                        {
                            if((e !is InvalidForegroundServiceTypeException) &&
                               (e !is MissingForegroundServiceTypeException))
                            {
                                throw e
                            }
                        }
                        else
                        {
                            throw e
                        }
                    }
                }
                else
                {
                    throw e
                }
            }
        }

        //Stop playing.
        MusicPlayer.stopPlaying(context = applicationContext)
        //Start playing.
        MusicPlayer.startPlaying(context = applicationContext)

        return START_NOT_STICKY
    }

    override fun onDestroy()
    {
        super.onDestroy()

        //Stop playing.
        MusicPlayer.stopPlaying(context = applicationContext)

        //This service stops being a foreground service. Remove notification.
        //https://developer.android.com/develop/background-work/services/fgs/stop-fgs
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder?
    {
        return null
    }

    //Static methods.
    companion object
    {
        private const val MUSIC_SERVICE_NOTIFICATION_CHANNEL_ID: String =
            "Music Service Notification Channel ID"
        private const val MUSIC_SERVICE_NOTIFICATION_CHANNEL_NAME: String =
            "Music Service Notification Channel"
        private const val MUSIC_SERVICE_NOTIFICATION_TITLE: String =
            "Wakey-wakey!"
        private const val MUSIC_SERVICE_PENDING_INTENT_CODE: Int = 2
        private const val MUSIC_SERVICE_NOTIFICATION_ID: Int = 3

        public fun startMusicService(context: Context) : Boolean
        {
            //Stop any existing music service.
            val resultStopMusicService: Boolean =
                stopMusicService(context = context)
            if(resultStopMusicService == false)
            {
                return false
            }

            //Start music service.
            val musicServiceIntent: Intent = createMusicServiceIntent(
                context = context)
            try
            {
                //https://developer.android.com/develop/background-work/services/fgs/launch
                val componentName: ComponentName? =
                    context.startForegroundService(musicServiceIntent)
                if(componentName != null)
                {
                    return true
                }
                else
                {
                    return false
                }
            }
            catch(e: Exception)
            {
                Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }

                if(e !is SecurityException)
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    {
                        if(e !is ForegroundServiceStartNotAllowedException)
                        {
                            throw e
                        }
                    }
                    else
                    {
                        throw e
                    }
                }

                return false
            }
        }

        public fun stopMusicService(context: Context) : Boolean
        {
            //Stop any existing music service.
            val musicServiceIntent: Intent = createMusicServiceIntent(
                context = context)
            try
            {
                //https://developer.android.com/develop/background-work/services/fgs/stop-fgs
                context.stopService(musicServiceIntent)
                return true
            }
            catch(e: Exception)
            {
                Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }

                if((e !is SecurityException) && (e !is IllegalStateException))
                {
                    throw e
                }

                return false
            }
        }

        private fun createMusicServiceIntent(context: Context) : Intent
        {
            val musicServiceIntent: Intent = Intent(
                context,
                MusicService::class.java)
            return musicServiceIntent
        }

        public fun createMusicServiceNotificationChannel(context: Context)
        {
            //https://developer.android.com/develop/ui/views/notifications/channels
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if(notificationManager.getNotificationChannel(MUSIC_SERVICE_NOTIFICATION_CHANNEL_ID) == null)
                {
                    val notificationChannel: NotificationChannel =
                        NotificationChannel(
                            MUSIC_SERVICE_NOTIFICATION_CHANNEL_ID,
                            MUSIC_SERVICE_NOTIFICATION_CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH)

                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }
        }

        private fun createMusicServiceNotification(context: Context) : Notification
        {
            val intent: Intent = Intent(context, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                MUSIC_SERVICE_PENDING_INTENT_CODE,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification : Notification = NotificationCompat
                .Builder(context, MUSIC_SERVICE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(MUSIC_SERVICE_NOTIFICATION_TITLE)
                .setContentText("")
                .setFullScreenIntent(pendingIntent, true)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            return notification
        }
    }
}