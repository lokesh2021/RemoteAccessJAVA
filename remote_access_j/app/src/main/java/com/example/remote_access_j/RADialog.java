package com.example.remote_access_j;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RADialog extends AppCompatActivity {
    private static EditText access_key_inp, reenter_access_key_inp, sq_answer;
    private static TextView error_text;

    public RADialog() {
    }

    public static void EnableRADialog(View v, Dialog dialog, Context context) {
        access_key_inp = dialog.findViewById(R.id.access_key);
        reenter_access_key_inp = dialog.findViewById(R.id.reenter_access_key);
        error_text = dialog.findViewById(R.id.error_text);
        sq_answer = dialog.findViewById(R.id.sq_answer);
        /*****************************
         if both the access keys are equal the password is set in sharedpreferences
         else an error message is displayed on the screen
         ******************************/
        if (access_key_inp.getText().toString().isEmpty() || reenter_access_key_inp.getText().toString().isEmpty() || sq_answer.getText().toString().isEmpty()) {
            error_text.setText("Access Key/Security Question length cannot be Empty, Please try again!!!");
            error_text.setVisibility(View.VISIBLE);
        } else if (access_key_inp.getText().toString().length() < 5 || reenter_access_key_inp.getText().toString().length() < 5) {
            error_text.setText("Access Key's length cannot be less than 5, Please try again!!");
            error_text.setVisibility(View.VISIBLE);
        } else if (access_key_inp.getText().toString().equals(reenter_access_key_inp.getText().toString())) {
            KeyValueDB.setSPData(context, "access_key", access_key_inp.getText().toString());
            KeyValueDB.setSPData(context, "ra_enabled", "true");//next_update
            Toast.makeText(context, "Access Key Successfully set to: " + access_key_inp.getText().toString(), Toast.LENGTH_SHORT).show();
            dialog.findViewById(R.id.error_text).setVisibility(View.INVISIBLE);
            KeyValueDB.setSPData(context, "sq_answer", sq_answer.getText().toString());
            Toast.makeText(context, "security answer is set to: "+sq_answer.getText().toString(), Toast.LENGTH_SHORT).show();
            dialog.dismiss(); //dialog is dismissed when the access key is set
            //next_update findViewById(R.id.enable_rc_button).setVisibility(View.INVISIBLE);// the button which opens the dialog is deleted
            Toast.makeText(context, "Remote Access Enabled!!", Toast.LENGTH_SHORT).show();
        } else {
            dialog.findViewById(R.id.error_text).setVisibility(View.VISIBLE);
            error_text.setText("Access keys did not match, please try again!!");
        }


    }
}
