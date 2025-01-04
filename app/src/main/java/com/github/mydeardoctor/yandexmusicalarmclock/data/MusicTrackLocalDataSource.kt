package com.github.mydeardoctor.yandexmusicalarmclock.data

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.ResponseBody
import java.io.File


object MusicTrackLocalDataSource
{
    private const val MUSIC_TRACK_FOLDER_NAME: String = "musicTrackFolder"
    private const val MUSIC_TRACK_FILE_NAME: String = "musicTrackFile.mp3"
    private val mutexMusicTrackFile: Mutex = Mutex()

    public suspend fun updateMusicTrackFile(
        context: Context,
        httpResponseBody: ResponseBody) : Boolean
    {
        mutexMusicTrackFile.withLock {
            //Create musicTrack folder handle.
            val musicTrackFolderHandle: File? = generateMusicTrackFolderHandle(
                context = context)
            if(musicTrackFolderHandle == null)
            {
                return false
            }

            //Create musicTrack file handle.
            val musicTrackFileHandle: File? = generateMusicTrackFileHandle(
                context = context,
                musicTrackFolderHandle = musicTrackFolderHandle)
            if(musicTrackFileHandle == null)
            {
                return false
            }

            //Create musicTrack folder, if it does not exist.
            if(!DataSource.doesFolderOrFileExist(
                    context = context,
                    folderOrFileHandle = musicTrackFolderHandle,
                    withLogging = true
                )
            )
            {
                val resultCreateFolder: Boolean = DataSource.createFolder(
                    context = context,
                    folderHandle = musicTrackFolderHandle,
                    withLogging = true
                )
                if(resultCreateFolder == false)
                {
                    return false
                }
            }

            //Delete old musicTrack file, if it exists.
            if(DataSource.doesFolderOrFileExist(
                    context = context,
                    folderOrFileHandle = musicTrackFileHandle,
                    withLogging = true
                )
            )
            {
                val resultDeleteFile: Boolean = DataSource.deleteFolderOrFile(
                    context = context,
                    folderOrFileHandle = musicTrackFileHandle,
                    withLogging = true
                )
                if(resultDeleteFile == false)
                {
                    return false
                }
            }

            //Write http response body to a new music track file.
            val resultWrite: Boolean = DataSource.writeHttpResponseBodyToFile(
                context = context,
                httpResponseBody = httpResponseBody,
                fileHandle = musicTrackFileHandle,
                withLogging = true
            )
            if(resultWrite == false)
            {
                return false
            }

            return true
        }
    }

    public fun generateMusicTrackFolderHandle(context: Context) : File?
    {
        return DataSource.generateFolderHandle(
            context = context,
            parent = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            folderName = MUSIC_TRACK_FOLDER_NAME,
            withLogging = true
        )
    }

    public fun generateMusicTrackFileHandle(
        context: Context,
        musicTrackFolderHandle: File) : File?
    {
        return DataSource.generateFileHandle(
            context = context,
            folderHandle = musicTrackFolderHandle,
            fileName = MUSIC_TRACK_FILE_NAME,
            withLogging = true
        )
    }
}