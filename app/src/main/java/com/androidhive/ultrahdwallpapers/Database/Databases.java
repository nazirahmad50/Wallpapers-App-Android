package com.androidhive.ultrahdwallpapers.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.androidhive.ultrahdwallpapers.Database.Dao.IFavouritesDao;
import com.androidhive.ultrahdwallpapers.Database.Dao.IRecentsDao;

//5
@Database(entities = {RecentsDatabase.class, FavouritesDatabase.class}, version = 2) //@Database: Represent a database holder
public abstract class Databases extends RoomDatabase {

    private static final String DATABASE_NAME = "LiveWallpaper";

    public abstract IRecentsDao iRecentsLocalDatabase();

    public abstract IFavouritesDao iFavouritesDao();


    private static Databases databasesInstance;


    public static Databases getLocalDatabaseInstance(Context context){

        if (databasesInstance == null){

            databasesInstance = Room.databaseBuilder(context,Databases.class,DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return databasesInstance;
    }


}
