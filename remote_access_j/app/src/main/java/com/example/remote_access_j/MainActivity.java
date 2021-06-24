package com.example.remote_access_j;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    ConstraintLayout constraintLayout;
    private Spinner spinner;
    private static TextView welcome_to_ra, access_key_verif_error_text;
    private static Button settings_button, stop_sound_button;
    private static ImageView ra_enabled_button_image_view, ra_enabled_icon;
    private EditText verification_access_key_text, forgot_access_key_security_answer_text, forgot_access_key_new_access_key_text, forgot_access_key_re_enter_new_access_key_text;
    private static final String[] security_ques = {"What Is your favorite book?", "What is your mother’s maiden name?", "Where did you go to high school/college?"};
    public static Switch lock_perm_switch, setting_p_lock_switch, contacts_perm_switch, setting_p_contacts_switch ;
    /******
     * Used for Location
     */
    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds

    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName compName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*****************************
         to check if the required permission is enabled or not
         ******************************/
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintlayout);
        final String[] permission = new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_CONTACTS};
        if (ActivityCompat.checkSelfPermission(this, permission[0]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[0]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[1]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[2]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[3]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[4]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[5]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[6]) != PackageManager.PERMISSION_GRANTED
                /*&& ActivityCompat.checkSelfPermission(this, permission[7]) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission[8]) != PackageManager.PERMISSION_GRANTED*/
        ) {
            requestPermissions(permission, 1000);
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }

        //lock device code
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        devicePolicyManager.removeActiveAdmin(compName);

        String ra_enabled = KeyValueDB.getSPData(getApplicationContext(), "ra_enabled");
        if (ra_enabled.equals("yes")) {
            welcome_to_ra = findViewById(R.id.welcome_to_ra);
            ra_enabled_button_image_view = findViewById(R.id.enable_rc_button);
            ra_enabled_icon = findViewById(R.id.ra_enabled_icon);
            settings_button = findViewById(R.id.settings);

            if (Globals.SoundButton == 1) {
                stop_sound_button = findViewById(R.id.stop_sound);
                stop_sound_button.setVisibility(View.VISIBLE);
                stop_sound_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Services.makeSound(getApplicationContext(), "stop");
                        recreate();
                        Globals.SoundButton = 0;
                    }
                });
            }

            ra_enabled_button_image_view.setVisibility(View.INVISIBLE);
            settings_button.setVisibility(View.VISIBLE);
            ra_enabled_icon.setVisibility(View.VISIBLE);
            welcome_to_ra.setText("Remote Access Enabled");
            GoogleApiClient();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    onLocationChanged(location);
                    handler.postDelayed(this, 5000);
                }
            }, 5000);
        }
    }

    private void GoogleApiClient() {
        // we build google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();
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
        ComponentName compName = new ComponentName(this, MyAdmin.class);
        /*boolean active = devicePolicyManager.isAdminActive(compName);
        if (active) lock_perm_switch.setChecked(true);
        else lock_perm_switch.setChecked(false);*/

        lock_perm_switch = dialog.findViewById(R.id.lock_p_switch);
        lock_perm_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Clicked on lock permission", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
                    startActivityForResult(intent, 11);

                    //Snackbar.make(constraintLayout, "!!! Switch Enabled !!!", Snackbar.LENGTH_LONG).show();
                } else {
                    devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

                    devicePolicyManager.removeActiveAdmin(compName);
                    Toast.makeText(getApplicationContext(), "!!! LockDevice Permission/Feature Disabled !!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        contacts_perm_switch = dialog.findViewById(R.id.contacts_p_switch);

        String contacts_saved = KeyValueDB.getSPData(getApplicationContext(), "contacts_saved");
        if (contacts_saved.equals("yes")) {
            contacts_perm_switch.setChecked(true);
            Log.d("contacts", "contacts_saved = \"yes\"");

        } else {
            contacts_perm_switch.setChecked(false);
            Log.d("contacts", "contacts_saved = \"no\"");

        }

        contacts_perm_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Services services = new Services();
                if (isChecked) {
                    services.saveContacts(getApplicationContext());
                    KeyValueDB.setSPData(getApplicationContext(), "contacts_saved", "yes");
                    Log.d("contacts", "contacts saved in DB");
                    Toast.makeText(MainActivity.this, "Contacts Permission Enabled!!", Toast.LENGTH_SHORT).show();

                } else {
                    services.DeleteContactTable(getApplicationContext());
                    KeyValueDB.setSPData(getApplicationContext(), "contacts_saved", "no");
                    Log.d("contacts", "contacts deleted from DB");
                    Toast.makeText(MainActivity.this, "Contacts Permission Disabled!!", Toast.LENGTH_SHORT).show();
                }
            }
        });


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
                    recreate();
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
         checks if lock permissions are enabled, if yes switch is enabled
         ******************************/
        boolean active = devicePolicyManager.isAdminActive(compName);
        setting_p_lock_switch = dialog.findViewById(R.id.settings_lock_p_switch);
        if (active) setting_p_lock_switch.setChecked(true);
        else setting_p_lock_switch.setChecked(false);
        /*****************************
         lock permission switch actions
         ******************************/
        setting_p_lock_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Clicked on lock permission", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
                    startActivityForResult(intent, 11);

                    //Snackbar.make(constraintLayout, "!!! Switch Enabled !!!", Snackbar.LENGTH_LONG).show();
                } else {
                    devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

                    devicePolicyManager.removeActiveAdmin(compName);
                    Toast.makeText(getApplicationContext(), "!!! LockDevice Permission/Feature Disabled !!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        setting_p_contacts_switch = dialog.findViewById(R.id.settings_contacts_p_switch);
        String contacts_saved = KeyValueDB.getSPData(getApplicationContext(), "contacts_saved");
        if (contacts_saved.equals("yes")) {
            setting_p_contacts_switch.setChecked(true);
            Log.d("contacts", "contacts_saved = \"yes\"");

        } else {
            setting_p_contacts_switch.setChecked(false);
            Log.d("contacts", "contacts_saved = \"no\"");

        }

        setting_p_contacts_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Services services = new Services();
                if (isChecked) {
                    services.saveContacts(getApplicationContext());
                    KeyValueDB.setSPData(getApplicationContext(), "contacts_saved", "yes");
                    Toast.makeText(MainActivity.this, "Contacts Permission Enabled!!", Toast.LENGTH_SHORT).show();
                    Log.d("contacts", "contacts saved in DB");
                } else {
                    services.DeleteContactTable(getApplicationContext());
                    KeyValueDB.setSPData(getApplicationContext(), "contacts_saved", "no");
                    Log.d("contacts", "contacts deleted from DB");
                    Toast.makeText(MainActivity.this, "Contacts Permission Disabled!!", Toast.LENGTH_SHORT).show();

                }
            }
        });



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

        /*****************************
         disable remote access feature
         ******************************/

        settings_disable_ra_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisableRA();
            }


        });
        dialog.show();
    }

    private void DisableRA() {
        KeyValueDB.setSPData(getApplicationContext(), "ra_enabled", "no");
        Globals.RAEnabled = 0;
        Toast.makeText(MainActivity.this, "Remote Access Disabled, You cannot access any features of this application untill you enable it again", Toast.LENGTH_LONG).show();
        recreate();
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
                    Snackbar.make(constraintLayout, "Your New Access Key is set to: " + forgot_access_key_new_access_key_text.getText().toString(), Snackbar.LENGTH_LONG).show();
                    showAccessKeyVerificationDialog(v);
                }
            }
        });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            Toast.makeText(this, "You need to install Google Play Services to use the App properly", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
//        if (googleApiClient != null && googleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
//            googleApiClient.disconnect();
//        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            Toast.makeText(getApplicationContext(), "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            KeyValueDB.setSPData(getApplicationContext(), "loc_lat", String.valueOf(location.getLatitude()));
            KeyValueDB.setSPData(getApplicationContext(), "loc_long", String.valueOf(location.getLongitude()));

        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            //Toast.makeText(getApplicationContext(), "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            KeyValueDB.setSPData(getApplicationContext(), "loc_lat", String.valueOf(location.getLatitude()));
            KeyValueDB.setSPData(getApplicationContext(), "loc_long", String.valueOf(location.getLongitude()));
        }
    }
}