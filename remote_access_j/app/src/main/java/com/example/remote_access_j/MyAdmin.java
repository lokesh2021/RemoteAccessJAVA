package com.example.remote_access_j;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyAdmin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(@androidx.annotation.NonNull Context context, @androidx.annotation.NonNull Intent intent) {
        Toast.makeText(context, "Device Admin : enabled", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDisabled(@androidx.annotation.NonNull Context context, @androidx.annotation.NonNull Intent intent) {
        Toast.makeText(context, "Device Admin : disabled", Toast.LENGTH_SHORT).show();

    }
}
