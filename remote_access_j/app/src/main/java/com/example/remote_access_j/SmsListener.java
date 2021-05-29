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
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


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
                     Performs Actions for the received messages
                     ******************************/
                    processReceivedMessage(context, msg_from, msgBody);

                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    private void processReceivedMessage(Context context, String msg_from, String msgBody) {
        //get the access_key from sharedpreference
        String shrpf_access_key = KeyValueDB.getSPData(context, "access_key");
        String shrpf_ra_enabled = KeyValueDB.getSPData(context, "ra_enabled");


        //responding to the message "remote_access" with the message "Welcome Sir!, How can i help you?"
        if (msgBody.equalsIgnoreCase("remote_access") && shrpf_ra_enabled.equals("yes")) {
            //responds to the message "remote_acess" with "Welcome Sir!, How can i help you?"
            //sending SMS to the Sender
            sendSMSMessage(context, msg_from, "Welcome Sir!, How can i help you?");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equalsIgnoreCase("--help") && shrpf_ra_enabled.equals("yes")) {
            //responds to the message "remote_acess --help" with instructions
            String help_msg = "Help Info:\nMessage format: remote_access <password> <action>\nActions Available:\n1.getContact <contactname>\n2.getLocation\n3.ChangeProfile\n4.setLockScreen";
            //sending help instructions to the Sender
            sendSMSMessage(context, msg_from, help_msg);
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equalsIgnoreCase("password")
                && msgBody.split(" ")[2].equals(shrpf_access_key) && shrpf_ra_enabled.equals("yes")) {
            //responds to the message "remote_acess <access_key>" if the <access_key> is correct
            //sending SMS to the Sender
            sendSMSMessage(context, msg_from, "Access Key Authentication Successful");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equalsIgnoreCase("password")
                && msgBody.split(" ")[2] != shrpf_access_key && shrpf_ra_enabled.equals("yes")) {
            //responds to the message "remote_acess <access_key>" if the <access_key> is incorrect
            //sending SMS to the Sender
            sendSMSMessage(context, msg_from, "Incorrect Access Key, please try again!!!");
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("getlocation") && shrpf_ra_enabled.equals("yes")) {
            //responds to the message "remote_acess <access_key> getLocation" with the users location
            String loc_lat = KeyValueDB.getSPData(context, "loc_lat");//getting the lat/long from ShrdPrefs
            String loc_long = KeyValueDB.getSPData(context, "loc_long");
            //sending location link & co-ordinates to the Sender
            sendSMSMessage(context, msg_from, "Your Mobile Location is at: https://www.latlong.net/c/?lat=" + loc_lat + "&long=" + loc_long + "\nThe GPS co-ordinates are latitude:" + loc_lat + "& longitude:" + loc_long);
        } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                && msgBody.split(" ")[1].equals(shrpf_access_key) && msgBody.split(" ")[2].equalsIgnoreCase("setlock") && shrpf_ra_enabled.equals("yes")) {
            //responds to the message "remote_acess <access_key> setlock"
            LockDevice.lock(context);
            sendSMSMessage(context, msg_from, "Your Device is Locked");
        }
    }

    private void sendSMSMessage(Context context, String msg_from, String msgBody) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(msg_from, null, msgBody, null, null);
        Toast.makeText(context, "Remote Access has responded to the received SMS", Toast.LENGTH_LONG).show();

    }

}
