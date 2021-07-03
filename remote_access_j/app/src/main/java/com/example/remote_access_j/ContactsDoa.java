package com.example.remote_access_j;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactsDoa {

    //Insert Values into the DataBase
    @Insert
    void insertContacts(Contacts contacts);

    //Query to return all the contacts that start with same name
    @Query("SELECT * FROM contacts_table where contact_name like :name")
    List<Contacts> loadContacts(String name);

    //Query to return the number of contacts in the database
    @Query("SELECT COUNT(*) FROM contacts_table")
    int numberOfContacts();

    //Query to Delete the contacts_table Table from DataBase
    @Query("DELETE FROM contacts_table")
    void DeleteContactsTable();
}
