package com.credenceid.midverifier.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.credenceid.midverifier.document.RequestDocumentList
import com.credenceid.midverifier.transfer.TransferManager
import com.credenceid.midverifier.util.TransferStatus

class TransferViewModel(val app: Application) : AndroidViewModel(app) {

    companion object {
        private const val LOG_TAG = "TransferViewModel"
    }

    private val transferManager = TransferManager.getInstance(app.applicationContext)

    fun getTransferStatus(): LiveData<TransferStatus> = transferManager.getTransferStatus()

    fun connect() {
        transferManager.connect()
    }

    fun sendRequest(requestDocumentList: RequestDocumentList) {
        transferManager.sendRequest(requestDocumentList)
    }

    fun sendNewRequest(requestDocumentList: RequestDocumentList) {
        transferManager.sendNewRequest(requestDocumentList)
    }
}