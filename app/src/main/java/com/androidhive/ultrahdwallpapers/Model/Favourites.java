package com.androidhive.ultrahdwallpapers.Model;

public class Favourites {

    private String categoryId,imageLink,userEmail;



    public Favourites() {
    }

    public Favourites(String categoryId, String imageLink, String userEmail) {
        this.categoryId = categoryId;
        this.imageLink = imageLink;
        this.userEmail = userEmail;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }




}
