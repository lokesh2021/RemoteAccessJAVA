package com.example.remote_access_j;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts_table")
public class Contacts {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "contact_name")
    private String contact_name;
    @ColumnInfo(name = "contact_number")
    private String contact_number;

    public Contacts(int id, String contact_name, String contact_number) {
        this.id = id;
        this.contact_name = contact_name;
        this.contact_number = contact_number;
    }

    @Ignore
    public Contacts(String contact_name, String contact_number) {
        this.contact_name = contact_name;
        this.contact_number = contact_number;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String name) {
        this.contact_name = name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String email) {
        this.contact_number = email;
    }
}
