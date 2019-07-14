package com.androidhive.ultrahdwallpapers.Model;

public class Wallpapers {

    private String categoryId,imageLink, storageFile, name;
    private String userEmail,  userName, userImage;
    private long viewCount,downloadCount, recentCount;

    public Wallpapers() {
    }

    public Wallpapers(String categoryId, String imageLink,String userEmail, String storageFile, String name,String userName, String userImage) {
        this.categoryId = categoryId;
        this.imageLink = imageLink;
        this.name = name;
        this.userEmail = userEmail;
        this.storageFile = storageFile;
        this.userName = userName;
        this.userImage = userImage;


    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(String storageFile) {
        this.storageFile = storageFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public long getRecentCount() {
        return recentCount;
    }

    public void setRecentCount(long recentCount) {
        this.recentCount = recentCount;
    }
}
