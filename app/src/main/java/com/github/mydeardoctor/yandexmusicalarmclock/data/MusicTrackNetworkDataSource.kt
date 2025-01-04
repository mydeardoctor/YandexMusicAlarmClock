package com.github.mydeardoctor.yandexmusicalarmclock.data

import android.content.Context
import android.util.Log
import android.util.Xml
import com.github.mydeardoctor.yandexmusicalarmclock.logger.Logger
import com.github.mydeardoctor.yandexmusicalarmclock.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.random.Random


object MusicTrackNetworkDataSource
{
    private val client: OkHttpClient = OkHttpClient()
    private const val BASE_URL: String = "https://api.music.yandex.net"
    public var token: String? = null
    private var jsonEncoderDecoder: Json? = null
    private var xmlParser: XmlPullParser? = null

    public suspend fun downloadMusicTrack(context: Context): ResponseBody?
    {
        //Check internet connection.
        val isInternetConnected: Boolean =
            PermissionManager.getIsInternetConnected(context = context)
        if(isInternetConnected == false)
        {
            return null
        }

        //Check token.
        if(token == null)
        {
            return null
        }

        //Communicate with Yandex Music server.
        val uid: String? = getAccountUid(context = context, token = token!!)
        if(uid == null)
        {
            return null
        }

        val randomTrackId: String? = getRandomTrackId(
            context = context,
            uid = uid,
            token = token!!)
        if(randomTrackId == null)
        {
            return null
        }

        val downloadInfoUrl: String? = getDownloadInfoUrl(
            context = context,
            randomTrackId = randomTrackId,
            token = token!!)
        if(downloadInfoUrl == null)
        {
            return null
        }

        val numberOfDownloadInfoXmlElements: Int = 4
        val downloadInfoXmlElements: Array<String?> = getDownloadInfoXmlElements(
            context = context,
            downloadInfoUrl = downloadInfoUrl,
            token = token!!,
            numberOfDownloadInfoXmlElements = numberOfDownloadInfoXmlElements)
        if(
            (downloadInfoXmlElements.size != numberOfDownloadInfoXmlElements) ||
            (downloadInfoXmlElements[0] == null) ||
            (downloadInfoXmlElements[1] == null) ||
            (downloadInfoXmlElements[2] == null) ||
            (downloadInfoXmlElements[3] == null))
        {
            return null
        }
        val host: String = downloadInfoXmlElements[0]!!
        val path: String = downloadInfoXmlElements[1]!!
        val ts: String = downloadInfoXmlElements[2]!!
        val s: String = downloadInfoXmlElements[3]!!

        val sign: String? = generateSign(context = context, path = path, s = s)
        if(sign == null)
        {
            return null
        }

        val responseBody: ResponseBody? = getMp3(
            context = context,
            host = host,
            path = path,
            ts = ts,
            sign = sign,
            token = token!!)
        return responseBody
    }

    private suspend fun getAccountUid(context: Context, token: String) : String?
    {
        val responseBody: ResponseBody? = httpGet(
            context = context,
            url = "$BASE_URL/account/status",
            token = token)
        if(responseBody == null)
        {
            return null
        }

        val jsonString: String = responseBody.string()

        val uid: String? = parseAccountStatus(
            context = context,
            json = jsonString)
        return uid
    }

