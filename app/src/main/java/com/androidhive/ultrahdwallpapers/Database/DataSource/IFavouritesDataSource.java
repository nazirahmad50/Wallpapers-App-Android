package com.androidhive.ultrahdwallpapers.Database.DataSource;

import com.androidhive.ultrahdwallpapers.Database.FavouritesDatabase;

import java.util.List;

import io.reactivex.Flowable;

public interface IFavouritesDataSource {

    Flowable<List<FavouritesDatabase>> getAllFavourites();
    Flowable<FavouritesDatabase> getKey(String keyId);


    void insertFavourites(FavouritesDatabase...favouritesDatabases);
    void updateFavourites(FavouritesDatabase...favouritesDatabases);
    void deleteFavourites(FavouritesDatabase...favouritesDatabases);
    void deleteAllFavourites();
}
