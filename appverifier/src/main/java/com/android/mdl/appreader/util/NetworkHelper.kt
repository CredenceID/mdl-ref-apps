package com.android.mdl.appreader.util

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

object NetworkHelper {

    var retrofit: Retrofit? = null
    val RETROFIT_DATA_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"

    fun initRetrofit() {
        val gson = GsonBuilder().setLenient()
            .setDateFormat(RETROFIT_DATA_FORMAT)
            .create()
        retrofit = Retrofit.Builder()
            .baseUrl("https://devapi.credenceid.com/")
            .client(configureInterceptor().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun configureInterceptor(): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        httpClient.addInterceptor(interceptor)
        httpClient.connectTimeout(5, TimeUnit.MINUTES)
        httpClient.callTimeout(5, TimeUnit.MINUTES)
        httpClient.writeTimeout(5, TimeUnit.MINUTES)
        httpClient.readTimeout(5, TimeUnit.MINUTES)
        return httpClient
    }


    interface MIDService {
        @POST("/recentactivity/saveActivityLogs")
        fun sendMIDActivity(@Body loginRequest: MIDRequest):Call<ResponseBody>

        @POST("/midDevice/saveMIDDetails")
        fun sendMIDDetails(@Body loginRequest: MIDDetailsRequest):Call<ResponseBody>
    }


    data class MIDRequest (
        @SerializedName("createDttm" ) var createDttm : Int    = 0,
        @SerializedName("ethernetMacAddr" ) var ethernetMacAddr: String  =  "",
        @SerializedName("imei" ) var imei : String  =  "",
        @SerializedName("apiId") var apiId: String  =  "",
        @SerializedName("latitude") var latitude : String  =  "",
        @SerializedName("longitude"  ) var longitude: String  =  "",
        @SerializedName("androidID"  ) var androidID: String  =  "",
        @SerializedName("macAddress" ) var macAddress : String  =  "",
        @SerializedName("buildNumber") var buildNumber: String  =  "",
        @SerializedName("androidVersion") var androidVersion : String  =  "",
        @SerializedName("bitmapImage") var bitmapImage: String  =  "",
        @SerializedName("faceLandmarkPoints" ) var faceLandmarkPoints : String  =  "",
        @SerializedName("faceTemplate"  ) var faceTemplate : String  =  ""

    )

    data class MIDDetailsRequest (
        @SerializedName("firstName") var firstName : String    = "",
        @SerializedName("lastName") var lastName: String  =  "",
        @SerializedName("docId") var docId : String="",
        @SerializedName("email") var email: String=  "",
        @SerializedName("imei") var imei : String  =  "",
        @SerializedName("latitude") var latitude : String  =  "",
        @SerializedName("longitude"  ) var longitude: String  =  "",
        @SerializedName("midReaderStatus"  ) var midReaderStatus: String  =  "",
        @SerializedName("createdOn" ) var createdOn : String  =  "",
        @SerializedName("dob") var dob: String  =  "",
    )
}