package com.hiennv.flutter_callkit_incoming

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class TransparentActivity : Activity() {

    companion object {
        private const val TAG = "TransparentActivity"
        
        fun getIntent(context: Context, action: String, data: Bundle?): Intent {
            Log.d(TAG, "getIntent: Creating TransparentActivity intent with action=$action")
            val intent = Intent(context, TransparentActivity::class.java)
            intent.action = action
            intent.putExtra("data", data)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return intent
        }
    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Setting activity invisible")
        setVisible(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: TransparentActivity started")

        // Set window flags to show when locked, similar to CallkitIncomingActivity
        Log.d(TAG, "onCreate: Setting window flags for lock screen display")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            Log.d(TAG, "onCreate: Android O_MR1+: Setting setShowWhenLocked(true)")
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            Log.d(TAG, "onCreate: Pre-O_MR1: Setting FLAG_SHOW_WHEN_LOCKED and FLAG_DISMISS_KEYGUARD")
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }

        val data = intent.getBundleExtra("data")
        Log.d(TAG, "onCreate: Action=${intent.action}, data=${data != null}")

        Log.d(TAG, "onCreate: Sending broadcast to CallkitIncomingBroadcastReceiver")
        val broadcastIntent = CallkitIncomingBroadcastReceiver.getIntent(this, intent.action!!, data)
        broadcastIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        sendBroadcast(broadcastIntent)

        Log.d(TAG, "onCreate: Getting main app intent from AppUtils")
        val activityIntent = AppUtils.getAppIntent(this, intent.action, data)
        Log.d(TAG, "onCreate: Main app intent=${activityIntent != null}")
        
        if (activityIntent != null) {
            Log.d(TAG, "onCreate: Starting main app activity")
            startActivity(activityIntent)
        } else {
            Log.e(TAG, "onCreate: Failed to get main app intent")
        }

        Log.d(TAG, "onCreate: Finishing TransparentActivity")
        finish()
        overridePendingTransition(0, 0)
    }
}
