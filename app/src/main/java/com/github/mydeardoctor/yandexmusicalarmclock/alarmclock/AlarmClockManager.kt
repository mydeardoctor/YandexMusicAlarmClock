package com.github.mydeardoctor.yandexmusicalarmclock.alarmclock

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Log
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import com.github.mydeardoctor.yandexmusicalarmclock.permissions.PermissionManager
import com.github.mydeardoctor.yandexmusicalarmclock.ui.HOURS_MAX
import com.github.mydeardoctor.yandexmusicalarmclock.ui.HOURS_MIN
import com.github.mydeardoctor.yandexmusicalarmclock.ui.MINUTES_MAX
import com.github.mydeardoctor.yandexmusicalarmclock.ui.MINUTES_MIN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object AlarmClockManager
{
    private const val ALARM_PENDING_INTENT_CODE: Int = 1

    fun startAlarmClock(context: Context, hours: Int, minutes: Int): Boolean
    {
        //Check alarm clock permission.
        val isAlarmClockPermissionGranted: Boolean =
            PermissionManager.getIsAlarmClockPermissionGranted(
                context = context)
        if(isAlarmClockPermissionGranted == false)
        {
            return false
        }

        //Stop any existing alarm clock.
        val resultStopAlarmClock: Boolean = stopAlarmClock(context = context)
        if(resultStopAlarmClock == false)
        {
            return false
        }

        //Check arguments.
        if((hours < HOURS_MIN) ||
           (hours > HOURS_MAX) ||
           (minutes < MINUTES_MIN) ||
           (minutes > MINUTES_MAX))
        {
            return false
        }

        //Create alarmManager and pendingIntent.
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent: PendingIntent = createPendingIntent(
            context = context)

        //Set time.
        val systemTime: Long = System.currentTimeMillis()
        val timeZone: TimeZone = TimeZone.getDefault()
        val calendar: Calendar = Calendar.getInstance(timeZone)
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var alarmClockTime: Long = calendar.timeInMillis
        if(alarmClockTime < systemTime)
        {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            alarmClockTime = calendar.timeInMillis
        }

        //Create alarmClockInfo.
        val alarmClockInfo: AlarmClockInfo = AlarmClockInfo(
            alarmClockTime,
            pendingIntent)

        //Start alarm clock.
        try
        {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            return true
        }
        catch(e: SecurityException)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }
            return false
        }
    }

    fun stopAlarmClock(context: Context): Boolean
    {
        //Check alarm clock permission.
        val isAlarmClockPermissionGranted: Boolean =
            PermissionManager.getIsAlarmClockPermissionGranted(
                context = context)
        if(isAlarmClockPermissionGranted == false)
        {
            return false
        }

        //Create alarmManager and pendingIntent.
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent: PendingIntent = createPendingIntent(
            context = context)

        //Stop any existing alarm clock.
        try
        {
            alarmManager.cancel(pendingIntent)
            return true
        }
        catch(e: SecurityException)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }
            return false
        }
    }

    private fun createPendingIntent(context: Context) : PendingIntent
    {
        val intent: Intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_PENDING_INTENT_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        return pendingIntent
    }
}