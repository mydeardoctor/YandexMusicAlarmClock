package com.github.mydeardoctor.yandexmusicalarmclock.musicplayer

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.github.mydeardoctor.yandexmusicalarmclock.R
import com.github.mydeardoctor.yandexmusicalarmclock.data.DataSource
import com.github.mydeardoctor.yandexmusicalarmclock.data.MusicTrackLocalDataSource
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


object MusicPlayer
{
    private var musicPlayer: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    public fun startPlaying(context: Context)
    {
        val handler: Handler = Handler(Looper.getMainLooper())
        handler.post{

            //Create musicPlayer.
            if(musicPlayer == null)
            {
                try
                {
                    musicPlayer = ExoPlayer
                        .Builder(context)
                        .setLooper(Looper.getMainLooper())
                        .build()
                }
                catch(e: IllegalStateException)
                {
                    Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                    CoroutineScope(Dispatchers.IO).launch{
                        Logger.updateLogFile(
                            context = context,
                            errorMessage = e.stackTraceToString())
                    }
                    return@post
                }
            }


            //Stop playing.
            musicPlayer!!.stop()


            //Create musicTrack folder handle.
            val musicTrackFolderHandle: File? =
                MusicTrackLocalDataSource.generateMusicTrackFolderHandle(
                    context = context)
            if(musicTrackFolderHandle == null)
            {
                return@post
            }

            //Create musicTrack file handle.
            val musicTrackFileHandle: File? =
                MusicTrackLocalDataSource.generateMusicTrackFileHandle(
                    context = context,
                    musicTrackFolderHandle = musicTrackFolderHandle)
            if(musicTrackFileHandle == null)
            {
                return@post
            }

            //Set music track.
            var mediaItem: MediaItem? = null
            //If music track does not exist. Use default music track.
            if(!DataSource.doesFolderOrFileExist(
                    context = context,
                    folderOrFileHandle = musicTrackFileHandle,
                    withLogging = true))
            {
                try
                {
                    @RawRes
                    val resourceId: Int = R.raw.music_track_default
                    val uriDefault: Uri = Uri
                        .Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(context.resources.getResourcePackageName(resourceId))
                        .appendPath(context.resources.getResourceTypeName(resourceId))
                        .appendPath(context.resources.getResourceEntryName(resourceId))
                        .build()
                    mediaItem = MediaItem.fromUri(uriDefault)
                }
                catch(e: Exception)
                {
                    Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                    CoroutineScope(Dispatchers.IO).launch{
                        Logger.updateLogFile(
                            context = context,
                            errorMessage = e.stackTraceToString())
                    }

                    if((e !is UnsupportedOperationException) &&
                       (e !is Resources.NotFoundException))
                    {
                        throw e
                    }

                    return@post
                }
            }
            //If music track exists.
            else
            {
                try
                {
                    mediaItem = MediaItem.fromUri(Uri.fromFile(musicTrackFileHandle))
                }
                catch(e: NullPointerException)
                {
                    Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                    CoroutineScope(Dispatchers.IO).launch{
                        Logger.updateLogFile(
                            context = context,
                            errorMessage = e.stackTraceToString())
                    }
                    return@post
                }
            }
            musicPlayer!!.setMediaItem(mediaItem)


            //Start playing.
            musicPlayer!!.repeatMode = Player.REPEAT_MODE_ONE
            musicPlayer!!.prepare()
            musicPlayer!!.play()
        }
    }

    @OptIn(UnstableApi::class)
    public fun stopPlaying(context: Context)
    {
        val handler: Handler = Handler(Looper.getMainLooper())
        handler.post {

            //Create musicPlayer.
            if(musicPlayer == null)
            {
                try
                {
                    musicPlayer = ExoPlayer
                        .Builder(context)
                        .setLooper(Looper.getMainLooper())
                        .build()
                }
                catch(e: IllegalStateException)
                {
                    Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
                    CoroutineScope(Dispatchers.IO).launch{
                        Logger.updateLogFile(
                            context = context,
                            errorMessage = e.stackTraceToString())
                    }
                    return@post
                }
            }

            //Stop playing.
            musicPlayer!!.stop()
            musicPlayer!!.release()
            musicPlayer = null
        }
    }
}

