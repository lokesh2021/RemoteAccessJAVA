package com.example.remote_access_j;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyValueDB {

    /*
     * AccesKey= "access_key"
     * SecurityQuestion= "sq_question"
     * SecurityAnswer= "sq_answer"
     * Remote Access Enabled= "ra_enabled"
     * location Latitude = "loc_lat"
     * location Longitute = "loc_lat"
     *
     */
    private static String PREF_NAME = "prefs";

    public KeyValueDB() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getSPData(Context context, String key) {
        return getPrefs(context).getString(key, "");
    }

    public static void setSPData(Context context, String key, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(key, value);
        editor.commit();
    }
}
