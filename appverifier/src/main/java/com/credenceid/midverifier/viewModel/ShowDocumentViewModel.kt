package com.credenceid.midverifier.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.credenceid.midverifier.repository.CIDRepository

class ShowDocumentViewModel : ViewModel() {

    fun sendDeviceActivity() : LiveData<String> {
        return CIDRepository().registerResponseOnServer()
    }
}