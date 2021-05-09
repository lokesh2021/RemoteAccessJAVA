package com.example.remote_access_j;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText access_key_inp, reenter_access_key_inp;
    private TextView error_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*****************************
         to check if the required permission is enabled or not
         ******************************/
        final String[] permission = new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE};
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, 1000);
        }
        String mPhoneNumber = tMgr.getLine1Number();
        TextView contacttext = findViewById(R.id.contacttextview);
        contacttext.setText(mPhoneNumber);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    /*****************************
     opens enable remote access menu
     ******************************/
    public void showRCMenu(View v) {
        final Dialog dialog = new Dialog(this, R.style.CustomAlertDialog);
        dialog.setContentView(R.layout.enable_rc_dialog);

        /*****************************
         Performs the required task when the "enable_rc_button_menu" button is pressed
         ******************************/
        Button dialogButton = (Button) dialog.findViewById(R.id.enable_rc_button_menu);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                access_key_inp = dialog.findViewById(R.id.access_key);
                reenter_access_key_inp = dialog.findViewById(R.id.reenter_access_key);
                error_text = dialog.findViewById(R.id.error_text);
                /*****************************
                 if both the access keys are equal the password is set in sharedpreferences
                 else an error message is displayed on the screen
                 ******************************/
                if (access_key_inp.getText().toString().length() < 5) {
                    error_text.setText("Access Key's length cannot be less than 5, Please try again!!");
                    error_text.setVisibility(View.VISIBLE);
                } else if (access_key_inp.getText().toString().equals(reenter_access_key_inp.getText().toString())) {
                    KeyValueDB.setSPData(getApplicationContext(), "access_key", access_key_inp.getText().toString());
                    //KeyValueDB.setSPData(getApplicationContext(), "ra_enabled", "yes");//next_update
                    Toast.makeText(getApplicationContext(), "Access Key Successfully set to: " + access_key_inp.getText().toString(), Toast.LENGTH_SHORT).show();
                    dialog.findViewById(R.id.error_text).setVisibility(View.INVISIBLE);
                    dialog.dismiss(); //dialog is dismissed when the access key is set
                    //next_update findViewById(R.id.enable_rc_button).setVisibility(View.INVISIBLE);// the button which opens the dialog is deleted
                    Toast.makeText(getApplicationContext(), "Remote Access Enabled!!", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.findViewById(R.id.error_text).setVisibility(View.VISIBLE);
                    error_text.setText("Access keys did not match, please try again!!");
                }

            }
        });
        dialog.show();

    }
}