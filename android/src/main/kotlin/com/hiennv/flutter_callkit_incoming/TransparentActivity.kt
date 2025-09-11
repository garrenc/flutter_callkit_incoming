package com.hiennv.flutter_callkit_incoming

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager

class TransparentActivity : Activity() {

    companion object {
        private const val TAG = "TransparentActivity"

        fun getIntent(context: Context, action: String, data: Bundle?): Intent {
            Log.d(TAG, "getIntent: Creating TransparentActivity intent with action=$action")
            return Intent(context, TransparentActivity::class.java).apply {
                this.action = action
                putExtra("data", data)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
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

        // Window flags for lock screen display
        Log.d(TAG, "onCreate: Setting window flags for lock screen display")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }

        val data: Bundle? = intent?.getBundleExtra("data")
        val action: String = intent?.action ?: run {
            // Action is missing – nothing to do. Avoid NPE and exit gracefully.
            Log.e(TAG, "onCreate: Missing intent.action, finishing to avoid crash")
            finish()
            overridePendingTransition(0, 0)
            return
        }

        Log.d(TAG, "onCreate: Action=$action, data=${data != null}")

        // Send broadcast safely
        Log.d(TAG, "onCreate: Sending broadcast to CallkitIncomingBroadcastReceiver")
        try {
            val broadcastIntent = CallkitIncomingBroadcastReceiver.getIntent(this, action, data)
            broadcastIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            sendBroadcast(broadcastIntent)
        } catch (t: Throwable) {
            Log.e(TAG, "onCreate: Failed to send broadcast", t)
        }

        // Bring app to foreground if possible
        Log.d(TAG, "onCreate: Getting main app intent from AppUtils")
        val activityIntent = AppUtils.getAppIntent(this, action, data)
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
