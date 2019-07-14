package com.androidhive.ultrahdwallpapers.Database.Dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;

import java.util.List;

import io.reactivex.Flowable;

//3
@Dao //Provides an API for reading and writing data
public interface IRecentsDao {

    @Query("SELECT * FROM recents ORDER BY saveTime DESC LIMIT 30")
    Flowable<List<RecentsDatabase>> getAllRecents();

    @Query("SELECT * FROM recents WHERE `key`=:keyId")
    Flowable<RecentsDatabase> getKey(String keyId);

    @Insert()
    void insertRecents(RecentsDatabase...recentsDatabases);

    @Update
    void updateRecents(RecentsDatabase...recentsDatabases);

    @Delete
    void deleteRecents(RecentsDatabase...recentsDatabases);

    @Query("DELETE FROM recents")
    void deleteAllRecents();
}
