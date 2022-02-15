package com.android.mdl.appreader.logger

import android.content.Context
import android.util.Log
import com.android.mdl.appreader.repository.CIDRepository
import com.android.mdl.appreader.util.NetworkHelper.MIDDetailsRequest
import com.android.mdl.appreader.util.NetworkUtils.hasInternet
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset

object DocumentLogger {

    private const val DELAY_MS = 500
    private var mDataBase: CachedDocumentDetailsDataBase? = null

    /* If there is internet connectivity will send log to CredenceConnect, otherwise saves log to
     * local database.
     *
     * @param log log object to send to server. Object MUST be one of the following types:
     *
     * If object is of an invalid type, method will do nothing.
     */
    fun log(context: Context, log: MIDDetailsRequest?, portraitBytes: ByteArray?) {
        CoroutineScope(Dispatchers.Default).launch {
            if (null == log) return@launch
            try {
                //TODO: write a file
//                JSONObject jsonParam = new JSONObject();
//                jsonParam.put("lastReportedActivity", GenericUtils.getDeviceEpochTime());
//                FileUtils.writeLastActivityTimeToFile(App.Context, jsonParam.toString());
            } catch (e: Exception) {
                Log.d(
                    "App.TAG",
                    "Failed to write last reported activity to file :" + e.message
                )
            }
            /*var file: File? = null
            if (portraitBytes != null) {
                try {
                    file = File(context.filesDir.toString() + "/file.png")
                    val fos = FileOutputStream(file)
                    //write your byteArray here
                    fos.write(portraitBytes)
                    fos.flush()
                    fos.close()
                    log.addImage(file)
                    log.addByteList(portraitBytes.toString(Charset.defaultCharset()))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }*/

            /* Obtain database Instance if none. */
            if (null == mDataBase)
                mDataBase = CachedDocumentDetailsDataBase.getInstance(context)

            /* Convert log to type database understands. This means extracting all fields and converting
         * them to a JSON string. If object was of an invalid type, null be returned.
         */
            val midDetails = toLogObject(log) ?: return@launch

            /* Save to database if no internet. */
            if (!hasInternet(context)) {
            Log.d("C-service", "network not available")
            mDataBase?.insert(midDetails)
        } else {
            Log.d("C-service", "network available")
            /* Send log immediately if there is internet. */
            sendLog(context, log)

//            new Thread(() -> {
//                if(DEBUG)Log.d("C-service","inside new thread");
//                /* If log was successfully sent over, delete it from database. */
//                AnalyticsLogger.sendLog(logObject);
//                /*if (AnalyticsLogger.sendLog(logObject))
//                    mDataBase.delete(logObject.getID());*/
//            }).start();
        }
        }
    }

    fun sendCachedLogs(context: Context?) {
        /* Obtain database Instance if none. */
        if (null == mDataBase)
            mDataBase = CachedDocumentDetailsDataBase.getInstance(context)
        /* If internet send over all logs from database. */
        if (hasInternet(context!!))
            mDataBase?.getLogs { logs: List<MIDDetails> ->
            sendLogs(context,
                logs
            )
        }
    }

    private fun toLogObject(detailsRequest: MIDDetailsRequest): MIDDetails {
        val midDetails = MIDDetails()
        try {
            val jsonParam = Gson().toJson(detailsRequest)
            Log.d("TAG", jsonParam)
            midDetails.json = jsonParam
        } catch (ignore: java.lang.Exception) {
        }
        return midDetails
    }

    fun createDetailsRequest(): MIDDetailsRequest? {
        val mIDRequest = MIDDetailsRequest()
        mIDRequest.createdOn = System.currentTimeMillis().toString()
        mIDRequest.imei = "356905071680409"
        mIDRequest.firstName = "first name"
        mIDRequest.lastName = "last name"
        mIDRequest.midReaderStatus = "verified"
        mIDRequest.docId = "1231222"
        mIDRequest.dob = "2012-12-12"
        mIDRequest.latitude = "18.4880822"
        mIDRequest.longitude = "73.9518927"
        return mIDRequest
    }

    /* Sends over a given set of logs to CredenceConnect server in separate Thread. */
    private fun sendLogs(context: Context, logs: List<MIDDetails>) {
        CoroutineScope(Dispatchers.IO).launch {
                for (log in logs) {
                    /* If log was successfully sent over, delete it from database. */
                    val request = Gson().fromJson(
                        log.json,
                        MIDDetailsRequest::class.java
                    )
                    if (sendLog(context,request))
                        mDataBase?.delete(log.id)
                    /* Small delay before sending over next log. */
                    delay(
                        DELAY_MS.toLong()
                    )
                }
        }
    }
    val mutex = Mutex()
    private suspend fun sendLog(context: Context, log: MIDDetailsRequest?): Boolean {
        if (null == log) return false
        mutex.withLock {
            var file : File
            val bytes = log.imageData?.toByteArray(Charset.defaultCharset())
            if (bytes != null) {
                try {
                    file = File(context.filesDir.toString() + "/file.png")
                    val fos = FileOutputStream(file)
                    //write your byteArray here
                    fos.write(bytes)
                    fos.flush()
                    fos.close()
                    log.addImage(file)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                return CIDRepository().submitDetailsToServer(log)
            } catch (e: java.lang.Exception) {
                Log.w("TAG", "Unable to send analytic(s) to server." + e.message)
            }
            return false
        }
    }
}