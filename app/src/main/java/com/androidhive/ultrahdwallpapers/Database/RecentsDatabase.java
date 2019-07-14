package com.androidhive.ultrahdwallpapers.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

//1.
//This creates a table with columnes (Its like the model class of database)
@Entity(tableName = "recents", primaryKeys = {"imageLink","categoryId"})
public class RecentsDatabase {


    @ColumnInfo(name = "imageLink")
    @NonNull
    private String imageLink;

    @ColumnInfo(name = "categoryId")
    @NonNull
    private String categoryId;

    @ColumnInfo(name = "saveTime")
    private String saveTime;

    @ColumnInfo(name = "key")
    private String key;

    public RecentsDatabase(@NonNull String imageLink, @NonNull String categoryId, String saveTime, String key) {
        this.imageLink = imageLink;
        this.categoryId = categoryId;
        this.saveTime = saveTime;
        this.key = key;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(String saveTime) {
        this.saveTime = saveTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
