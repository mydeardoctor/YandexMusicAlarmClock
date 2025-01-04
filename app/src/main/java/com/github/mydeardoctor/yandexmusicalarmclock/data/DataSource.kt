package com.github.mydeardoctor.yandexmusicalarmclock.data

import android.content.Context
import android.util.Log
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object DataSource
{
    public fun doesFolderOrFileExist(
        context: Context,
        folderOrFileHandle: File,
        withLogging: Boolean) : Boolean
    {
        try
        {
            val result: Boolean = folderOrFileHandle.exists()
            return result
        }
        catch(e: SecurityException)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())

            if(withLogging == true)
            {
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }

            return false
        }
    }

    public fun generateFolderHandle(
        context: Context,
        parent: File?,
        folderName: String,
        withLogging: Boolean) : File?
    {
        if(parent == null)
        {
            return null
        }

        try
        {
            val folderHandle: File = File(parent, folderName)
            return folderHandle
        }
        catch(e: NullPointerException)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())

            if(withLogging == true)
            {
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }

            return null
        }
    }

    public fun generateFileHandle(
        context: Context,
        folderHandle: File,
        fileName: String,
        withLogging: Boolean) : File?
    {
        try
        {
            val fileHandle: File = File(folderHandle, fileName)
            return fileHandle
        }
        catch(e: NullPointerException)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())

            if(withLogging == true)
            {
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }

            return null
        }
    }

    public fun deleteFolderOrFile(
        context: Context,
        folderOrFileHandle: File,
        withLogging: Boolean) : Boolean
    {
        if(doesFolderOrFileExist(
                context = context,
                folderOrFileHandle = folderOrFileHandle,
                withLogging = withLogging)
        )
        {
            try
            {
                val result: Boolean = folderOrFileHandle.delete()
                return result
            }
            catch(e: SecurityException)
            {
                Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())

                if(withLogging == true)
                {
                    CoroutineScope(Dispatchers.IO).launch{
                        Logger.updateLogFile(
                            context = context,
                            errorMessage = e.stackTraceToString())
                    }
                }

                return false
            }
        }
        else
        {
            return true
        }
    }

    public fun createFolder(
        context: Context,
        folderHandle: File,
        withLogging: Boolean) : Boolean
    {
        if(!doesFolderOrFileExist(
                context = context,
                folderOrFileHandle = folderHandle,
                withLogging = withLogging)
        )
        {
            try
            {
                val result: Boolean = folderHandle.mkdirs()
                return result
            }
            catch(e: SecurityException)
            {
                Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())

                if(withLogging == true)
                {
                    CoroutineScope(Dispatchers.IO).launch{
                        Logger.updateLogFile(
                            context = context,
                            errorMessage = e.stackTraceToString())
                    }
                }

                return false
            }
        }
        else
        {
            return true
        }
    }

    public fun writeTextToFile(
        context: Context,
        text: String,
        fileHandle: File,
        append: Boolean,
        withLogging: Boolean) : Boolean
    {
        try
        {
            FileOutputStream(fileHandle, append).use { outputStream ->
                outputStream.write(text.toByteArray())
            }
            return true
        }
        catch(e: Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())

            if(withLogging == true)
            {
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }

            if((e !is FileNotFoundException) &&
               (e !is SecurityException) &&
               (e !is IOException))
            {
                throw e
            }

            return false
        }
    }

    public fun writeHttpResponseBodyToFile(
        context: Context,
        httpResponseBody: ResponseBody,
        fileHandle: File,
        withLogging: Boolean) : Boolean
    {
        try
        {
            httpResponseBody.byteStream().use { inputStream ->
                fileHandle.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return true
        }
        catch(e: Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())

            if(withLogging == true)
            {
                CoroutineScope(Dispatchers.IO).launch{
                    Logger.updateLogFile(
                        context = context,
                        errorMessage = e.stackTraceToString())
                }
            }

            throw e

            return false
        }
    }
}