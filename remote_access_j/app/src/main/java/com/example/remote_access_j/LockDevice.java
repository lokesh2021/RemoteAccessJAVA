package com.example.remote_access_j;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

public class LockDevice extends AppCompatActivity {
    private static DevicePolicyManager devicePolicyManager;
    private static ActivityManager activityManager;
    private static ComponentName componentName;
    LockDevice() {
    }



    public static void lock(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        componentName = new ComponentName(context, MyAdmin.class);
        devicePolicyManager.lockNow();
    }
}
