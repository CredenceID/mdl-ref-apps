package com.credenceid.midverifier.logger

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.credenceid.midverifier.repository.CIDRepository
import com.credenceid.midverifier.util.NetworkHelper.MIDDetailsRequest
import com.credenceid.midverifier.util.NetworkUtils.hasInternet
import com.credenceid.midverifier.util.SystemUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object DocumentLogger {

    private const val DELAY_MS = 500
    private var mDataBase: CachedDocumentDetailsDB? = null

    /* If there is internet connectivity will send log to CredenceConnect, otherwise saves log to
     * local database.
     *
     * @param log log object to send to server. Object MUST be one of the following types:
     *
     * If object is of an invalid type, method will do nothing.
     */
    fun document(context: Context, log: MIDDetailsRequest?) {
        CoroutineScope(Dispatchers.Default).launch {
            if (null == log) return@launch
            try {
                val bitmapStringData = log.imageBitmap?.let { encodeToBase64(it) }
                if (bitmapStringData != null) {
                    log.imageData = bitmapStringData
                }

                /* Obtain database Instance if none. */
                if (null == mDataBase)
                    mDataBase = CachedDocumentDetailsDB.getInstance(context)

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
                    sendDocumentDetails(context, log)
                }
            }catch (ex : Exception) {
                Log.e("TAG", "Exception while creating and sending log ${ex.message}")
            }
        }
    }

    fun sendCachedDocuments(context: Context?) {
        /* Obtain database Instance if none. */
        if (null == mDataBase)
            mDataBase = CachedDocumentDetailsDB.getInstance(context)
        /* If internet send over all logs from database. */
        if (hasInternet(context!!))
            mDataBase?.getLogs(object : CachedDocumentDetailsDB.OnGet {
                override fun onGet(logs: List<MIDDetails>) {
                    sendDocs(
                        context,
                        logs
                    )
                }
            })
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

    /* Sends over a given set of logs to CredenceConnect server in separate Thread. */
    private fun sendDocs(context: Context, logs: List<MIDDetails>) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                for (log in logs) {
                    Log.d(
                        "TAG",
                        "-------------------------START-----------------------------------"
                    )
                    Log.d("TAG", "sending log *** getting new log ${log.id}")
                    /* If log was successfully sent over, delete it from database. */
                    val request = Gson().fromJson(
                        log.json,
                        MIDDetailsRequest::class.java
                    )
                    val isSend = sendDocumentDetails(context, request)
                    Log.d("TAG", "sending log 333 returning received ${log.id}")
                    if (isSend) {
                        Log.d("TAG", "sending log 444 deleting log from db ${log.id}")
                        mDataBase?.delete(log.id)
                    }
                    Log.d("TAG", "xxxxxxxxxxxxxxxxxxxx--END---xxxxxxxxxxxxxxxxxxxxx")
                    /* Small delay before sending over next log. */
                    /*delay(
                    DELAY_MS.toLong()
                )*/
                }
            }
        }catch (ex : Exception) {
            Log.e("TAG", "Exception sending logs to server ${ex.message}")
        }
    }

    private val mutex = Mutex()
    private suspend fun sendDocumentDetails(context: Context, log: MIDDetailsRequest?): Boolean {
        if (null == log) return false
        Log.d("TAG", "sending log 111")
        mutex.withLock {
            try {
                if(log.imageData != null) {
                    decodeBase64(log.imageData)?.let { log.addImageBitmap(it) }
                    val file: File
                    if (log.imageBitmap != null) {
                        file = File(context.filesDir.toString() + "/file.png")
                        val bos = ByteArrayOutputStream()
                        log.imageBitmap?.compress(Bitmap.CompressFormat.PNG, 0, bos)
                        val bitmapData = bos.toByteArray()
                        //write your byteArray here
                        val fos = FileOutputStream(file)
                        fos.write(bitmapData)
                        fos.flush()
                        fos.close()
                        log.addImage(file)
                    }
                }
            }catch (e: IOException) {
                Log.e("TAG", "Exception while creating file ${e.message}")
            }
            try {
                val returnValue =  CIDRepository().submitDetailsToServer(log)
                Log.d("TAG", "sending log 222 returning value ")
                // Setting value for tap------
                SystemUtils.setSystemState(SystemUtils.STATE_WAITING_FOR_TAP)
                return returnValue
            } catch (e: java.lang.Exception) {
                Log.w("TAG", "Unable to send analytic(s) to server." + e.message)
            }
            return false
        }
    }

    private fun encodeToBase64(image: Bitmap?): String?{
        return if(image != null) {
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b = baos.toByteArray()
            val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
            imageEncoded
        } else {
            null
        }
    }

    private fun decodeBase64(input: String?): Bitmap? {
        return if(input != null) {
            val decodedByte = Base64.decode(input, 0)
            BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        } else {
            null
        }
    }
}