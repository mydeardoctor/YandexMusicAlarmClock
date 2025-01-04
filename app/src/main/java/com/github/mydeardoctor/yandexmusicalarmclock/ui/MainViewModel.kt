package com.github.mydeardoctor.yandexmusicalarmclock.ui


import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.mydeardoctor.yandexmusicalarmclock.alarmclock.AlarmClockManager
import com.github.mydeardoctor.yandexmusicalarmclock.application.MyApplication
import com.github.mydeardoctor.yandexmusicalarmclock.data.MusicTrackLocalDataSource
import com.github.mydeardoctor.yandexmusicalarmclock.data.MusicTrackNetworkDataSource
import com.github.mydeardoctor.yandexmusicalarmclock.data.UiStateLocalDataSource
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import com.github.mydeardoctor.yandexmusicalarmclock.musicplayer.MusicService
import com.github.mydeardoctor.yandexmusicalarmclock.permissions.PermissionManager
import com.yandex.authsdk.YandexAuthLoginOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.ResponseBody


class MainViewModel(private val applicationContext: Context) : ViewModel()
{
    //Persistent.
    //By default all interactive UI elements are disabled.
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(value = UiState())
    public val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    //Not persistent. Resets when app closes and reopens.
    private val _loginStatus: MutableStateFlow<String> =
        MutableStateFlow(value = LOGIN_STATUS_DEFAULT)
    public val loginStatus: StateFlow<String> =
        _loginStatus.asStateFlow()

    init{
        //Read uiState from file.
        CoroutineScope(Dispatchers.IO).launch{
            val uiStateNew: UiState = UiStateLocalDataSource.readUiStateFile(
                context = applicationContext)
            _uiState.update{ uiStateNew }
        }
    }

    public fun onLoginClick(
        yandexLoginActivityLauncher: ActivityResultLauncher<YandexAuthLoginOptions>?)
    {
        //Check internet connection.
        val isInternetConnected: Boolean =
            PermissionManager.getIsInternetConnected(
                context = applicationContext)
        if(isInternetConnected == false)
        {
            PermissionManager.requestInternetConnection(
                context = applicationContext)
            return
        }

        //Launch Yandex login activity.
        val loginOptions: YandexAuthLoginOptions = YandexAuthLoginOptions()
        yandexLoginActivityLauncher?.launch(loginOptions)
    }

    public fun onSuccessfulLogin(token: String)
    {
        //Save acquired token.
        MusicTrackNetworkDataSource.token = token

        //Update UI.
        _loginStatus.update { LOGIN_STATUS_IN }
    }

    public fun onHoursChange(hoursString: String)
    {
        var hoursStringNew: String = ""

        //Determine if hoursString can be parsed.
        val hours: Int? = parseInteger(
            valueString = hoursString,
            valueMin = HOURS_MIN,
            valueMax = HOURS_MAX
        )
        if(hours != null)
        {
            hoursStringNew = hoursString
        }
        else
        {
            hoursStringNew = ""
        }

        //Update UI.
        val hoursStringOld: String = _uiState.value.hours
        if(hoursStringOld != hoursStringNew)
        {
            _uiState.update{ currentUiState ->
                currentUiState.copy(hours = hoursStringNew)
            }
        }
    }

    public fun onMinutesChange(minutesString: String)
    {
        var minutesStringNew: String = ""

        //Determine if minutesString can be parsed.
        val minutes: Int? = parseInteger(
            valueString = minutesString,
            valueMin = MINUTES_MIN,
            valueMax = MINUTES_MAX
        )
        if(minutes != null)
        {
            minutesStringNew = minutesString
        }
        else
        {
            minutesStringNew = ""
        }

        //Update UI.
        val minutesStringOld: String = _uiState.value.minutes
        if(minutesStringOld != minutesStringNew)
        {
            _uiState.update{ currentUiState ->
                currentUiState.copy(minutes = minutesStringNew)
            }
        }
    }

