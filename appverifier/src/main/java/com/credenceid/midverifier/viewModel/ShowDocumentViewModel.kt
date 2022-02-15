package com.credenceid.midverifier.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.credenceid.midverifier.logger.DocumentLogger
import com.credenceid.midverifier.repository.CIDRepository

class ShowDocumentViewModel : ViewModel() {

    fun sendDeviceActivity() : LiveData<String> {
        return CIDRepository().registerResponseOnServer()
    }

    fun sendMIDDetails(context: Context, portraitBytes : ByteArray?) {
        DocumentLogger.log(context,
            portraitBytes?.let { DocumentLogger.createDetailsRequest(it) })
    }
}