package com.androidhive.ultrahdwallpapers.Database.Repository;

import com.androidhive.ultrahdwallpapers.Database.DataSource.IRecentsDataSource;
import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;

import java.util.List;

import io.reactivex.Flowable;

public class RecentRepositery implements IRecentsDataSource {

    private final IRecentsDataSource mLocalDataSource;
    private static RecentRepositery instance;

    private RecentRepositery(IRecentsDataSource mLocalDataSource){

        this.mLocalDataSource = mLocalDataSource;
    }

    public static RecentRepositery getInstance(IRecentsDataSource mLocalDataSource){

        if (instance == null){

            instance = new RecentRepositery(mLocalDataSource);
        }
        return instance;
    }

    @Override
    public Flowable<List<RecentsDatabase>> getAllRecents() {
        return mLocalDataSource.getAllRecents();
    }

    @Override
    public Flowable<RecentsDatabase> getKey(String keyId) {
        return mLocalDataSource.getKey(keyId);
    }

    @Override
    public void insertRecents(RecentsDatabase... recentsDatabases) {

        mLocalDataSource.insertRecents(recentsDatabases);

    }

    @Override
    public void updateRecents(RecentsDatabase... recentsDatabases) {

        mLocalDataSource.updateRecents(recentsDatabases);

    }

    @Override
    public void deleteRecents(RecentsDatabase... recentsDatabases) {

        mLocalDataSource.deleteRecents(recentsDatabases);

    }

    @Override
    public void deleteAllRecents() {

        mLocalDataSource.deleteAllRecents();

    }
}
