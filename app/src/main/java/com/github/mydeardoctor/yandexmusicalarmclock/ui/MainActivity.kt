package com.github.mydeardoctor.yandexmusicalarmclock.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import com.github.mydeardoctor.yandexmusicalarmclock.ui.theme.YandexMusicAlarmClockTheme
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk


class MainActivity : ComponentActivity()
{
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Prepare Yandex Login activity launcher.
        //https://yandex.ru/dev/id/doc/ru/mobileauthsdk/android/3.1.3/sdk-android-use
        val sdk: YandexAuthSdk = YandexAuthSdk.create(
            options = YandexAuthOptions(context = applicationContext))
        val launcher: ActivityResultLauncher<YandexAuthLoginOptions> =
            registerForActivityResult(contract = sdk.contract) { result ->
            if(result is YandexAuthResult.Success)
            {
                mainViewModel.onSuccessfulLogin(token = result.token.value)
            }
        }

        setContent {
            YandexMusicAlarmClockTheme {
                UserInterface(
                    mainViewModel = mainViewModel,
                    yandexLoginActivityLauncher = launcher,
                    modifier = Modifier)
            }
        }
    }
}