# Yandex Music Alarm Clock

This is an Android application written in Kotlin
that plays a random liked music track from Yandex Music
when an alarm clock goes off.

Communication with Yandex servers is based on
[unofficial reverse engineered Yandex Music API][Yandex_Music_API_link].

## Requirements

* [Windows 10][Windows_10_link].
* [Android Studio][Android_Studio_link].
* Android SDK Platform 35.

## Workflow

### Open project

* Open Android Studio.

    ![1_Open_Project_text][1_Open_Project_link]

* Click "Open" and select the path to the project.

    ![2_Open_Project_text][2_Open_Project_link]

* Click "OK".

    ![3_Open_Project_text][3_Open_Project_link]

    The project is opened successfully.

### Build project

* Navigate to "Main Menu" -> "Build" and select "Make Project".

    ![4_Build_Project_text][4_Build_Project_link]

* Click on the "Build" icon to show the build output.

    ![5_Build_Project_text][5_Build_Project_link]

    The project is built successfully.

### Build APK

* Navigate to "Main Menu" -> "Build" -> "Build App Bundle(s) / APK(s)"
and select "Build APK(s)".

    ![6_Build_APK_text][6_Build_APK_link]

* APK is built successfully.

    ![7_Build_APK_text][7_Build_APK_link]

* APK is located in app\build\outputs\apk\debug\app-debug.apk

    ![8_Build_APK_text][8_Build_APK_link]

## How to use

* Install APK on your Android device.

* Open the application.

    ![9_How_to_use_text][9_How_to_use_link]

* To download a random liked music track from Yandex Music
you must login to your Yandex account. Click "Log in" button.

* If there is no internet connection, you will be redirected
to "Internet" settings screen.

    ![10_How_to_use_text][10_How_to_use_link]

    Connect to the internet.

    ![11_How_to_use_text][11_How_to_use_link]

    Return to the application screen and click "Log in" button again.

* You will be redirected to Yandex ID Login screen.

    ![12_How_to_use_text][12_How_to_use_link]

    Login to your Yandex account.
    
    As soon as you login to your Yandex account,
    you will be redirected back to the application screen.

* Enter the time for the alarm clock to go off.

    ![13_How_to_use_text][13_How_to_use_link]

* Check the switch.

* If there is no internet connection, you will be redirected
to "Internet" settings screen.

    ![14_How_to_use_text][14_How_to_use_link]

    Connect to the internet.

    ![15_How_to_use_text][15_How_to_use_link]

    Return to the application screen and check the switch again.

* If this is the first time you use this application,
you need to allow the application to set alarms.
You will be redirected to "Alarms & reminders" screen.

    ![16_How_to_use_text][16_How_to_use_link]

    Click on "YandexMusicAlarmClock".

    ![17_How_to_use_text][17_How_to_use_link]

    Check the switch to allow the application to set alarms.
    ![18_How_to_use_text][18_How_to_use_link]

    Return to the application screen and check the switch again.

* If this is the first time you use this application,
you need to allow the application to post notifications.
You will be redirected to "YandexMusicAlarmClock" notification settings screen.

    ![19_How_to_use_text][19_How_to_use_link]

    Check the switch to allow the application to post notifications.

    ![20_How_to_use_text][20_How_to_use_link]

    Return to the application screen and check the switch again.


* After you check the switch, wait for a music track to download
and alarm clock to turn on.

    ![21_How_to_use_text][21_How_to_use_link]
    
* After that you can close the application and turn off the screen
of your device.
    
    ![22_How_to_use_text][22_How_to_use_link]

* When the alarm clock goes off and a music track starts to play,
turn on the screen of your device. The application will be opened.
    
    ![23_How_to_use_text][23_How_to_use_link]

* Uncheck the switch to turn off the alarm clock and stop a music track.
    
    ![24_How_to_use_text][24_How_to_use_link]




[Yandex_Music_API_link]: https://github.com/MarshalX/yandex-music-api

[Windows_10_link]: https://www.microsoft.com/software-download/windows10
[Android_Studio_link]: https://developer.android.com/studio

[1_Open_Project_link]: images/1_Open_Project.png
[2_Open_Project_link]: images/2_Open_Project.png
[3_Open_Project_link]: images/3_Open_Project.png
[4_Build_Project_link]: images/4_Build_Project.png
[5_Build_Project_link]: images/5_Build_Project.png
[6_Build_APK_link]: images/6_Build_APK.png
[7_Build_APK_link]: images/7_Build_APK.png
[8_Build_APK_link]: images/8_Build_APK.png
[9_How_to_use_link]: images/9_How_to_use.png
[10_How_to_use_link]: images/10_How_to_use.png
[11_How_to_use_link]: images/11_How_to_use.png
[12_How_to_use_link]: images/12_How_to_use.png
[13_How_to_use_link]: images/13_How_to_use.png
[14_How_to_use_link]: images/14_How_to_use.png
[15_How_to_use_link]: images/15_How_to_use.png
[16_How_to_use_link]: images/16_How_to_use.png
[17_How_to_use_link]: images/17_How_to_use.png
[18_How_to_use_link]: images/18_How_to_use.png
[19_How_to_use_link]: images/19_How_to_use.png
[20_How_to_use_link]: images/20_How_to_use.png
[21_How_to_use_link]: images/21_How_to_use.png
[22_How_to_use_link]: images/22_How_to_use.png
[23_How_to_use_link]: images/23_How_to_use.png
[24_How_to_use_link]: images/24_How_to_use.png