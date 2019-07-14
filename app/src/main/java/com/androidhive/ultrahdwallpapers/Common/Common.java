package com.androidhive.ultrahdwallpapers.Common;

import android.content.Context;
import android.net.ConnectivityManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.androidhive.ultrahdwallpapers.Model.Wallpapers;

import java.util.Objects;


public class Common {


    //**************************************************************Firebase database Refrenses********************************************************

    public static final String CATEGORY_BACKGROUND = "CategoryBackground";
    public static final String WALLPAPER_LIST = "Background";
    public static final String FAVOURITES = "Favourites";



    //***************************************************************Selected Category: Name/Id*******************************************************

    public static String CATEGORY_ID_SELECTED;

    //This is shown on 'ListWallpaperActivity' ToolBar
    public static String CATEGORY_NAME_SELECTED;


    //**************************************************************Selected Wallpaper: Object/key****************************************************

    public static Wallpapers SELECTED_BACKGROUND = new Wallpapers();

    //The Selected Wallpaper key
    public static String SELECTED_BACKGROUND_KEY;

    //*************************************************************Clicked Favourite icon in Favourite Activity*************************************

    //When Use clicks on Favourite icon in Favourite Activity
    //Then this becomes false otherwise its true
    public static boolean IS_FAVOURITES = true;


    //**************************************************************Interstitial/Banner Ad********************************************************
    public static final String BANNER_AD_ID = "ca-app-pub-9684265233745287~6389926339";

    private static int AD_COUNTER = 0;

    public static void setInterstitialAd(Context context, final int adCount){

        final InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId("ca-app-pub-9684265233745287/3786209063"); //TODO: Change it to real ID
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                if (Common.AD_COUNTER < adCount){
                    Common.AD_COUNTER++;

                }else{
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();

                        Common.AD_COUNTER = 0;

                    }
                }
            }

        });
    }




    //***********************************************************************Request Codes******************************************************

    //Downloading Image request Code
    public static final int PERMISSION_REQUEST_CODE = 1000;

    //Signing In with Google
    public static final int SIGN_IN_REQUEST_CODE = 1001 ;
    public static final String REQUEST_TOKEN_ID = "240984518171-1k6a671s9cmnettt1p7f41jbu7n5adft.apps.googleusercontent.com";

    //Getting image from Gallery
    public static final int PICK_IMAGE_REQUEST = 1002;




    //***********************************************************************Check Network Connection******************************************************

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return Objects.requireNonNull(cm).getActiveNetworkInfo() != null;
    }








}
