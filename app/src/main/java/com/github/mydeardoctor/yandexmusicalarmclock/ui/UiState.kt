package com.github.mydeardoctor.yandexmusicalarmclock.ui

import kotlinx.serialization.Serializable


public const val HOURS_MIN: Int = 0
public const val HOURS_MAX: Int = 23
public const val MINUTES_MIN: Int = 0
public const val MINUTES_MAX: Int = 59
public const val DOWNLOAD_STATUS_NOT_DOWNLOADED: String = "Music track is NOT downloaded."
public const val DOWNLOAD_STATUS_DOWNLOADING: String = "Music track is downloading."
public const val DOWNLOAD_STATUS_DOWNLOADED: String = "Music track is downloaded."
public const val ALARM_CLOCK_STATUS_OFF: String = "Alarm clock is OFF."
public const val ALARM_CLOCK_STATUS_TURNING_ON: String = "Alarm clock is turning on."
public const val ALARM_CLOCK_STATUS_ON: String = "Alarm clock is ON."
private const val LOGIN_STATUS_OUT: String = "Logged OUT."
public const val LOGIN_STATUS_IN: String = "Logged IN."

private const val IS_HOURS_ENABLED_DEFAULT: Boolean = false
private const val HOURS_DEFAULT: String = "00"
private const val IS_MINUTES_ENABLED_DEFAULT: Boolean = false
private const val MINUTES_DEFAULT: String = "00"
private const val IS_SWITCH_ENABLED_DEFAULT: Boolean = false
private const val IS_SWITCH_CHECKED_DEFAULT: Boolean = false
private const val DOWNLOAD_STATUS_DEFAULT: String = DOWNLOAD_STATUS_NOT_DOWNLOADED
private const val ALARM_CLOCK_STATUS_DEFAULT: String = ALARM_CLOCK_STATUS_OFF
public const val LOGIN_STATUS_DEFAULT: String = LOGIN_STATUS_OUT


@Serializable
data class UiState(
    val isHoursEnabled: Boolean = IS_HOURS_ENABLED_DEFAULT,
    val hours: String = HOURS_DEFAULT,
    val isMinutesEnabled: Boolean = IS_MINUTES_ENABLED_DEFAULT,
    val minutes: String = MINUTES_DEFAULT,
    val isSwitchEnabled: Boolean = IS_SWITCH_ENABLED_DEFAULT,
    val isSwitchChecked: Boolean = IS_SWITCH_CHECKED_DEFAULT,
    val downloadStatus: String = DOWNLOAD_STATUS_DEFAULT,
    val alarmClockStatus: String = ALARM_CLOCK_STATUS_DEFAULT
)