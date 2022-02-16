package com.credenceid.midverifier.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.credenceid.midverifier.logger.DocumentLogger
import com.credenceid.midverifier.repository.CIDRepository
import com.credenceid.midverifier.util.NetworkHelper

class ShowDocumentViewModel : ViewModel() {

    fun sendDeviceActivity() : LiveData<String> {
        return CIDRepository().registerResponseOnServer()
    }

    fun sendMIDDetails(context: Context, midDetailsRequest : NetworkHelper.MIDDetailsRequest) {
        DocumentLogger.document(context,midDetailsRequest)
    }
}