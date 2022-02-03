package com.android.mdl.appreader.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.mdl.appreader.util.NetworkHelper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
}