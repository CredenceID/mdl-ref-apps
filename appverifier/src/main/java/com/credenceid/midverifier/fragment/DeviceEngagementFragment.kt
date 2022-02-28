package com.credenceid.midverifier.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.credenceid.midverifier.R
import com.credenceid.midverifier.databinding.FragmentDeviceEngagementBinding
import com.credenceid.midverifier.transfer.TransferManager
import com.credenceid.midverifier.util.SystemUtils
import com.credenceid.midverifier.util.TransferStatus


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DeviceEngagementFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "DeviceEngagementFragment"
    }

    private val args: DeviceEngagementFragmentArgs by navArgs()

    private val appPermissions:List<String> get() {
        var permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (android.os.Build.VERSION.SDK_INT >= 31) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        return permissions
    }

    private var _binding: FragmentDeviceEngagementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //private var mCodeScanner: CodeScanner? = null
    private lateinit var transferManager: TransferManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDeviceEngagementBinding.inflate(inflater, container, false)
        transferManager = TransferManager.getInstance(requireContext())
        transferManager.initVerificationHelper()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
        // QR Code Engagement
        //mCodeScanner = CodeScanner(requireContext(), binding.csScanner)
        /*mCodeScanner?.decodeCallback = DecodeCallback { result ->
            requireActivity().runOnUiThread {
                val qrText = result.text
                Log.d(LOG_TAG, "qrText: $qrText")
                transferManager.setQrDeviceEngagement(qrText)
            }
        }

        binding.csScanner.setOnClickListener { mCodeScanner?.startPreview() }*/

        binding.button.setOnClickListener {
           /* SystemUtils.setSystemState(SystemUtils.EMPTY_STATE)
            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(
                Runnable {
                    SystemUtils.turnOffLights()
                }
            )*/
        }

        binding.button2.setOnClickListener {
            /*SystemUtils.setSystemState(SystemUtils.STATE_WAITING_FOR_TAP)
            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(
                Runnable {
                    SystemUtils.execBlueCircle()
                }
            )*/
        }

        transferManager.getTransferStatus().observe(viewLifecycleOwner, {
            try {
                when (it) {
                    TransferStatus.ENGAGED -> {
                        Log.d(LOG_TAG, "Device engagement received")
                        onDeviceEngagementReceived()
                    }
                    TransferStatus.CONNECTED -> {
                        Log.d(LOG_TAG, "Device connected")
                        Toast.makeText(
                            requireContext(), "Error invalid callback connected",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_Transfer_to_RequestOptions)
                    }
                    TransferStatus.RESPONSE -> {
                        Log.d(LOG_TAG, "Device response received")
                        Toast.makeText(
                            requireContext(), "Error invalid callback response",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_Transfer_to_RequestOptions)
                    }
                    TransferStatus.DISCONNECTED -> {
                        Log.d(LOG_TAG, "Device disconnected")
                        Toast.makeText(
                            requireContext(), "Device disconnected",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_Transfer_to_RequestOptions)
                    }
                    TransferStatus.ERROR -> {
                        Log.d(LOG_TAG, "Error received")
                        Toast.makeText(
                            requireContext(), "Error connecting to holder",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_Transfer_to_RequestOptions)
                    }
                }
            }catch ( ex :Exception) {
                Log.e("TAG", "Exception while navigation ${ex.message}")
            }
        })

        binding.btCancel.setOnClickListener {
            requireActivity().finish()
            //findNavController().navigate(R.id.action_ScanDeviceEngagement_to_RequestOptions)
        }
    }

    override fun onResume() {
        super.onResume()
        enableReader()
    }

    override fun onPause() {
        super.onPause()
        disableReader()
    }

    private fun enableReader() {
        /*if (isAllPermissionsGranted()) {
            mCodeScanner?.startPreview()
        } else {
            shouldRequestPermission()
        }*/
        shouldRequestPermission()
        transferManager.setNdefDeviceEngagement(
            NfcAdapter.getDefaultAdapter(requireContext()),
            requireActivity()
        )
    }

    private fun disableReader() {
        //mCodeScanner?.releaseResources()
    }

    private fun shouldRequestPermission() {
        val permissionsNeeded = appPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            permissionsLauncher.launch(
                permissionsNeeded.toTypedArray()
            )
        }
    }

    private fun isAllPermissionsGranted(): Boolean {
        // If any permission is not granted return false
        return appPermissions.none { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(LOG_TAG, "permissionsLauncher ${it.key} = ${it.value}")

                // Open settings if user denied any required permission
                if (!it.value && !shouldShowRequestPermissionRationale(it.key)) {
                    openSettings()
                    return@registerForActivityResult
                }
            }
        }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", requireContext().packageName, null)
        startActivity(intent)
    }

    private fun onDeviceEngagementReceived() {
        val availableAddressesSize = transferManager.availableMdocAddresses?.size ?: 0
        if( availableAddressesSize >= 2) {
            val availableMdocAddressList = transferManager.availableMdocAddresses?.toList()
            val mDocAddress = availableMdocAddressList?.get(1)
            Log.d(LOG_TAG, "mdoc Address $mDocAddress")
            mDocAddress?.let {
                transferManager.setMdocAddress(it)
                findNavController().navigate(
                    DeviceEngagementFragmentDirections.actionScanDeviceEngagementToTransfer(
                        args.requestDocumentList
                    )
                )
            }
        } else {
            if (transferManager.availableMdocAddresses?.size == 1) {
                val mDocAddress = transferManager.availableMdocAddresses?.toList()?.get(0)
                mDocAddress?.let {
                    transferManager.setMdocAddress(it)
                    findNavController().navigate(
                        DeviceEngagementFragmentDirections.actionScanDeviceEngagementToTransfer(
                            args.requestDocumentList
                        )
                    )
                }
            }
        }

        // start Other LED -- change state
        SystemUtils.setSystemState(SystemUtils.STATE_WAITING_FOR_EXCHANGE)

        /*if (transferManager.availableMdocAddresses?.size == 1) {
            findNavController().navigate(
                DeviceEngagementFragmentDirections.actionScanDeviceEngagementToTransfer(
                    args.requestDocumentList
                )
            )
        } else {
            findNavController().navigate(
                DeviceEngagementFragmentDirections.actionScanDeviceEngagementToSelectTransport(
                    args.requestDocumentList
                )
            )
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}