    private fun parseAccountStatus(context: Context, json: String) : String?
    {
        if(jsonEncoderDecoder == null)
        {
            jsonEncoderDecoder = Json{ encodeDefaults = true }
        }

        try
        {
            val root: JsonObject = jsonEncoderDecoder!!.parseToJsonElement(json).jsonObject
            val result: JsonObject? = root["result"]?.jsonObject
            val account: JsonObject? = result?.get("account")?.jsonObject
            var uid: String? = account?.get("uid")?.jsonPrimitive?.content.toString()
            if(uid == "")
            {
                uid = null
            }
            return uid
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

    private suspend fun getRandomTrackId(
        context: Context,
        uid: String,
        token: String): String?
    {
        val responseBody: ResponseBody? = httpGet(
            context = context,
            url = "$BASE_URL/users/${uid}/likes/tracks",
            token = token)
        if(responseBody == null)
        {
            return null
        }

        val jsonString: String = responseBody.string()

        val randomTrackId: String? = parseLikedTracks(
            context = context,
            json = jsonString)
        return randomTrackId
    }

    private fun parseLikedTracks(context: Context, json: String) : String?
    {
        if(jsonEncoderDecoder == null)
        {
            jsonEncoderDecoder = Json{ encodeDefaults = true }
        }

        try
        {
            val root: JsonObject = jsonEncoderDecoder!!.parseToJsonElement(json).jsonObject
            val result: JsonObject? = root["result"]?.jsonObject
            val library: JsonObject? = result?.get("library")?.jsonObject
            val tracks: JsonArray? = library?.get("tracks")?.jsonArray
            val randomTrack: JsonObject? = tracks?.get(Random.nextInt(tracks.size))?.jsonObject
            var randomTrackId: String? = randomTrack?.get("id")?.jsonPrimitive?.content.toString()
            if(randomTrackId == "")
            {
                randomTrackId = null
            }
            return randomTrackId
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

    private suspend fun getDownloadInfoUrl(
        context: Context,
        randomTrackId: String,
        token: String): String?
    {
        val responseBody: ResponseBody? = httpGet(
            context = context,
            url = "$BASE_URL/tracks/${randomTrackId}/download-info",
            token = token)
        if(responseBody == null)
        {
            return null
        }

        val jsonString: String = responseBody.string()

        val downloadInfoUrl: String? = parseDownloadInfo(
            context = context,
            json = jsonString)
        return downloadInfoUrl
    }

    private fun parseDownloadInfo(context: Context, json: String) : String?
    {
        if(jsonEncoderDecoder == null)
        {
            jsonEncoderDecoder = Json{ encodeDefaults = true }
        }

        try
        {
            val root: JsonObject = jsonEncoderDecoder!!.parseToJsonElement(json).jsonObject
            val result: JsonArray? = root["result"]?.jsonArray

            var downloadInfoUrlCurrent: String? = null
            var bitrateInKbpsMax: Int = 0

            if(result != null)
            {
                for(element: JsonElement in result)
                {
                    val elementAsJsonObject: JsonObject = element.jsonObject
                    val codec: String = elementAsJsonObject["codec"]?.jsonPrimitive?.content.toString()
                    val downloadInfoUrl: String = elementAsJsonObject["downloadInfoUrl"]?.jsonPrimitive?.content.toString()
                    val bitrateInKbps: Int? = elementAsJsonObject["bitrateInKbps"]?.jsonPrimitive?.int

                    if((codec == "mp3") &&
                       (bitrateInKbps != null) &&
                       (bitrateInKbps > bitrateInKbpsMax))
                    {
                        downloadInfoUrlCurrent = downloadInfoUrl
                        bitrateInKbpsMax = bitrateInKbps
                    }
                }
            }

            if(downloadInfoUrlCurrent == "")
            {
                downloadInfoUrlCurrent = null
            }
            return downloadInfoUrlCurrent
        }
        catch (e: Exception)
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

    private suspend fun getDownloadInfoXmlElements(
        context: Context,
        downloadInfoUrl: String,
        token: String,
        numberOfDownloadInfoXmlElements: Int): Array<String?>
    {
        val responseBody: ResponseBody? = httpGet(
            context = context,
            url = downloadInfoUrl,
            token = token)
        if(responseBody == null)
        {
            val emptyArray: Array<String?> = arrayOfNulls(
                size = numberOfDownloadInfoXmlElements)
            return emptyArray
        }

        val xmlString: String = responseBody.string()

        val downloadInfoXmlElements: Array<String?> = parseDownloadInfoXml(
            context = context,
            xml = xmlString,
            numberOfDownloadInfoXmlElements = numberOfDownloadInfoXmlElements)
        return downloadInfoXmlElements
    }

    private fun parseDownloadInfoXml(
        context: Context,
        xml: String,
        numberOfDownloadInfoXmlElements: Int): Array<String?>
    {
        if(xmlParser == null)
        {
            xmlParser = Xml.newPullParser()
        }

        var host: String? = null
        var path: String? = null
        var ts: String? = null
        var s: String? = null

        try
        {
            val inputStream: ByteArrayInputStream = xml.byteInputStream()
            xmlParser!!.setInput(inputStream, null)

            var eventType = xmlParser!!.next()
            while(eventType != XmlPullParser.END_DOCUMENT)
            {
                when(eventType)
                {
                    XmlPullParser.START_TAG ->
                    {
                        when (xmlParser!!.name)
                        {
                            "host" ->
                            {
                                host = xmlParser!!.nextText()
                            }
                            "path" ->
                            {
                                path = xmlParser!!.nextText()
                            }
                            "ts" ->
                            {
                                ts = xmlParser!!.nextText()
                            }
                            "s" ->
                            {
                                s = xmlParser!!.nextText()
                            }
                        }
                    }
                }
                eventType = xmlParser!!.next()
            }

            if(host == "")
            {
                host = null
            }
            if(path == "")
            {
                path = null
            }
            if(ts == "")
            {
                ts = null
            }
            if(s == "")
            {
                s = null
            }
            val downloadInfoXmlElements: Array<String?> = arrayOf(host, path, ts, s)
            return downloadInfoXmlElements
        }
        catch (e: Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }

            if((e !is XmlPullParserException) &&
               (e !is IOException))
            {
                throw e
            }

            val emptyArray: Array<String?> = arrayOfNulls(size = numberOfDownloadInfoXmlElements)
            return emptyArray
        }
    }

    private suspend fun getMp3(
        context: Context,
        host: String,
        path: String,
        ts: String,
        sign: String,
        token: String): ResponseBody?
    {
        val responseBody: ResponseBody? = httpGet(
            context = context,
            url = "https://${host}/get-mp3/${sign}/${ts}${path}",
            token = token)
        return responseBody
    }

    private suspend fun httpGet(
        context: Context,
        url: String,
        token: String?): ResponseBody?
    {
        val host: String? = generateHost(url)
        if(host == null)
        {
            return null
        }

        if(token == null)
        {
            return null
        }

        //Build request.
        var request: Request? = null
        try
        {
            request = Request
                .Builder()
                .url(url)
                .addHeader("host", host)
                .addHeader("Authorization", "OAuth ${token}")
                .build()
        }
        catch(e: IllegalArgumentException)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }

            return null
        }

        //Get response.
        try
        {
            val response: Response = client.newCall(request).execute()
            if(response.isSuccessful)
            {
                return response.body
            }
            else
            {
                return null
            }
        }
        catch(e: Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }

            if((e !is IOException) && (e !is IllegalStateException))
            {
                throw e
            }

            return null
        }
    }

    private fun generateHost(url: String) : String?
    {
        val firstStartIndex: Int = 0

        val indexOfFirstSlash: Int = url.indexOf(
            string = "/",
            startIndex = firstStartIndex,
            ignoreCase = true)
        if(indexOfFirstSlash == -1)
        {
            return null
        }

        val secondStartIndex: Int = indexOfFirstSlash + 1
        if(secondStartIndex > (url.length - 1))
        {
            return null
        }

        val indexOfSecondSlash: Int = url.indexOf(
            string = "/",
            startIndex = secondStartIndex,
            ignoreCase = true)
        if(indexOfSecondSlash == -1)
        {
            return null
        }

        val thirdStartIndex: Int = indexOfSecondSlash + 1
        if(thirdStartIndex > (url.length - 1))
        {
            return null
        }

        val indexOfThirdSlash: Int = url.indexOf(
            string = "/",
            startIndex = thirdStartIndex,
            ignoreCase = true)

        var lastIndex: Int = url.length
        if(indexOfThirdSlash != -1)
        {
            lastIndex = indexOfThirdSlash
        }

        val host: String = url.substring(thirdStartIndex, lastIndex)
        return host
    }

    private fun generateSign(context: Context, path: String, s: String): String?
    {
        val SIGN_SALT: String = "XGRlBW9FXlekgbPrRHuSiA"
        val input = SIGN_SALT + path.substring(1) + s

        try
        {
            val md = MessageDigest.getInstance("MD5")
            val hashBytes = md.digest(input.toByteArray(Charsets.UTF_8))
            val sign: String = hashBytes.joinToString("") { "%02x".format(it) }
            return sign
        }
        catch(e : Exception)
        {
            Log.e(Logger.LOGCAT_TAG, e.stackTraceToString())
            CoroutineScope(Dispatchers.IO).launch{
                Logger.updateLogFile(
                    context = context,
                    errorMessage = e.stackTraceToString())
            }

            if((e !is NoSuchAlgorithmException) &&
               (e !is NullPointerException))
            {
                throw e
            }

            return null
        }
    }
}