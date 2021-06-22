package com.example.remote_access_j;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactsDoa {


    @Insert
    void insertContacts(Contacts contacts);

    @Update
    void updateContacts(Contacts contacts);

    @Delete
    void delete(Contacts contacts);

    @Query("SELECT * FROM contacts_table where contact_name like :name")
    List<Contacts> loadContacts(String name);

    @Query("SELECT COUNT(*) FROM contacts_table")
    int numberOfContacts();

    @Query("DELETE FROM contacts_table")
    void DeleteContactsTable();

}
