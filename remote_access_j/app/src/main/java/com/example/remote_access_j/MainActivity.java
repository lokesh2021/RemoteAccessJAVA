package com.example.remote_access_j;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ConstraintLayout constraintLayout;
    private Spinner spinner;
    private static TextView welcome_to_ra, access_key_verif_error_text;
    private static Button settings_button;
    private static ImageView ra_enabled_button_image_view, ra_enabled_icon;
    private EditText verification_access_key_text, forgot_access_key_security_answer_text, forgot_access_key_new_access_key_text, forgot_access_key_re_enter_new_access_key_text;
    private static final String[] security_ques = {"What Is your favorite book?", "What is your mother’s maiden name?", "Where did you go to high school/college?"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*****************************
         to check if the required permission is enabled or not
         ******************************/
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintlayout);
        final String[] permission = new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE};
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, 1000);
        }

        String ra_enabled = KeyValueDB.getSPData(getApplicationContext(), "ra_enabled");
        if (ra_enabled.equals("yes")) {
            welcome_to_ra = findViewById(R.id.welcome_to_ra);
            ra_enabled_button_image_view = findViewById(R.id.enable_rc_button);
            ra_enabled_icon = findViewById(R.id.ra_enabled_icon);
            settings_button = findViewById(R.id.settings);
            ra_enabled_button_image_view.setVisibility(View.INVISIBLE);
            settings_button.setVisibility(View.VISIBLE);
            ra_enabled_icon.setVisibility(View.VISIBLE);
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
                String ra_enabled = KeyValueDB.getSPData(getApplicationContext(), "ra_enabled");
                if (Globals.RAEnabled == 1 && ra_enabled.equals("yes")) {
                    welcome_to_ra = findViewById(R.id.welcome_to_ra);
                    ra_enabled_button_image_view = findViewById(R.id.enable_rc_button);
                    ra_enabled_icon = findViewById(R.id.ra_enabled_icon);
                    settings_button = findViewById(R.id.settings);
                    ra_enabled_button_image_view.setVisibility(View.INVISIBLE);
                    settings_button.setVisibility(View.VISIBLE);
                    ra_enabled_icon.setVisibility(View.VISIBLE);
                    welcome_to_ra.setText("Remote Access Enabled");
                    Snackbar.make(constraintLayout, "!!! Remote Access Enabled !!!", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                KeyValueDB.setSPData(getApplicationContext(), "sq_question", "0");
                //Toast.makeText(this, "Your Security Question is: " + security_ques[0], Toast.LENGTH_SHORT).show();
                break;
            case 1:
                KeyValueDB.setSPData(getApplicationContext(), "sq_question", "1");
                //Toast.makeText(this, "Your Security Question is: " + security_ques[1], Toast.LENGTH_SHORT).show();
                break;
            case 2:
                KeyValueDB.setSPData(getApplicationContext(), "sq_question", "2");
                //Toast.makeText(this, "Your Security Question is: " + security_ques[2], Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void showAccessKeyVerificationDialog(View view) {
        final Dialog dialog = new Dialog(this, R.style.CustomAlertDialog);
        dialog.setContentView(R.layout.access_key_verification_dialog);

        /*****************************
         Performs the required task when the "verify access key" button is pressed
         ******************************/
        verification_access_key_text = dialog.findViewById(R.id.access_key_verification_edittext);
        Button dialogButton = (Button) dialog.findViewById(R.id.verifi_access_key_button);
        String access_key = KeyValueDB.getSPData(getApplicationContext(), "access_key");
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /********************
                 when Save Access Key button is clicked, this function is called to perform data validation in Dialog
                 **********************/
                access_key_verif_error_text = dialog.findViewById(R.id.access_key_verif_error_text);
                if (verification_access_key_text.getText().toString().equals(access_key)) {
                    access_key_verif_error_text.setVisibility(View.INVISIBLE);
                    dialog.dismiss();
                    Snackbar.make(constraintLayout, "Access Key Verified", Snackbar.LENGTH_LONG).show();

                    showSettingsDialog();
                } else if (verification_access_key_text.getText().toString().isEmpty()) {
                    access_key_verif_error_text.setText("Access key cannot be empty, please try again!!!");
                    access_key_verif_error_text.setVisibility(View.VISIBLE);
                } else {
                    access_key_verif_error_text.setText("Access key is incorrect, please try again!!!");
                    access_key_verif_error_text.setVisibility(View.VISIBLE);
                }
            }


        });
        dialog.show();
    }

    private void showSettingsDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomAlertDialog);
        dialog.setContentView(R.layout.settings_dialog);

        /*****************************
         Performs the required task when the "verify access key" button is pressed
         ******************************/
        Button settings_edit_access_key_button = dialog.findViewById(R.id.edit_access_key_button);
        Button settings_disable_ra_button = (Button) dialog.findViewById(R.id.disable_ra_button);
        settings_edit_access_key_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showEditAccessKeyDialog();
            }


        });

        settings_disable_ra_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyValueDB.setSPData(getApplicationContext(),"ra_enabled","no");
                Globals.RAEnabled=0;
                Toast.makeText(MainActivity.this, "Remote Access Disabled, You cannot access any features of this application untill you enable it again", Toast.LENGTH_LONG).show();
                recreate();
            }


        });
        dialog.show();
    }

    private void showEditAccessKeyDialog() {

        final Dialog dialog = new Dialog(this, R.style.CustomAlertDialog);
        dialog.setContentView(R.layout.edit_access_key);

        /*****************************
         Performs the required task when the "verify access key" button is pressed
         ******************************/
        Button settings_save_access_key_button = dialog.findViewById(R.id.edit_access_key_save_new_access_key);
        settings_save_access_key_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit_access_key_new_acces_key_text = dialog.findViewById(R.id.edit_access_key_new_acces_key);
                EditText edit_access_key_new_acces_key_rep_text = dialog.findViewById(R.id.edit_access_key_new_acces_key_rep);
                TextView edit_access_key_error_text = dialog.findViewById(R.id.edit_access_key_error_text);
                if (edit_access_key_new_acces_key_rep_text.getText().toString().isEmpty() || edit_access_key_new_acces_key_text.getText().toString().isEmpty()) {
                    edit_access_key_error_text.setText("Access Key cannot be empty, please try again!!!");
                    edit_access_key_error_text.setVisibility(View.VISIBLE);
                } else if (edit_access_key_new_acces_key_rep_text.getText().toString().length() < 5 || edit_access_key_new_acces_key_text.getText().toString().length() < 5) {
                    edit_access_key_error_text.setText("Access Key length cannot be less than 5, please try again!!!");
                    edit_access_key_error_text.setVisibility(View.VISIBLE);
                } else if (!edit_access_key_new_acces_key_rep_text.getText().toString().equals(edit_access_key_new_acces_key_text.getText().toString())) {
                    edit_access_key_error_text.setText("Access keys do not match, please try again!!!");
                    edit_access_key_error_text.setVisibility(View.VISIBLE);
                } else if (edit_access_key_new_acces_key_rep_text.getText().toString().equals(edit_access_key_new_acces_key_text.getText().toString())) {
                    KeyValueDB.setSPData(getApplicationContext(), "access_key", edit_access_key_new_acces_key_text.getText().toString());
                    dialog.dismiss();
                    Snackbar.make(constraintLayout, "New Access Key is set to: " + edit_access_key_new_acces_key_text.getText().toString(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();

    }

    public void showForgotPasswordDialog(View view) {
        final Dialog dialog = new Dialog(this, R.style.CustomAlertDialog);
        dialog.setContentView(R.layout.forgot_access_key_dialog);

        /*****************************
         Performs the required task when the "enable_rc_button_menu" button is pressed
         ******************************/
        String sq_question_number = KeyValueDB.getSPData(getApplicationContext(), "sq_question");
        TextView forgot_access_key_sq_question = dialog.findViewById(R.id.forgot_access_key_security_question_textview);
        forgot_access_key_sq_question.setText(security_ques[Integer.parseInt(sq_question_number)]);
        Button dialogButton = (Button) dialog.findViewById(R.id.forgot_access_key_save_access_key_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /********************
                 when Save Access Key button is clicked, this function is called to perform data validation in Dialog
                 **********************/

                String sq_answer = KeyValueDB.getSPData(getApplicationContext(), "sq_answer");
                forgot_access_key_security_answer_text = dialog.findViewById(R.id.forgot_access_key_security_answer_text);
                forgot_access_key_new_access_key_text = dialog.findViewById(R.id.forgot_passwd_enter_new_acces_key);
                forgot_access_key_re_enter_new_access_key_text = dialog.findViewById(R.id.forgot_passwd_re_enter_new_acces_key);
                TextView forgot_aceess_key_error_text = dialog.findViewById(R.id.forgot_access_key_error_text);
                //Toast.makeText(MainActivity.this, "sq_answer:+" + sq_answer + "+" + forgot_access_key_security_answer_text.getText().toString(), Toast.LENGTH_LONG).show();

                if (forgot_access_key_new_access_key_text.getText().toString().isEmpty()
                        || forgot_access_key_re_enter_new_access_key_text.getText().toString().isEmpty()
                        || forgot_access_key_security_answer_text.getText().toString().isEmpty()) {
                    forgot_aceess_key_error_text.setText("Text Fields cannot be empty, please try again!!!");
                    forgot_aceess_key_error_text.setVisibility(View.VISIBLE);
                } else if (forgot_access_key_new_access_key_text.getText().toString().length() < 5 &&
                        forgot_access_key_re_enter_new_access_key_text.getText().toString().length() < 5) {
                    forgot_aceess_key_error_text.setText("Access Key length cannot be less than 5, please try again!!!");
                    forgot_aceess_key_error_text.setVisibility(View.VISIBLE);
                } else if (!forgot_access_key_security_answer_text.getText().toString().equals(sq_answer)) {
                    forgot_aceess_key_error_text.setText("Security Answer is incorrect, please try again!!!");
                    forgot_aceess_key_error_text.setVisibility(View.VISIBLE);
                } else if (!forgot_access_key_new_access_key_text.getText().toString().equals(forgot_access_key_re_enter_new_access_key_text.getText().toString())) {
                    forgot_aceess_key_error_text.setText("Access keys did not match, please try again!!");
                    forgot_aceess_key_error_text.setVisibility(View.VISIBLE);
                } else if (forgot_access_key_security_answer_text.getText().toString().equals(sq_answer)
                        && forgot_access_key_new_access_key_text.getText().toString().equals(forgot_access_key_re_enter_new_access_key_text.getText().toString())) {
                    forgot_aceess_key_error_text.setVisibility(View.INVISIBLE);
                    KeyValueDB.setSPData(getApplicationContext(), "access_key", forgot_access_key_new_access_key_text.getText().toString());
                    dialog.dismiss();
                    Snackbar.make(constraintLayout, "Your New Access Key is set to: "+forgot_access_key_new_access_key_text.getText().toString(), Snackbar.LENGTH_LONG).show();
                    showAccessKeyVerificationDialog(v);
                }
            }
        });
        dialog.show();
    }
}