package com.credenceid.midverifier.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.credenceid.midverifier.MainActivity

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG", "Reboot detected .....")
        var paramIntent = intent
        paramIntent = Intent(context, MainActivity::class.java)
        paramIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(paramIntent);
    }
}