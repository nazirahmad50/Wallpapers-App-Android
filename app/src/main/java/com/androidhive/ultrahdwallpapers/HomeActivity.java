package com.androidhive.ultrahdwallpapers;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.androidhive.ultrahdwallpapers.Adapter.MyFragmentAdapter;
import com.androidhive.ultrahdwallpapers.Common.Common;

import java.io.File;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //Used for user sign in
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageView profile_image;

    //Signing in Widgets
    private TextView txtUserName;
    private SignInButton signIn;

    //Used for Signing with Google account
    private GoogleApiClient mGoogleApiClient;

    //Sign Out Menu item
    private MenuItem nav_sign_out_item;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);


        //Drawer Layout
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Check Internet Connection
        if (!Common.isNetworkConnected(this)){
            Snackbar.make(drawer, "Please Check Your Internet Connection", Snackbar.LENGTH_SHORT).show();

        }

        //Navigation View
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Navigation HeaderView Widgets
        signIn = navigationView.getHeaderView(0).findViewById(R.id.sign_in);
        txtUserName = navigationView.getHeaderView(0).findViewById(R.id.user_email);
        profile_image = navigationView.getHeaderView(0).findViewById(R.id.profile_image);


        //1.Set the adapter (MyFragmentsAdapter) to the viewPager
        ViewPager viewPager = findViewById(R.id.viewPager);
        MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);

        //2.TabLayout
        //When swiping tabs the viewpager changes
        TabLayout tableLayout = findViewById(R.id.tabLayout);
        tableLayout.setupWithViewPager(viewPager);



        //Default Google Sign In Requiremetns
        setUpGoogleSignIn();

        //Sign In Button
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(intent, Common.SIGN_IN_REQUEST_CODE);

            }
        });


        checkIfUserSignedIn();




    }



    private void checkIfUserSignedIn(){

        //Disable Sign out nav item if there is no user signed in
        final Menu menuNav = navigationView.getMenu();
        nav_sign_out_item = menuNav.findItem(R.id.nav_sign_out);

        //Check if User Signed In
        if (FirebaseAuth.getInstance().getCurrentUser() == null) { //If user Not Signed In

            nav_sign_out_item.setVisible(false);
            txtUserName.setVisibility(View.GONE);


        } else { //If User Signed In

            String userDisplayName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();

            //Show Users display name
            txtUserName.setVisibility(View.VISIBLE);
            txtUserName.setText(userDisplayName);

            //Get Users profile Pic
            Glide.with(HomeActivity.this).load(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl()).into(profile_image);


            signIn.setVisibility(View.GONE);

            Snackbar.make(drawer, "Welcome Back " + userDisplayName,
                    Snackbar.LENGTH_LONG).show();

            nav_sign_out_item.setVisible(true);

        }

            //TODO: Future- Make Favourites navigation item heart red if there is Favourites in Favourites Activity



    }


    //*********************************Default Google Sign Requirements**********************

    private void setUpGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Common.REQUEST_TOKEN_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(HomeActivity.this, "Connection Failed: " + connectionResult, Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }



    //*********************************Add New User To Firebase**********************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Common.SIGN_IN_REQUEST_CODE) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {

                GoogleSignInAccount userAcount = result.getSignInAccount();


                txtUserName.setVisibility(View.VISIBLE);
                txtUserName.setText(Objects.requireNonNull(userAcount).getDisplayName());

                Glide.with(HomeActivity.this).load(userAcount.getPhotoUrl()).into(profile_image);

                signIn.setVisibility(View.GONE);


                nav_sign_out_item.setVisible(true);


                addUserToFirebase(userAcount);



            }


        }
    }


    private void addUserToFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Toast.makeText(HomeActivity.this, "Signed In Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //*********************************Remove Cache Directory**********************

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        switch (id) {
            case R.id.nav_view_uploads:

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                    Intent intent = new Intent(HomeActivity.this, ViewUploadsActivity.class);
                    startActivity(intent);

                } else {
                    Snackbar.make(drawer, "Please Sign In", Snackbar.LENGTH_SHORT).show();

                }
                break;

            case R.id.nav_upload:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                    Intent intent = new Intent(HomeActivity.this, UploadWallpaperActivity.class);
                    startActivity(intent);
                } else {
                    Snackbar.make(drawer, "Please Sign In", Snackbar.LENGTH_SHORT).show();

                }
                break;

            case R.id.nav_favourites:
                Intent intent = new Intent(HomeActivity.this, FavouritesActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_clear_cache:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                alertDialog.setMessage("This will clear all images saved on your device's cache, but frees up space. Loading Images again will be slower");

                alertDialog.setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            File dir = HomeActivity.this.getCacheDir();
                            deleteDir(dir);
                            Toast.makeText(HomeActivity.this, "Cache Cleared Successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("Clear Cache Error",""+e.getMessage());
                        }
                    }
                });
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                alertDialog.show();


            break;

            case R.id.nav_sign_out:

                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(HomeActivity.this, "Signed Out Successfully", Toast.LENGTH_SHORT).show();

                        txtUserName.setVisibility(View.GONE);

                        profile_image.setImageResource(R.drawable.ic_person_white_24dp);

                        signIn.setVisibility(View.VISIBLE);
                        nav_sign_out_item.setVisible(false);


                    }
                });

                break;

            case R.id.nav_privacy_policy:
                Intent privacy = new Intent();
                privacy.setAction(Intent.ACTION_VIEW);
                privacy.addCategory(Intent.CATEGORY_BROWSABLE);
                privacy.setData(Uri.parse("https://sites.google.com/view/ultrahdwallpapers/home"));
                startActivity(privacy);

                break;
        }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
