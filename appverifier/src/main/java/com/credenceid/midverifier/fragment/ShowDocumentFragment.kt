package com.credenceid.midverifier.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.security.identity.DeviceResponseParser
import com.credenceid.midverifier.R
import com.credenceid.midverifier.databinding.FragmentShowDocumentBinding
import com.credenceid.midverifier.issuerauth.SimpleIssuerTrustStore
import com.credenceid.midverifier.logger.DocumentLogger
import com.credenceid.midverifier.transfer.TransferManager
import com.credenceid.midverifier.util.FormatUtil
import com.credenceid.midverifier.util.KeysAndCertificates
import com.credenceid.midverifier.util.NetworkHelper
import com.credenceid.midverifier.util.TransferStatus
import com.credenceid.midverifier.viewModel.ShowDocumentViewModel
import org.jetbrains.anko.attr


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ShowDocumentFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "ShowDocumentFragment"
        private const val MDL_DOCTYPE = "org.iso.18013.5.1.mDL"
        private const val MICOV_DOCTYPE = "org.micov.1"
        private const val MDL_NAMESPACE = "org.iso.18013.5.1"
        private const val MICOV_ATT_NAMESPACE = "org.micov.attestation.1"
    }

    private var _binding: FragmentShowDocumentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var portraitBytes: ByteArray? = null
    private lateinit var transferManager: TransferManager
    private lateinit var viewModel : ShowDocumentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentShowDocumentBinding.inflate(inflater, container, false)
        transferManager = TransferManager.getInstance(requireContext())
        viewModel = ViewModelProvider(this).get(ShowDocumentViewModel::class.java)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val documents = transferManager.getDeviceResponse().documents
        binding.tvResults.text =
            Html.fromHtml(formatTextResult(documents), Html.FROM_HTML_MODE_COMPACT)
        portraitBytes
        ?.let { pb ->
            Log.d(LOG_TAG, "Showing portrait " + pb.size + " bytes")
            binding.ivPortrait.setImageBitmap(
                BitmapFactory.decodeByteArray(portraitBytes, 0, pb.size)
            )
            binding.ivPortrait.visibility = View.VISIBLE
        }


        binding.btOk.setOnClickListener {
            findNavController().navigate(R.id.action_ShowDocument_to_RequestOptions)
        }
        binding.btCloseConnection.setOnClickListener {
            transferManager.stopVerification(
                sendSessionTerminationMessage = false,
                useTransportSpecificSessionTermination = false
            )
            hideButtons()
        }
        binding.btCloseTransportSpecific.setOnClickListener {
            //calling close transport specific after document trf completes
            transferManager.stopVerification(
                sendSessionTerminationMessage = true,
                useTransportSpecificSessionTermination = true
            )
            hideButtons()
        }
        binding.btCloseTerminationMessage.setOnClickListener {
            transferManager.stopVerification(
                sendSessionTerminationMessage = true,
                useTransportSpecificSessionTermination = false
            )
            hideButtons()
        }
        binding.btNewRequest.setOnClickListener {
            findNavController().navigate(
                ShowDocumentFragmentDirections.actionShowDocumentToRequestOptions(true)
            )
        }
        transferManager.getTransferStatus().observe(viewLifecycleOwner) {
            when (it) {
                TransferStatus.ENGAGED -> {
                    Log.d(LOG_TAG, "Device engagement received.")
                }
                TransferStatus.CONNECTED -> {
                    Log.d(LOG_TAG, "Device connected received.")
                }
                TransferStatus.RESPONSE -> {
                    Log.d(LOG_TAG, "Device response received.")
                    registerResponseOnServer()
                }
                TransferStatus.DISCONNECTED -> {
                    Log.d(LOG_TAG, "Device disconnected received.")
                    transferManager.stopVerification(
                        sendSessionTerminationMessage = false,
                        useTransportSpecificSessionTermination = false
                    )
                    hideButtons()
                }
                TransferStatus.ERROR -> {
                    Toast.makeText(
                        requireContext(), "Error with the connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                    transferManager.stopVerification(
                        sendSessionTerminationMessage = false,
                        useTransportSpecificSessionTermination = false
                    )
                    hideButtons()
                }
            }
        }
    }

    private fun hideButtons() {
        binding.btOk.visibility = View.VISIBLE
        binding.btCloseConnection.visibility = View.GONE
        binding.btCloseTransportSpecific.visibility = View.GONE
        binding.btCloseTerminationMessage.visibility = View.GONE
        binding.btNewRequest.visibility = View.GONE
    }
    val detailsMap = mutableMapOf<String,String>()
    private fun formatTextResult(documents: Collection<DeviceResponseParser.Document>): String {
        // Create the trustManager to validate the DS Certificate against the list of known
        // certificates in the app
        val simpleIssuerTrustStore =
            SimpleIssuerTrustStore(KeysAndCertificates.getTrustedIssuerCertificates(requireContext()))

        val sb = StringBuffer()
        sb.append("Number of documents returned: <b>${documents.size}</b><br>")
        sb.append("Address: <b>" + transferManager.mdocAddress + "</b><br>")
        sb.append("<br>")
        for (doc in documents) {
            // Get primary color from theme to use in the HTML formatted document.
            val color = String.format(
                "#%06X",
                0xFFFFFF and requireContext().theme.attr(R.attr.colorPrimary).data
            )
            sb.append("<h3>Doctype: <font color=\"$color\">${doc.docType}</font></h3>")
            val certPath =
                simpleIssuerTrustStore.createCertificationTrustPath(doc.issuerCertificateChain.toList())
            val isDSTrusted = simpleIssuerTrustStore.validateCertificationTrustPath(certPath)
            var commonName = ""
            // Use the issuer certificate chain if we could not build the certificate trust path
            val certChain = if (certPath?.isNotEmpty() == true) {
                certPath
            } else {
                doc.issuerCertificateChain.toList()
            }

            certChain.last().issuerX500Principal.name.split(",").forEach { line ->
                val (key, value) = line.split("=", limit = 2)
                if (key == "CN") {
                    commonName = "($value)"
                }
            }
            sb.append("${getFormattedCheck(isDSTrusted)}Issuer’s DS Key Recognized: $commonName<br>")
            sb.append("${getFormattedCheck(doc.issuerSignedAuthenticated)}Issuer Signed Authenticated<br>")
            sb.append("${getFormattedCheck(doc.deviceSignedAuthenticated)}Device Signed Authenticated<br>")
            for (ns in doc.issuerNamespaces) {
                sb.append("<br>")
                sb.append("<h5>Namespace: $ns</h5>")
                sb.append("<p>")
                for (elem in doc.getIssuerEntryNames(ns)) {
                    val value: ByteArray = doc.getIssuerEntryData(ns, elem)
                    var valueStr: String
                    if (doc.docType == MDL_DOCTYPE && ns == MDL_NAMESPACE && elem == "portrait") {
                        valueStr = String.format("(%d bytes, shown above)", value.size)
                        portraitBytes = doc.getIssuerEntryByteString(ns, elem)
                    } else if (doc.docType == MICOV_DOCTYPE && ns == MICOV_ATT_NAMESPACE && elem == "fac") {
                        valueStr = String.format("(%d bytes, shown above)", value.size)
                        portraitBytes = doc.getIssuerEntryByteString(ns, elem)
                    } else if (doc.docType == MDL_DOCTYPE
                        && ns == MDL_NAMESPACE && elem == "extra"
                    ) {
                        valueStr = String.format("%d bytes extra data", value.size)
                    } else {
                        valueStr = FormatUtil.cborPrettyPrint(value)
                    }
                    sb.append(
                        "${
                            getFormattedCheck(doc.getIssuerEntryDigestMatch(ns, elem))
                        }<b>$elem</b> -> $valueStr<br>"
                    )
                    fetchDataFrom(elem, valueStr)
                }
                sb.append("</p><br>")
            }
        }
        return sb.toString()
    }

    private fun fetchDataFrom(elem : String, valueStr : String) {
        var elementValue = valueStr
        if(elementValue.contains("\'")){
            elementValue = elementValue.replace("\'", "")
        }
        if(elementValue.contains("\"")) {
            elementValue = elementValue.replace("\"", "")
        }
        if(elementValue.contains("tag 1004 ")) {
            elementValue = elementValue.replace("tag 1004 ", "")
        }
        //--------------
        if(elem.equals("given_name",true) || elem.equals("family_name",true)
            || elem.equals("birth_date",true) || elem.equals("document_number", true)) {
            detailsMap[elem] = elementValue
        }
    }


    fun createDetailsRequest(portraitBytes : ByteArray): NetworkHelper.MIDDetailsRequest {
        val mIDRequest = NetworkHelper.MIDDetailsRequest()
        mIDRequest.createdOn = FormatUtil.getCreatedOn()
        mIDRequest.imei = "356905071680409"
        mIDRequest.firstName = detailsMap["given_name"] ?: ""
        mIDRequest.lastName = detailsMap["family_name"] ?: ""
        mIDRequest.midReaderStatus = "PASS"
        mIDRequest.docId = detailsMap["document_number"] ?:""
        mIDRequest.dob = detailsMap["birth_date"] ?: ""
        mIDRequest.latitude = "18.4880822"
        mIDRequest.longitude = "73.9518927"
        mIDRequest.imageBitmap = BitmapFactory.decodeByteArray(portraitBytes, 0, portraitBytes.size)
        return mIDRequest
    }

    private fun getFormattedCheck(authenticated: Boolean) = if (authenticated) {
        "<font color=green>&#x2714;</font> "
    } else {
        "<font color=red>&#x274C;</font> "
    }

    private var callback = object : OnBackPressedCallback(true /* enabled by default */) {
        override fun handleOnBackPressed() {
            TransferManager.getInstance(requireContext()).stopVerification(
                sendSessionTerminationMessage = true,
                useTransportSpecificSessionTermination = true
            )
            findNavController().navigate(R.id.action_ShowDocument_to_RequestOptions)
        }
    }

    private fun registerResponseOnServer() {
        /*viewModel.sendDeviceActivity().observe(viewLifecycleOwner, Observer {
            if(it.equals("SUCCESS", true)) {
                Log.d(LOG_TAG, "Response received from the Server")
            } else {
                Log.e(LOG_TAG,"Error while receiving response from server : $it")
            }
        })*/

        portraitBytes?.let { createDetailsRequest(portraitBytes = it) }?.let {
            viewModel.sendMIDDetails(requireContext(),
                it
            )
        }

        //-- closing transferP@
        transferManager.stopVerification(
            sendSessionTerminationMessage = true,
            useTransportSpecificSessionTermination = true
        )
        hideButtons()
        //calling OK click
        findNavController().navigate(R.id.action_ShowDocument_to_RequestOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}