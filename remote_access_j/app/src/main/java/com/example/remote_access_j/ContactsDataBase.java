package com.example.remote_access_j;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Contacts.class, exportSchema = false, version = 1)
public abstract class ContactsDataBase extends RoomDatabase {
    private static final String LOG_TAG = ContactsDataBase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "contacts";
    private static ContactsDataBase sInstance;

    public static ContactsDataBase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        ContactsDataBase.class, ContactsDataBase.DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract ContactsDoa ContactsDoa();
}