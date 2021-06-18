package com.example.remote_access_j;

import android.content.Context;
import android.content.SharedPreferences;

public class ContactsDB {

    private static String PREF_NAME = "contacts";

    public ContactsDB() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getContact(Context context, String key) {
        return getPrefs(context).getString(key.toLowerCase(), "");
    }

    public static void setContact(Context context, String key, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(key.toLowerCase(), value);
        editor.commit();
    }
}
