package com.android.mdl.appreader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.mdl.appreader.MainActivity

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG", "Reboot detected .....")
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}