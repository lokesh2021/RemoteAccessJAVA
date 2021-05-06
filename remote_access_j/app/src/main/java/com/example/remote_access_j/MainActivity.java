package com.example.remote_access_j;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText access_key_inp, reenter_access_key_inp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*****************************
         to check if the required permissions are enabled or not
         ******************************/
        final String[] permission = new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS};
        requestPermissions(permission, 1000);
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
        final Dialog dialog = new Dialog(this);
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
                /*****************************
                 if both the access keys are equal the password is set in sharedpreferences
                 else an error message is displayed on the screen
                 ******************************/
                if (access_key_inp.getText().toString().equals(reenter_access_key_inp.getText().toString())) {
                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor shrd_pref_access_key = sharedPreferences.edit();
                    shrd_pref_access_key.putString("access_key_inp", access_key_inp.toString());
                    Toast.makeText(getApplicationContext(), access_key_inp.toString(), Toast.LENGTH_SHORT).show();
                    dialog.findViewById(R.id.error_text).setVisibility(View.INVISIBLE);
                    dialog.dismiss(); //dialog is dismissed when the password is set
                    findViewById(R.id.enable_rc_button).setVisibility(View.INVISIBLE);// the button which opens the dialog is deleted
                    Toast.makeText(getApplicationContext(), "Remote Access Enabled!!", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.findViewById(R.id.error_text).setVisibility(View.VISIBLE);
                }

            }
        });
        dialog.show();

    }
}