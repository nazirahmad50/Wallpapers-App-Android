package com.androidhive.ultrahdwallpapers.Database.DataSource;

import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;

import java.util.List;

import io.reactivex.Flowable;

//2.
public interface IRecentsDataSource {

    Flowable<List<RecentsDatabase>> getAllRecents();
    Flowable<RecentsDatabase> getKey(String keyId);


    void insertRecents(RecentsDatabase...recentsDatabases);
    void updateRecents(RecentsDatabase...recentsDatabases);
    void deleteRecents(RecentsDatabase...recentsDatabases);
    void deleteAllRecents();

}
