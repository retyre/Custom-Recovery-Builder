package com.example.tronautils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "TronaDebug";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "Broadcast received with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {
            
            Log.i(TAG, "Boot completed intent caught! Attempting to trigger script...");

            try {
                // Execute the shell wrapper which handles the delay and exploit
                Process process = Runtime.getRuntime().exec(new String[]{
                    "sh", "-c", "/data/local/tmp/delayed_trona.sh"
                });
                
                Log.i(TAG, "Shell execution command sent successfully.");
            } catch (IOException e) {
                Log.e(TAG, "IOException encountered while trying to execute script", e);
            }
        }
    }
}