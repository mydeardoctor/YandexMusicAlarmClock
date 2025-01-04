package com.github.mydeardoctor.yandexmusicalarmclock.application

import android.app.Application
import com.github.mydeardoctor.yandexmusicalarmclock.musicplayer.MusicService


class MyApplication : Application()
{
    override fun onCreate()
    {
        super.onCreate()

        MusicService.createMusicServiceNotificationChannel(
            context = applicationContext)
    }
}