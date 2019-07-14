package com.androidhive.ultrahdwallpapers.Database.Dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.androidhive.ultrahdwallpapers.Database.FavouritesDatabase;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface IFavouritesDao {

    @Query("SELECT * FROM favourites")
    Flowable<List<FavouritesDatabase>> getAllFavourites();

    @Query("SELECT * FROM favourites WHERE `key`=:keyId")
    Flowable<FavouritesDatabase> getKey(String keyId);

    @Insert()
    void insertFavourites(FavouritesDatabase...favouritesDatabases);

    @Update
    void updateFavourites(FavouritesDatabase...favouritesDatabases);

    @Delete
    void deleteFavourites(FavouritesDatabase...favouritesDatabases);

    @Query("DELETE FROM favourites")
    void deleteAllFavourites();
}
