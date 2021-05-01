package com.example.remote_access_j;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsListener extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        //Toast.makeText(context,"Sms Received",Toast.LENGTH_SHORT).show();
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

                    //responding to the message "remote_acess" with the message "Welcome Sir!, How can i help you?"
                    if (msgBody.equalsIgnoreCase("remote_access")) {
                        sendSMSMessage(msg_from, "Welcome Sir!, How can i help you?");
                        Log.d("debugging", "sms sent");
                        Toast.makeText(context, "SMS sent", Toast.LENGTH_LONG).show();
                    } else if (msgBody.split(" ")[0].equalsIgnoreCase("remote_access")
                            && msgBody.split(" ")[1].equalsIgnoreCase("--help")) { //responding to the message "remote_acess --help" with instructions
                        sendMessageHelp(msg_from);
                    }


                    Log.d("debugging", "Message received from: " + msg_from + " \nand the message is :" + msgBody);
                    Toast.makeText(context, "Message received from: " + msg_from + " \nand the message is :" + msgBody, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    private void sendMessageHelp(String msg_from) {
        String help_msg = "Help Instructions"; //"remote_access <password> <action>"
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(msg_from, null, help_msg, null, null);
    }

    protected void sendSMSMessage(String msg_from, String msgBody) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(msg_from, null, msgBody, null, null);
    }
}
