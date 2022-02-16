package com.credenceid.midverifier.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.credenceid.midverifier.util.NetworkHelper
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.suspendCoroutine

class CIDRepository {

    fun registerResponseOnServer() : LiveData<String> {
        val data = MutableLiveData<String>()
        val LOG_TAG = "CIDRepository"
        Log.d(LOG_TAG, "Calling API....")
        val mIDService = NetworkHelper.retrofit?.create(NetworkHelper.MIDService::class.java)
        val responseCall = mIDService?.sendMIDActivity(createRequest())
        responseCall?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.body() != null) {
                    data.postValue("SUCCESS")
                } else {
                    data.postValue("FAIL ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                data.postValue("FAIL ${t?.cause}")
            }
        })
        return data
    }

    private fun createRequest() : NetworkHelper.MIDRequest {
        val mIDRequest = NetworkHelper.MIDRequest()
        mIDRequest.createDttm = System.currentTimeMillis().toInt()
        mIDRequest.imei = "356905071680409"
        mIDRequest.apiId = "6666"
        mIDRequest.latitude = "18.4880822"
        mIDRequest.longitude = "73.9518927"
        mIDRequest.androidID = "ce08171833b84f3705"
        return mIDRequest
    }

    suspend fun submitDetailsToServer(log : NetworkHelper.MIDDetailsRequest) = suspendCoroutine<Boolean> {
        val data = MutableLiveData<String>()
        val LOG_TAG = "CIDRepository"
        Log.d(LOG_TAG, "Calling API....")
        val mIDService = NetworkHelper.retrofit?.create(NetworkHelper.MIDService::class.java)
        val responseCall = mIDService?.sendMIDDetails(createMultiPartRequest(log))
        responseCall?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.body() != null) {
                    it.resumeWith(result = Result.success(true))
                } else {
                    it.resumeWith(result = Result.success(false))
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                it.resumeWith(result = Result.success(false))
            }
        })
    }

    private fun createMultiPartRequest(log : NetworkHelper.MIDDetailsRequest) : MultipartBody {

        val multipartBody = log.image?.asRequestBody()?.let {
            MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("firstName", log.firstName)
                .addFormDataPart("lastName", log.lastName)
                .addFormDataPart("createdOn", log.createdOn)
                .addFormDataPart("dob", log.dob)
                .addFormDataPart("email", "sample@sample.com")
                .addFormDataPart("docId", log.docId)
                .addFormDataPart("lattitude", "18.4880822")
                .addFormDataPart("longitude", "73.9518927")
                .addFormDataPart("midReaderStatus", log.midReaderStatus)
                .addFormDataPart("imei", log.imei)
                .addFormDataPart("image", log.image?.toString(), it)
                .build()
        }
        return multipartBody!!
    }
}