package com.credenceid.midverifier

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.credenceid.midverifier.logger.DocumentLogger
import com.credenceid.midverifier.util.NetworkHelper
import com.credenceid.midverifier.util.SystemUtils

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOG_TAG = "MainActivity"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var mAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUtils.setSystemState(SystemUtils.STATE_WAITING_FOR_TAP)
        setContentView(R.layout.activity_main)
        NetworkHelper.initRetrofit()
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.RequestOptions,
                R.id.SelectTransport,
                R.id.ShowDocument,
                R.id.Transfer
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        mAdapter = NfcAdapter.getDefaultAdapter(this)
        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //-- to capture all pending documents and send them to server
        DocumentLogger.sendCachedDocuments(this)
    }

    override fun onResume() {
        super.onResume()
        mAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        mAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(LOG_TAG, "New intent on Activity $intent")
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return when(navController.currentDestination?.id) {
            R.id.ScanDeviceEngagement -> {
                // custom behavior here
                finish()
                true
            }
            else -> navController.navigateUp(appBarConfiguration)
                    || super.onSupportNavigateUp()
        }
    }

    override fun onStop() {
        super.onStop()
        SystemUtils.setSystemState(SystemUtils.EMPTY_STATE)
    }
}