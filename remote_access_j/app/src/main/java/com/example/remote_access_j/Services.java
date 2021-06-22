package com.example.remote_access_j;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

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

    public static void setRinger(Context context) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio.setRingerMode(2);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.MODE_RINGTONE);
        float percent = 0.7f;
        int seventyVolume = (int) (maxVolume * percent);
        audio.setStreamVolume(AudioManager.MODE_RINGTONE, seventyVolume, 0);
    }

    public static void makeSound(Context context, String str) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        if (str == "start") {
            r.play();
            Globals.SoundButton = 1;
        } else {
            r.stop();
            Globals.SoundButton = 0;
        }
    }

    public static int batteryStatus(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            return percentage;
        }
        return 0;
    }

    public void saveContacts(Context context) {

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        long startnow;
        long endnow;

        startnow = android.os.SystemClock.uptimeMillis();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ContactsDataBase contactsDataBase = ContactsDataBase.getInstance(context);
                // Loop Through All The Numbers
                while (phones.moveToNext()) {

                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    // Cleanup the phone number
                    phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

                    //saving the contact details in the ContactsDataBase
                    Contacts contact = new Contacts(name.toLowerCase(), phoneNumber);
                    contactsDataBase.ContactsDoa().insertContacts(contact);
                }
                phones.close();
            }
        });
        endnow = android.os.SystemClock.uptimeMillis();
        Log.d("MYTAG", "Execution time to save contacts is: " + (endnow - startnow) + " ms");
    }

    public void getContactDetails(Context context, String msg_from, String name) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ContactsDataBase contactsDataBase = ContactsDataBase.getInstance(context);
                //loading all the contacts that start with <name> into result_from_database
                List<Contacts> result_from_database = contactsDataBase.ContactsDoa().loadContacts(name + "%");
                int contacts_list_size = result_from_database.size();
                String result = "";
                //adding all the similar contacts into the string to reply
                for (int i = 0; i < contacts_list_size; i++) {
                    result += result_from_database.get(i).getContact_name() + ": " + result_from_database.get(i).getContact_number() + "\n";
                }
                Log.d("contact_result", result);
                sendSMSMessage(context, msg_from, result, "yes");
            }
        });

    }

    public static void sendSMSMessage(Context context, String msg_from, String msgBody, String sendBatteryStatus) {
        int battery_status = Services.batteryStatus(context);
        SmsManager smsManager = SmsManager.getDefault();
        if (sendBatteryStatus.equals("yes")) {
            smsManager.sendTextMessage(msg_from, null, msgBody + "Battery Status: " + battery_status + "%", null, null);
            Log.d("debug", "Remote Access has responded to the received SMS");
        } else {
            smsManager.sendTextMessage(msg_from, null, msgBody, null, null);
        }
    }

}
