package com.android.mdl.appreader.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.android.mdl.appreader.repository.CIDRepository

class ShowDocumentViewModel : ViewModel() {

    fun sendDeviceActivity() : LiveData<String> {
        return CIDRepository().registerResponseOnServer()
    }
}