    private fun parseInteger(
        valueString: String,
        valueMin: Int,
        valueMax: Int) : Int?
    {
        //Check arguments.
        if(valueMin > valueMax)
        {
            return null
        }

        //Parse integer.
        try
        {
            val valueInt: Int = Integer.parseInt(valueString)
            if((valueInt >= valueMin) && (valueInt <= valueMax))
            {
                return valueInt
            }
            else
            {
                return null
            }
        }
        catch(e: NumberFormatException)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = applicationContext,
                    errorMessage = e.stackTraceToString())
            }
            return null
        }
    }

    public fun onSwitchToggle(isSwitchChecked: Boolean)
    {
        if(isSwitchChecked == true)
        {
            onSwitchCheck()
        }
        else
        {
            onSwitchUncheck()
        }
    }

    private fun onSwitchCheck()
    {
        //Disable interactive UI elements.
        _uiState.update{ currentUiState ->
            currentUiState.copy(
                isHoursEnabled = false,
                isMinutesEnabled = false,
                isSwitchEnabled = false,
                downloadStatus = DOWNLOAD_STATUS_DOWNLOADING,
                alarmClockStatus = ALARM_CLOCK_STATUS_TURNING_ON
            )
        }

        //Check internet connection.
        val isInternetConnected: Boolean =
            PermissionManager.getIsInternetConnected(
                context = applicationContext)
        if(isInternetConnected == false)
        {
            PermissionManager.requestInternetConnection(
                context = applicationContext)

            resetUi()
            return
        }

        //Check alarm clock permission.
        val isAlarmClockPermissionGranted: Boolean =
            PermissionManager.getIsAlarmClockPermissionGranted(
                context = applicationContext)
        if(isAlarmClockPermissionGranted == false)
        {
            PermissionManager.requestAlarmClockPermission(
                context = applicationContext)

            resetUi()
            return
        }

        //Check notifications permission.
        //Check full screen intent permission.
        val isNotificationPermissionGranted: Boolean =
            PermissionManager.getIsNotificationPermissionGranted(
                context = applicationContext)
        val isFullScreenIntentPermissionGranted: Boolean =
            PermissionManager.getIsFullScreenIntentPermissionGranted(
                context = applicationContext)
        if((isNotificationPermissionGranted == false) ||
           (isFullScreenIntentPermissionGranted == false))
        {
            PermissionManager.requestNotificationPermission(
                context = applicationContext)

            resetUi()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            //Download music track.
            val httpResponseBody: ResponseBody? =
                MusicTrackNetworkDataSource.downloadMusicTrack(
                    context = applicationContext)
            if(httpResponseBody == null)
            {
                resetUi()
                return@launch
            }

            //Save music track.
            val resultSaveMusicTrack: Boolean =
                MusicTrackLocalDataSource.updateMusicTrackFile(
                    context = applicationContext,
                    httpResponseBody = httpResponseBody)
            if(resultSaveMusicTrack == false)
            {
                resetUi()
                return@launch
            }

            //Update UI.
            _uiState.update{ currentUiState ->
                currentUiState.copy(downloadStatus = DOWNLOAD_STATUS_DOWNLOADED)
            }

            //Stop any existing alarm clock.
            val resultStopAlarmClock: Boolean =
                AlarmClockManager.stopAlarmClock(context = applicationContext)
            if(resultStopAlarmClock == false)
            {
                resetUi()
                return@launch
            }

            //Stop any existing music service.
            val resultStopMusicService: Boolean =
                MusicService.stopMusicService(context = applicationContext)
            if(resultStopMusicService == false)
            {
                resetUi()
                return@launch
            }

            //Parse hours.
            val hours: Int? = parseInteger(
                valueString = _uiState.value.hours,
                valueMin = HOURS_MIN,
                valueMax = HOURS_MAX
            )
            if(hours == null)
            {
                resetUi()
                return@launch
            }

            //Parse minutes.
            val minutes: Int? = parseInteger(
                valueString = _uiState.value.minutes,
                valueMin = MINUTES_MIN,
                valueMax = MINUTES_MAX
            )
            if(minutes == null)
            {
                resetUi()
                return@launch
            }

            //Start alarm clock.
            val resultStartAlarmClock: Boolean =
                AlarmClockManager.startAlarmClock(
                    context = applicationContext,
                    hours = hours,
                    minutes = minutes)
            if(resultStartAlarmClock == false)
            {
                resetUi()
                return@launch
            }

            //Save uiState to file and update UI.
            val uiStateNew: UiState = _uiState.value.copy(
                isSwitchEnabled = true,
                isSwitchChecked = true,
                alarmClockStatus = ALARM_CLOCK_STATUS_ON
            )
            UiStateLocalDataSource.updateUiStateFile(
                context = applicationContext,
                uiState = uiStateNew)
            _uiState.update { uiStateNew }
            return@launch
        }
    }

    private fun onSwitchUncheck()
    {
        //Disable interactive UI elements.
        _uiState.update{ currentUiState ->
            currentUiState.copy(
                isHoursEnabled = false,
                isMinutesEnabled = false,
                isSwitchEnabled = false
            )
        }

        //Stop any existing alarm clock.
        val resultStopAlarmClock: Boolean =
            AlarmClockManager.stopAlarmClock(context = applicationContext)
        //Stop any existing music service.
        val resultStopMusicService: Boolean =
            MusicService.stopMusicService(context = applicationContext)
        if((resultStopAlarmClock == false) || (resultStopMusicService == false))
        {
            _uiState.update{ currentUiState ->
                currentUiState.copy(isSwitchEnabled = true)
            }
            return
        }

        //Save uiState to file and update UI.
        CoroutineScope(Dispatchers.IO).launch {
            val uiStateNew: UiState = _uiState.value.copy(
                isHoursEnabled = true,
                isMinutesEnabled = true,
                isSwitchEnabled = true,
                isSwitchChecked = false,
                downloadStatus = DOWNLOAD_STATUS_NOT_DOWNLOADED,
                alarmClockStatus = ALARM_CLOCK_STATUS_OFF
            )
            UiStateLocalDataSource.updateUiStateFile(
                context = applicationContext,
                uiState = uiStateNew)
            _uiState.update { uiStateNew }
            return@launch
        }
    }

    private fun resetUi()
    {
        _uiState.update{ currentUiState ->
            currentUiState.copy(
                isHoursEnabled = true,
                isMinutesEnabled = true,
                isSwitchEnabled = true,
                isSwitchChecked = false,
                downloadStatus = DOWNLOAD_STATUS_NOT_DOWNLOADED,
                alarmClockStatus = ALARM_CLOCK_STATUS_OFF
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myApplicationReference: MyApplication =
                    (this[APPLICATION_KEY] as MyApplication)
                val applicationContext: Context =
                    myApplicationReference.applicationContext

                MainViewModel(applicationContext = applicationContext)
            }
        }
    }
}