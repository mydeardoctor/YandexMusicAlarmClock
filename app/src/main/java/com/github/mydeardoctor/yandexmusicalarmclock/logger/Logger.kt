package com.github.mydeardoctor.yandexmusicalarmclock.logger

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Environment
import com.github.mydeardoctor.yandexmusicalarmclock.data.DataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.Date
import java.util.Locale


object Logger
{
    public const val LOGCAT_TAG: String = "MyApplication"

    private const val LOG_FOLDER_NAME: String = "logFolder"
    private const val LOG_FILE_NAME: String = "log.txt"
    private val mutexLogFile: Mutex = Mutex()
    private var firstTime: Boolean = true

    public suspend fun updateLogFile(context: Context, errorMessage: String)
    {
        mutexLogFile.withLock {
            //Create log folder handle.
            val logFolderHandle: File? = generateLogFolderHandle(
                context = context)
            if(logFolderHandle == null)
            {
                return@withLock
            }

            //Create log file handle.
            val logFileHandle: File? = generateLogFileHandle(
                context = context,
                logFolderHandle = logFolderHandle)
            if(logFileHandle == null)
            {
                return@withLock
            }

            //Create log folder, if it does not exist.
            if(!DataSource.doesFolderOrFileExist(
                context = context,
                folderOrFileHandle = logFolderHandle,
                withLogging = false))
            {
                val resultCreateFolder: Boolean = DataSource.createFolder(
                    context = context,
                    folderHandle = logFolderHandle,
                    withLogging = false)
                if(resultCreateFolder == false)
                {
                    return@withLock
                }
            }

            //Delete old logFile, if it exists.
            if((firstTime == true) &&
               (DataSource.doesFolderOrFileExist(
                   context = context,
                   folderOrFileHandle = logFileHandle,
                   withLogging = false)))
            {
                firstTime = false
                val resultDeleteFile: Boolean = DataSource.deleteFolderOrFile(
                    context = context,
                    folderOrFileHandle = logFileHandle,
                    withLogging = false)
                if(resultDeleteFile == false)
                {
                    return@withLock
                }
            }

            //Prepare a message with a timestamp.
            val dateFormat: SimpleDateFormat = SimpleDateFormat(
                "dd-MM-yyyy HH:mm:ss",
                Locale.getDefault())
            val timestamp: String = dateFormat.format(Date())
            val message: String = "${timestamp}:\n${errorMessage}\n\n"

            //Write a message to log file.
            val resultWrite: Boolean = DataSource.writeTextToFile(
                context = context,
                text = message,
                fileHandle = logFileHandle,
                append = true,
                withLogging = false)
            if(resultWrite == false)
            {
                return@withLock
            }
        }
    }

    private fun generateLogFolderHandle(context: Context) : File?
    {
        return DataSource.generateFolderHandle(
            context = context,
            parent = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            folderName = LOG_FOLDER_NAME,
            withLogging = false)
    }

    private fun generateLogFileHandle(
        context: Context,
        logFolderHandle: File) : File?
    {
        return DataSource.generateFileHandle(
            context = context,
            folderHandle = logFolderHandle,
            fileName = LOG_FILE_NAME,
            withLogging = false)
    }
}