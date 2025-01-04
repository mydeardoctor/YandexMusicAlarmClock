package com.github.mydeardoctor.yandexmusicalarmclock.ui

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mydeardoctor.yandexmusicalarmclock.version.VERSION
import com.yandex.authsdk.YandexAuthLoginOptions


@Preview(showSystemUi = true)
@Composable
private fun UserInterfacePreview()
{
    UserInterface(
        mainViewModel = MainViewModel(
            applicationContext = LocalContext.current.applicationContext),
        yandexLoginActivityLauncher = null,
        modifier = Modifier
    )
}

@Composable
public fun UserInterface(
    mainViewModel: MainViewModel,
    yandexLoginActivityLauncher: ActivityResultLauncher<YandexAuthLoginOptions>?,
    modifier: Modifier = Modifier)
{
    val appState: UiState by mainViewModel.uiState.collectAsState()
    val loginStatus: String by mainViewModel.loginStatus.collectAsState()

    val regexHours: Regex = Regex(
        pattern = "(^\$)|(^([0-2]|0[0-9]|1[0-9]|2[0-3])\$)")
    val regexMinutes: Regex = Regex(
        pattern = "(^\$)|(^([0-5]|[0-5][0-9])\$)")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()))
    {
        Row(horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 100.dp))
        {
            Button(
                onClick = {
                    mainViewModel.onLoginClick(
                        yandexLoginActivityLauncher = yandexLoginActivityLauncher)
                },
                modifier = Modifier.padding(end = 20.dp))
            {
                Text(
                    text = "Log in",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary)
            }

            Text(
                text = loginStatus,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
        }

        Row(horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 150.dp))
        {
            TextFieldWithNumbers(
                isEnabled = appState.isHoursEnabled,
                value = appState.hours,
                regex = regexHours,
                onValueChange = mainViewModel::onHoursChange,
                imeAction = ImeAction.Next)

            Text(
                text = ":",
                fontSize = 80.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp))

            TextFieldWithNumbers(
                isEnabled = appState.isMinutesEnabled,
                value = appState.minutes,
                regex = regexMinutes,
                onValueChange = mainViewModel::onMinutesChange,
                imeAction = ImeAction.Done)
        }

        Switch(
            enabled = appState.isSwitchEnabled,
            checked = appState.isSwitchChecked,
            onCheckedChange = { mainViewModel.onSwitchToggle(it) },
            modifier = Modifier
                .scale(1.8F)
                .padding(top = 30.dp))

        Text(
            text = appState.downloadStatus,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 50.dp))

        Text(
            text = appState.alarmClockStatus,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 10.dp))

        Text(
            text = VERSION,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 150.dp))
    }
}

@Composable
private fun TextFieldWithNumbers(
    isEnabled: Boolean,
    value: String,
    regex: Regex,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction)
{
    TextField(
        enabled = isEnabled,
        value = value,
        onValueChange = {
            if(it.matches(regex))
            {
                onValueChange(it)
            }
        },
        textStyle = TextStyle(
            fontSize = 50.sp,
            textAlign = TextAlign.Center,
            color =
                if(isEnabled == true)
                {
                    MaterialTheme.colorScheme.onBackground
                }
                else
                {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                }
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        modifier = Modifier
            .onFocusChanged {
                if(it.isFocused)
                {
                    onValueChange("")
                }
            }
            .size(120.dp)
    )
}