package com.androidhive.ultrahdwallpapers.Database.LocalDatabase;

import com.androidhive.ultrahdwallpapers.Database.Dao.IFavouritesDao;
import com.androidhive.ultrahdwallpapers.Database.DataSource.IFavouritesDataSource;
import com.androidhive.ultrahdwallpapers.Database.FavouritesDatabase;

import java.util.List;

import io.reactivex.Flowable;

public class FavouritesLocalDatabase implements IFavouritesDataSource {

    private final IFavouritesDao iFavouritesDao;
    private static FavouritesLocalDatabase instance;

    private FavouritesLocalDatabase(IFavouritesDao iFavouritesDao) {
        this.iFavouritesDao = iFavouritesDao;
    }

    public static FavouritesLocalDatabase getInstance(IFavouritesDao iFavouritesDao){

        if (instance == null){

            instance = new FavouritesLocalDatabase(iFavouritesDao);
        }
        return instance;
    }


    @Override
    public Flowable<List<FavouritesDatabase>> getAllFavourites() {
        return iFavouritesDao.getAllFavourites();
    }

    @Override
    public Flowable<FavouritesDatabase> getKey(String keyId) {
        return iFavouritesDao.getKey(keyId);
    }



    @Override
    public void insertFavourites(FavouritesDatabase... favouritesDatabases) {

        iFavouritesDao.insertFavourites(favouritesDatabases);
    }

    @Override
    public void updateFavourites(FavouritesDatabase... favouritesDatabases) {
        iFavouritesDao.updateFavourites(favouritesDatabases);

    }

    @Override
    public void deleteFavourites(FavouritesDatabase... favouritesDatabases) {
        iFavouritesDao.deleteFavourites(favouritesDatabases);

    }

    @Override
    public void deleteAllFavourites() {
        iFavouritesDao.deleteAllFavourites();

    }
}
