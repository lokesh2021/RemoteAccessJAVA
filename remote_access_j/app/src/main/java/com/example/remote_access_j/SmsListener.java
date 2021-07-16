package com.example.remote_access_j;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.BATTERY_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.example.remote_access_j.Services.sendSMSMessage;


public class SmsListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from = "";
            String msgBody = "";
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msgBody = msgs[i].getMessageBody();
                    }
                    /*****************************
                     //checks if the SMS has OTP and saves the OTP
                     ******************************/
                    VerifyOTP(context, msgBody);

                    /***********
                     Performs Actions for the received messages
                     **********/
                    String shrpf_ra_enabled = KeyValueDB.getSPData(context, "ra_enabled");
                    if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access") && shrpf_ra_enabled.equals("yes")) {
                        processReceivedMessage(context, msg_from, msgBody);
                    }
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }


    private void processReceivedMessage(Context context, String msg_from, String msgBody) {
        //get the access_key from sharedpreference
        String shrpf_access_key = KeyValueDB.getSPData(context, "access_key");
        //responding to the message "remote_access" with the message "Welcome Sir!, How can i help you?"
        if (msgBody.equalsIgnoreCase("remote_access")) {
            //responds to the message "remote_acess" with "Welcome Sir!, How can i help you?"
            //sending SMS to the Sender
            sendSMSMessage(context, msg_from, "Welcome Sir!, How can i help you?\n", "no");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equalsIgnoreCase("--help")) {
            //responds to the message "remote_acess --help" with instructions
            String help_msg = "Help Info:\nMessage format: remote_access <password> <action>\nActions Available:\n1.getContact <contactname>\n2.getLocation\n3.makeSound\n4.setLockScreen\n";
            //sending help instructions to the Sender
            sendSMSMessage(context, msg_from, help_msg, "no");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("getlocation")) {
            //responds to the message "remote_acess <access_key> getLocation" with the users location
            String loc_lat = KeyValueDB.getSPData(context, "loc_lat");//getting the lat/long from ShrdPrefs
            String loc_long = KeyValueDB.getSPData(context, "loc_long");
            //sending location link & co-ordinates to the Sender
            Log.d("Location: ", "Location Latitude" + loc_lat);
            sendSMSMessage(context, msg_from, "Your Mobile Location is at: https://www.latlong.net/c/?lat=" + loc_lat + "&long=" + loc_long +/*+ "\nThe GPS co-ordinates are latitude:" + loc_lat + "& longitude:" + loc_long+*/"\n", "yes");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("lockscreen")) {
            //responds to the message "remote_acess <access_key> lockscreen"
            Services.lock(context);
            sendSMSMessage(context, msg_from, "Your Device is Locked\n", "yes");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("ringermode")) {
            //responds to the message "remote_acess <access_key> ringermode"
            Services.setRinger(context,msg_from);
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("makesound")) {
            //responds to the message "remote_acess <access_key> makesound"
            Services.makeSound(context, "start",msg_from);
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("getotp")) {
            //responds to the message "remote_acess <access_key> getotp"
            String otp_message = KeyValueDB.getSPData(context, "otp_message");
            String otp_time = KeyValueDB.getSPData(context, "otp_time");
            Log.d("otp", "Message Sent: -- " + otp_time + " -> " + otp_message);
            //sending SMS with OTP Details.
            sendSMSMessage(context, msg_from, otp_message, "no");
            //sending SMS with OTP Received time.
            sendSMSMessage(context, msg_from, "Time when the OTP was saved: " + otp_time + "\n", "yes");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("getcontact")) {
            //responds to the message "remote_acess <access_key> getcontact <contact_name>"
            String contact_name = msgBody.split(" ")[3];
            if (contact_name.length() < 3) {
                sendSMSMessage(context, msg_from, "Contact name cannot be less than 3 characters, please try again!!\n", "yes");
            } else {
                Services services = new Services();
                services.getContactDetails(context, msg_from, contact_name);
            }
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("batterystatus")) {
            sendSMSMessage(context, msg_from, "", "yes");
        }
    }

    private void VerifyOTP(Context context, String msgBody) {
        if (!msgBody.split(" ")[0].equalsIgnoreCase("remote_access")) {
            boolean otp1 = msgBody.toLowerCase().contains("otp");
            boolean otp2 = msgBody.toLowerCase().contains("one") && msgBody.toLowerCase().contains("time") && msgBody.toLowerCase().contains("password");
            boolean otp3 = msgBody.toLowerCase().contains("verification") && msgBody.toLowerCase().contains("code");
            if (otp1 || otp2 || otp3) {
                //saving the OTP Message
                KeyValueDB.setSPData(context, "otp_message", msgBody);
                //saving the Time when OTP was received
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
                String OTPDateandTime = sdf.format(new Date());
                KeyValueDB.setSPData(context, "otp_time", OTPDateandTime);
                Log.d("otp", OTPDateandTime + ":  " + msgBody);
            }
        }
    }


}