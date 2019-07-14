package com.androidhive.ultrahdwallpapers.Database.LocalDatabase;

import com.androidhive.ultrahdwallpapers.Database.Dao.IRecentsDao;
import com.androidhive.ultrahdwallpapers.Database.DataSource.IRecentsDataSource;
import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;

import java.util.List;

import io.reactivex.Flowable;

//4
public class RecentsLocalDatabase implements IRecentsDataSource {

    private final IRecentsDao iRecentsDao;
    private static RecentsLocalDatabase instance;

    private RecentsLocalDatabase(IRecentsDao iRecentsDao) {
        this.iRecentsDao = iRecentsDao;
    }

    public static RecentsLocalDatabase getInstance(IRecentsDao iRecentsLocalDatabases){

        if (instance == null){

            instance = new RecentsLocalDatabase(iRecentsLocalDatabases);
        }
        return instance;
    }

    @Override
    public Flowable<List<RecentsDatabase>> getAllRecents() {
        return iRecentsDao.getAllRecents();
    }

    @Override
    public Flowable<RecentsDatabase> getKey(String keyId) {
        return iRecentsDao.getKey(keyId);
    }

    @Override
    public void insertRecents(RecentsDatabase... recentsDatabases) {

        iRecentsDao.insertRecents(recentsDatabases);
    }

    @Override
    public void updateRecents(RecentsDatabase... recentsDatabases) {

        iRecentsDao.updateRecents(recentsDatabases);

    }

    @Override
    public void deleteRecents(RecentsDatabase... recentsDatabases) {

        iRecentsDao.deleteRecents(recentsDatabases);

    }

    @Override
    public void deleteAllRecents() {

        iRecentsDao.deleteAllRecents();

    }
}
