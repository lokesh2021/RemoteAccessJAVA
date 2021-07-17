package com.example.remote_access_j;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
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

    public static void setRinger(Context context, String msg_from) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio.setRingerMode(2);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.MODE_RINGTONE);
        float percent = 0.7f;
        int seventyVolume = (int) (maxVolume * percent);
        audio.setStreamVolume(AudioManager.MODE_RINGTONE, seventyVolume, 0);
        sendSMSMessage(context, msg_from, "Device sound is Enabled\n", "yes");
    }

    public static void makeSound(Context context, String str, String msg_from) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (audio.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("MyApp","Silent mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                Ringtone r = RingtoneManager.getRingtone(context, notification);
                sendSMSMessage(context, msg_from, "Playing Ringtone on device...\n", "yes");
                if (str == "start") {
                    r.play();
                    Globals.SoundButton = 1;
                } else {
                    r.stop();
                    Globals.SoundButton = 0;
                }
                break;
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

        long startnow, endnow;

        startnow = android.os.SystemClock.uptimeMillis();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ContactsDataBase contactsDataBase = ContactsDataBase.getInstance(context);
                // Loop Through All The Numbers
                Map<String, String> contacts_hm = new HashMap<>();
                while (phones.moveToNext()) {

                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    // Cleanup the phone number
                    phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

                    if(!contacts_hm.containsKey(phoneNumber)) {
                        contacts_hm.put(phoneNumber, name.toLowerCase());
                    }
                }
                phones.close();
                System.out.println(contacts_hm);
                System.out.println(contacts_hm.size());

                for (String phone: contacts_hm.keySet())
                {
                    String name=contacts_hm.get(phone);
                    Contacts contact = new Contacts(name, phone);
                    contactsDataBase.ContactsDoa().insertContacts(contact);
                }
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

                //if contact couldnt be found return a error message
                if (result.isEmpty()) {
                    String empty_contact_msg = "There was an error accessing the contacts, please try again with a different contact name!!\n";
                    Log.d("contact", empty_contact_msg);
                    sendSMSMessage(context, msg_from, empty_contact_msg, "yes");
                } else if (result.length() < 150) {
                    sendSMSMessage(context, msg_from, result, "no");
                    Log.d("contact<256", result+" \n"+result.length());
                } else {
                    String str1[] = result.split("\n");
                    List<String> contacts_list = new ArrayList<String>();
                    String fullstring = "";
                    int len = fullstring.length();

                    for (String a : str1) {
                        if ((len + a.length()) > 150) {
                            contacts_list.add(fullstring);
                            System.out.println();
                            len = 0;
                            fullstring = "";
                            fullstring = fullstring + " " + a;
                            len += a.length();
                            continue;
                        }
                        if ((len + a.length()) < 150)
                            fullstring = fullstring + " " + a;
                        len += a.length();
                    }
                    contacts_list.add(fullstring);
                    for (int i = 0; i < contacts_list.size(); i++) {
                        sendSMSMessage(context, msg_from, contacts_list.get(i), "no");
                        Log.d("contacts>256", contacts_list.get(i) + "\n");
                    }
                }
            }
        });
    }

    public void DeleteContactTable(Context context) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ContactsDataBase contactsDataBase = ContactsDataBase.getInstance(context);
                contactsDataBase.ContactsDoa().DeleteContactsTable();
            }
        });
    }

    public static void sendSMSMessage(Context context, String msg_from, String msgBody, String sendBatteryStatus) {
        int battery_status = Services.batteryStatus(context);
        SmsManager smsManager = SmsManager.getDefault();
        if (sendBatteryStatus.equals("yes")) {
            smsManager.sendTextMessage(msg_from, null, msgBody + "> Battery Status: " + battery_status + "%", null, null);
            Log.d("debug", "Remote Access has responded to the received SMS");
        } else {
            smsManager.sendTextMessage(msg_from, null, msgBody, null, null);
        }
    }


}
