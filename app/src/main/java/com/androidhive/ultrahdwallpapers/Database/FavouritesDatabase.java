package com.androidhive.ultrahdwallpapers.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "favourites", primaryKeys = {"imageLink","categoryId"})
public class FavouritesDatabase {


    @ColumnInfo(name = "imageLink")
    @NonNull
    private String imageLink;


    @ColumnInfo(name = "categoryId")
    @NonNull
    private String categoryId;

    @ColumnInfo(name = "key")
    private String key;

    public FavouritesDatabase() {
    }

    public FavouritesDatabase(@NonNull String imageLink, @NonNull String categoryId, String key) {
        this.imageLink = imageLink;
        this.categoryId = categoryId;
        this.key = key;
    }

    @NonNull
    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(@NonNull String imageLink) {
        this.imageLink = imageLink;
    }

    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
