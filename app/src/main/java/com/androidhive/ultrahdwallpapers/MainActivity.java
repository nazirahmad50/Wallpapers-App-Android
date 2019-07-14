package com.androidhive.ultrahdwallpapers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.androidhive.ultrahdwallpapers.Common.Common;
import com.google.android.gms.ads.MobileAds;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, Common.BANNER_AD_ID);

        Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();


    }
}
