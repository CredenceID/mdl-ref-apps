package com.credenceid.midverifier.document

import com.credenceid.midverifier.document.RequestDocument
import java.io.Serializable

class RequestDocumentList : Serializable {
    private val list = mutableListOf<RequestDocument>()

    fun addRequestDocument(requestDocument: RequestDocument) {
        list.add(requestDocument)
    }

    fun getAll(): List<RequestDocument> {
        return list
    }
}