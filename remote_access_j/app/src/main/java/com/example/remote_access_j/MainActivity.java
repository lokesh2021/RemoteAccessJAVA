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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spinner;
    private Button edit_access_key_button,disable_remote_access_button;
    private static TextView welcome_to_ra;
    private static final String[] security_ques = {"What Is your favorite book?", "What is your mother’s maiden name?", "Where did you go to high school/college?"};

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

        String ra_enabled=KeyValueDB.getSPData(getApplicationContext(),"ra_enabled");
        if(ra_enabled.equals("true")){
            welcome_to_ra=findViewById(R.id.welcome_to_ra);
            edit_access_key_button=findViewById(R.id.edit_access_key_button);
            disable_remote_access_button=findViewById(R.id.disable_remote_access_button);
            edit_access_key_button.setVisibility(View.VISIBLE);
            disable_remote_access_button.setVisibility(View.VISIBLE);
            welcome_to_ra.setText("Remote Access Enabled");
        }


//        String mPhoneNumber = tMgr.getLine1Number();
//        TextView contacttext = findViewById(R.id.contacttextview);
//        contacttext.setText(mPhoneNumber);
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

        /**************************
         *Sequrity Question Spinner declaration and its working
         **************************/
        spinner = (Spinner) dialog.findViewById(R.id.sq_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, security_ques);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        /*****************************
         Performs the required task when the "enable_rc_button_menu" button is pressed
         ******************************/
        Button dialogButton = (Button) dialog.findViewById(R.id.enable_rc_button_menu);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*********************
                 when Save Access Key button is clicked, this function is called to perform data validation in Dialog
                 ***********************/
                RADialog.EnableRADialog(v, dialog, getApplicationContext());
            }
        });
        dialog.show();


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                KeyValueDB.setSPData(getApplicationContext(), "sq_question", "0");
                Toast.makeText(this,"Your Security Question is: "+ security_ques[0], Toast.LENGTH_SHORT).show();
                break;
            case 1:
                KeyValueDB.setSPData(getApplicationContext(), "sq_question", "1");
                Toast.makeText(this,"Your Security Question is: "+  security_ques[1], Toast.LENGTH_SHORT).show();
                break;
            case 2:
                KeyValueDB.setSPData(getApplicationContext(), "sq_question", "2");
                Toast.makeText(this,"Your Security Question is: "+  security_ques[2], Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void showEditRADialog(View view) {
        final Dialog dialog = new Dialog(this, R.style.CustomAlertDialog);
        dialog.setContentView(R.layout.edit_ra_dialog);

        /*****************************
         Performs the required task when the "enable_rc_button_menu" button is pressed
         ******************************/
        Button dialogButton = (Button) dialog.findViewById(R.id.enable_rc_button_menu);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*********************
                 when Save Access Key button is clicked, this function is called to perform data validation in Dialog
                 ***********************/
                RADialog.EnableRADialog(v, dialog, getApplicationContext());
            }
        });
        dialog.show();
    }
}