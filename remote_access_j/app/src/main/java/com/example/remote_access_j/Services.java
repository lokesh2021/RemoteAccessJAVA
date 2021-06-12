package com.example.remote_access_j;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;

import androidx.appcompat.app.AppCompatActivity;

public class Services extends AppCompatActivity {
    private static DevicePolicyManager devicePolicyManager;
    private static ActivityManager activityManager;
    private static ComponentName componentName;

    Services() {
    }

    public static void lock(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        componentName = new ComponentName(context, MyAdmin.class);
        devicePolicyManager.lockNow();
    }

    public static void setRinger(Context context){
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio.setRingerMode(2);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.MODE_RINGTONE);
        float percent = 0.7f;
        int seventyVolume = (int) (maxVolume*percent);
        audio.setStreamVolume(AudioManager.MODE_RINGTONE, seventyVolume, 0);
    }

    public static void makeSound(Context context, String str){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        if(str=="start"){
            r.play();
            Globals.SoundButton = 1;
        }else{
            r.stop();
            Globals.SoundButton = 0;
        }

    }

    public static int batteryStatus(Context context){
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            return percentage;
        }
        return 0;
    }
}
