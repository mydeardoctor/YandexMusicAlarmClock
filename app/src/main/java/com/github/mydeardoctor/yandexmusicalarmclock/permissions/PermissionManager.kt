package com.github.mydeardoctor.yandexmusicalarmclock.permissions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object PermissionManager
{
    public fun getIsInternetConnected(context: Context) : Boolean
    {
        var isInternetConnected: Boolean = false

        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork: Network? = connectivityManager.activeNetwork
        if(activeNetwork == null)
        {
            isInternetConnected = false
            return isInternetConnected
        }

        val networkCapabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(activeNetwork)
        if(networkCapabilities == null)
        {
            isInternetConnected = false
            return isInternetConnected
        }

        isInternetConnected = networkCapabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return isInternetConnected
    }

    public fun requestInternetConnection(context: Context)
    {
        val isInternetConnected : Boolean =
            getIsInternetConnected(context = context)

        if(isInternetConnected == false)
        {
            val intent: Intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try
            {
                context.startActivity(intent)
            }
            catch(e: ActivityNotFoundException)
            {
                Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }
        }
    }

    public fun getIsAlarmClockPermissionGranted(context: Context) : Boolean
    {
        var isAlarmClockPermissionGranted: Boolean = true

        //https://developer.android.com/develop/background-work/services/alarms/schedule
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            isAlarmClockPermissionGranted = alarmManager.canScheduleExactAlarms()
        }

        return isAlarmClockPermissionGranted
    }

    public fun requestAlarmClockPermission(context: Context)
    {
        val isAlarmClockPermissionGranted: Boolean =
            getIsAlarmClockPermissionGranted(context = context)

        if((isAlarmClockPermissionGranted == false) &&
           (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))
        {
            val intent: Intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            try
            {
                context.startActivity(intent)
            }
            catch(e: ActivityNotFoundException)
            {
                Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }
        }
    }

    public fun getIsNotificationPermissionGranted(context: Context) : Boolean
    {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isNotificationPermissionGranted: Boolean =
            notificationManager.areNotificationsEnabled()
        return isNotificationPermissionGranted
    }

    public fun getIsFullScreenIntentPermissionGranted(context: Context) : Boolean
    {
        var isFullScreenIntentPermissionGranted: Boolean = true

        //https://source.android.com/docs/core/permissions/fsi-limits
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            isFullScreenIntentPermissionGranted =
                notificationManager.canUseFullScreenIntent()
        }

        return isFullScreenIntentPermissionGranted
    }

    public fun requestNotificationPermission(context: Context)
    {
        val isNotificationPermissionGranted: Boolean =
            getIsNotificationPermissionGranted(context = context)
        val isFullScreenIntentPermissionGranted: Boolean =
            getIsFullScreenIntentPermissionGranted(context = context)

        if(((isNotificationPermissionGranted == false) || (isFullScreenIntentPermissionGranted == false))
           && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
        {
            val intent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            try
            {
                context.startActivity(intent)
            }
            catch(e: ActivityNotFoundException)
            {
                Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }
        }
    }
}