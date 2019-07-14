package com.androidhive.ultrahdwallpapers.Database.Repository;

import com.androidhive.ultrahdwallpapers.Database.DataSource.IFavouritesDataSource;
import com.androidhive.ultrahdwallpapers.Database.FavouritesDatabase;

import java.util.List;

import io.reactivex.Flowable;

public class FavouritesRepositry implements IFavouritesDataSource {

    private final IFavouritesDataSource mLocalDataSource;
    private static FavouritesRepositry instance;

    private FavouritesRepositry(IFavouritesDataSource mLocalDataSource) {
        this.mLocalDataSource = mLocalDataSource;
    }


    public static FavouritesRepositry getInstance(IFavouritesDataSource mLocalDataSource){

        if (instance == null){

            instance = new FavouritesRepositry(mLocalDataSource);
        }
        return instance;
    }

    @Override
    public Flowable<List<FavouritesDatabase>> getAllFavourites() {
        return mLocalDataSource.getAllFavourites();
    }

    @Override
    public Flowable<FavouritesDatabase> getKey(String keyId) {
        return mLocalDataSource.getKey(keyId);
    }


    @Override
    public void insertFavourites(FavouritesDatabase... favouritesDatabases) {

        mLocalDataSource.insertFavourites(favouritesDatabases);
    }

    @Override
    public void updateFavourites(FavouritesDatabase... favouritesDatabases) {
        mLocalDataSource.updateFavourites(favouritesDatabases);

    }

    @Override
    public void deleteFavourites(FavouritesDatabase... favouritesDatabases) {
        mLocalDataSource.deleteFavourites(favouritesDatabases);

    }

    @Override
    public void deleteAllFavourites() {

        mLocalDataSource.deleteAllFavourites();
    }
}
