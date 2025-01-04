package com.github.mydeardoctor.yandexmusicalarmclock.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import com.github.mydeardoctor.yandexmusicalarmclock.ui.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


object UiStateLocalDataSource
{
    private const val UI_STATE_FOLDER_NAME: String = "uiStateFolder"
    private const val UI_STATE_FILE_NAME: String = "uiStateFile.json"
    private val mutexUiStateFile: Mutex = Mutex()
    private var jsonEncoderDecoder: Json? = null

    public suspend fun readUiStateFile(context: Context) : UiState
    {
        //If this function fails,
        //it must return a uiState with interactive elements enabled.

        mutexUiStateFile.withLock {
            val uiStateWithInteractiveElementsEnabled: UiState = UiState(
                isHoursEnabled = true,
                isMinutesEnabled = true,
                isSwitchEnabled = true)

            //Create uiState folder handle.
            val uiStateFolderHandle: File? = generateUiStateFolderHandle(
                context = context)
            if(uiStateFolderHandle == null)
            {
                return uiStateWithInteractiveElementsEnabled
            }

            //Create uiState file handle.
            val uiStateFileHandle: File? = generateUiStateFileHandle(
                context = context,
                uiStateFolderHandle = uiStateFolderHandle)
            if(uiStateFileHandle == null)
            {
                return uiStateWithInteractiveElementsEnabled
            }

            //Create uiState folder, if it does not exist.
            if(!DataSource.doesFolderOrFileExist(
                    context = context,
                    folderOrFileHandle = uiStateFolderHandle,
                    withLogging = true
                )
            )
            {
                val resultCreateFolder: Boolean = DataSource.createFolder(
                    context = context,
                    folderHandle = uiStateFolderHandle,
                    withLogging = true
                )
                if(resultCreateFolder == false)
                {
                    return uiStateWithInteractiveElementsEnabled
                }
            }

            //Create uiState file, if it does not exist.
            if(!DataSource.doesFolderOrFileExist(
                    context = context,
                    folderOrFileHandle = uiStateFileHandle,
                    withLogging = true
                )
            )
            {
                //Prepare default json.
                val jsonDefault: String? = uiStateToJson(
                    context = context,
                    uiState = uiStateWithInteractiveElementsEnabled)
                if(jsonDefault == null)
                {
                    return uiStateWithInteractiveElementsEnabled
                }

                //Write default json to uiStateFile.
                val resultWrite: Boolean = DataSource.writeTextToFile(
                    context = context,
                    text = jsonDefault,
                    fileHandle = uiStateFileHandle,
                    append = false,
                    withLogging = true
                )
                if(resultWrite == false)
                {
                    return uiStateWithInteractiveElementsEnabled
                }
            }

            //Read json from uiStateFile.
            val json: String = uiStateFileHandle.readText()

            //Parse json.
            val uiState: UiState? = jsonToUiState(
                context = context,
                json = json)
            if(uiState != null)
            {
                return uiState
            }
            else
            {
                //Delete old uiStateFile, if it exists.
                if(DataSource.doesFolderOrFileExist(
                        context = context,
                        folderOrFileHandle = uiStateFileHandle,
                        withLogging = true
                    )
                )
                {
                    val resultDeleteFile: Boolean = DataSource.deleteFolderOrFile(
                        context = context,
                        folderOrFileHandle = uiStateFileHandle,
                        withLogging = true
                    )
                    if(resultDeleteFile == false)
                    {
                        return uiStateWithInteractiveElementsEnabled
                    }
                }

                //Prepare default json.
                val jsonDefault: String? = uiStateToJson(
                    context = context,
                    uiState = uiStateWithInteractiveElementsEnabled)
                if(jsonDefault == null)
                {
                    return uiStateWithInteractiveElementsEnabled
                }

                //Write default json to uiStateFile.
                val resultWrite: Boolean = DataSource.writeTextToFile(
                    context = context,
                    text = jsonDefault,
                    fileHandle = uiStateFileHandle,
                    append = false,
                    withLogging = true
                )
                if(resultWrite == false)
                {
                    return uiStateWithInteractiveElementsEnabled
                }

                return uiStateWithInteractiveElementsEnabled
            }
        }
    }

    public suspend fun updateUiStateFile(context: Context, uiState: UiState)
    {
        mutexUiStateFile.withLock {
            //Create uiState folder handle.
            val uiStateFolderHandle: File? = generateUiStateFolderHandle(
                context = context)
            if(uiStateFolderHandle == null)
            {
                return@withLock
            }

            //Create uiState file handle.
            val uiStateFileHandle: File? = generateUiStateFileHandle(
                context = context,
                uiStateFolderHandle = uiStateFolderHandle)
            if(uiStateFileHandle == null)
            {
                return@withLock
            }

            //Create uiState folder, if it does not exist.
            if(!DataSource.doesFolderOrFileExist(
                    context = context,
                    folderOrFileHandle = uiStateFolderHandle,
                    withLogging = true
                )
            )
            {
                val resultCreateFolder: Boolean = DataSource.createFolder(
                    context = context,
                    folderHandle = uiStateFolderHandle,
                    withLogging = true
                )
                if(resultCreateFolder == false)
                {
                    return@withLock
                }
            }

            //Delete old uiStateFile, if it exists.
            if(DataSource.doesFolderOrFileExist(
                    context = context,
                    folderOrFileHandle = uiStateFileHandle,
                    withLogging = true
                )
            )
            {
                val resultDeleteFile: Boolean = DataSource.deleteFolderOrFile(
                    context = context,
                    folderOrFileHandle = uiStateFileHandle,
                    withLogging = true
                )
                if(resultDeleteFile == false)
                {
                    return@withLock
                }
            }

            //Prepare json.
            val json: String? = uiStateToJson(
                context = context,
                uiState = uiState)
            if(json == null)
            {
                return@withLock
            }

            //Write json to a new uiStateFile.
            val resultWrite: Boolean = DataSource.writeTextToFile(
                context = context,
                text = json,
                fileHandle = uiStateFileHandle,
                append = false,
                withLogging = true
            )
            if(resultWrite == false)
            {
                return@withLock
            }
        }
    }

    private fun generateUiStateFolderHandle(context: Context) : File?
    {
        return DataSource.generateFolderHandle(
            context = context,
            parent = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            folderName = UI_STATE_FOLDER_NAME,
            withLogging = true
        )
    }

    private fun generateUiStateFileHandle(
        context: Context,
        uiStateFolderHandle: File) : File?
    {
        return DataSource.generateFileHandle(
            context = context,
            folderHandle = uiStateFolderHandle,
            fileName = UI_STATE_FILE_NAME,
            withLogging = true
        )
    }

    private fun uiStateToJson(context: Context, uiState: UiState) : String?
    {
        if(jsonEncoderDecoder == null)
        {
            jsonEncoderDecoder = Json{ encodeDefaults = true }
        }

        try
        {
            val json: String =  jsonEncoderDecoder!!.encodeToString(uiState)
            return json
        }
        catch(e: Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }

            if((e !is SerializationException) &&
               (e !is IllegalArgumentException))
            {
                throw e
            }

            return null
        }
    }

    private fun jsonToUiState(context: Context, json: String) : UiState?
    {
        if(jsonEncoderDecoder == null)
        {
            jsonEncoderDecoder = Json{ encodeDefaults = true }
        }

        try
        {
            val uiState: UiState = jsonEncoderDecoder!!.decodeFromString<UiState>(json)
            return uiState
        }
        catch(e: Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }

            if((e !is SerializationException) &&
               (e !is IllegalArgumentException))
            {
                throw e
            }

            return null
        }
    }
}