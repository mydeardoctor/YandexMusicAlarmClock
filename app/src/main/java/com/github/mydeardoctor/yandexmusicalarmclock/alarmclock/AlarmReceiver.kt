package com.github.mydeardoctor.yandexmusicalarmclock.alarmclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.mydeardoctor.yandexmusicalarmclock.musicplayer.MusicService


class AlarmReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent)
    {
        val resultStopMusicService: Boolean = MusicService.stopMusicService(
            context = context.applicationContext)
        val resultStartMusicService: Boolean = MusicService.startMusicService(
            context = context.applicationContext)
    }
}