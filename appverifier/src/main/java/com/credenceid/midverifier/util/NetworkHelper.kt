package com.credenceid.midverifier.util

import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.File
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
        fun sendMIDDetails(@Body body: MultipartBody):Call<ResponseBody>
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
        var firstName : String    = "",
        var lastName: String  =  "",
        var docId : String="",
        var email: String=  "",
        var imei : String  =  "",
        var latitude : String  =  "",
        var longitude: String  =  "",
        var midReaderStatus: String  =  "",
        var createdOn : String  =  "",
        var dob: String  =  "",
        var image: File? = null,
        var imageData : String = "",
        var imageBitmap : Bitmap? = null
    ) {
        fun addImage(file: File) {
            image = file
        }

        fun addByteList(byteList: String) {
            imageData = byteList
        }

        fun addImageBitmap (bitmap : Bitmap) {
            imageBitmap = bitmap
        }
    }
}