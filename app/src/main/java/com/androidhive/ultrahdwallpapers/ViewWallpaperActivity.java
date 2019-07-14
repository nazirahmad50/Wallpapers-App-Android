package com.androidhive.ultrahdwallpapers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.androidhive.ultrahdwallpapers.Common.Common;
import com.androidhive.ultrahdwallpapers.Database.Databases;
import com.androidhive.ultrahdwallpapers.Database.Repository.FavouritesRepositry;
import com.androidhive.ultrahdwallpapers.Database.Repository.RecentRepositery;
import com.androidhive.ultrahdwallpapers.Database.FavouritesDatabase;
import com.androidhive.ultrahdwallpapers.Database.LocalDatabase.FavouritesLocalDatabase;
import com.androidhive.ultrahdwallpapers.Database.LocalDatabase.RecentsLocalDatabase;
import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;
import com.androidhive.ultrahdwallpapers.Model.Favourites;
import com.androidhive.ultrahdwallpapers.Model.Wallpapers;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewWallpaperActivity extends AppCompatActivity {

    //Widgets
    private FloatingActionButton fabSetWallpaper;
    private FloatingActionButton fabDownload;
    private FloatingActionButton fabShare;
    private FloatingActionButton fabInfo;

    //Alert Dialog Widgets
    private TextView viewCount;
    private TextView download_Count;
    private TextView uploader_name;
    private ImageView uploader_image;


    //Room Database
    private CompositeDisposable compositeDisposable;
    private CompositeDisposable compositeDisposable2;
    private FavouritesRepositry favouritesRepositry;

    //Favourites
    private Favourites favourites;
    private boolean isFavourite = false;
    private boolean isFavouriteLocal = false;


    private String currentUserEmail = "";

    private DatabaseReference favouritesRef;


    //Key of Favourited Wallpaper
    private String favouriteKey = "";

    //Used to time progress dialog
    private long start = 0;
    private long diff = 0;

    private int adCounter = 0;

    //Favourite Menu item
    private MenuItem favMenuItem;

    //Set firebase counts to these variables
    private long viewCountTotal = 0;
    private long downloadCountTotal = 0;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallpaper);



        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Common.setInterstitialAd(this, 4);

        //Needed for Share Intent (FileUriExposedException)
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        //ToolBar
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        }


        //Firebase Reference
        favouritesRef = FirebaseDatabase.getInstance().getReference(Common.FAVOURITES);


        //Image View Wallpaper
        ImageView imgWallpaper = findViewById(R.id.img_thumbnail);

        Glide.with(ViewWallpaperActivity.this)
                .load(Common.SELECTED_BACKGROUND.getImageLink())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                .into(imgWallpaper);

        //Double tap to favourite/un-favourite wallpaper
        imgWallpaper.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {

            }

            @Override
            public void onDoubleClick(View view) {
                addToFavouritesMenuItem();
            }
        }));


        //Floating Buttons
        fabInfo = findViewById(R.id.fabInfo);
        fabSetWallpaper = findViewById(R.id.fabWallpaper);
        fabDownload = findViewById(R.id.fabDownload);
        fabShare = findViewById(R.id.fabShare);

        floatingBtnsClick();


        //Init RoomDatabse Recents
        Databases database = Databases.getLocalDatabaseInstance(this);

        compositeDisposable = new CompositeDisposable();
        RecentRepositery recentRepositery = RecentRepositery.getInstance(RecentsLocalDatabase.getInstance(database.iRecentsLocalDatabase()));

        //Init RoomDatabse Favourites
        compositeDisposable2 = new CompositeDisposable();
        favouritesRepositry = FavouritesRepositry.getInstance(FavouritesLocalDatabase.getInstance(database.iFavouritesDao()));


        //Update Recents
        if (recentRepositery.getKey(Common.SELECTED_BACKGROUND_KEY) != null) {

            addOrUpdateRecents(null, recentRepositery);
        }

        //Add To Recents
        addOrUpdateRecents(recentRepositery, null);


//        recentsCount();

        //Wallpaper View Count
        wallpaperViewCount();

        loadDownloadCount();

    }


//***********************************************************************************************AD Floating Buttons***********************************************************************************

    private void adFloatingBtns(final Intent intent, final String intentMessage, final Bitmap bitmap, final String fileName, final AlertDialog.Builder alertDialog) {
        final InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-9684265233745287/3786209063"); //TODO: Change it to real ID:
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {


            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                if (adCounter < 1) {
                    adCounter++;


                    if (intent != null) {
                        startActivity(Intent.createChooser(intent, intentMessage));
                    } else if (bitmap != null) {
                        MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), bitmap, fileName, "Ultra HD Wallpapers");
                        Toast.makeText(ViewWallpaperActivity.this, "Downloaded Successfully", Toast.LENGTH_SHORT).show();
                    }else if (alertDialog != null){
                        alertDialog.show();
                    }


                } else {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();

                        adCounter = 0;

                    }
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();

                if (intent != null) {
                    startActivity(Intent.createChooser(intent, intentMessage));
                } else if (bitmap != null) {
                    MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), bitmap, fileName, "Ultra HD Wallpapers");
                    Toast.makeText(ViewWallpaperActivity.this, "Downloaded Successfully", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }


    //**********************************************************************************Floating Buttons/Progress Dialog************************************************************************

    private void floatingBtnsClick() {


        //Set Wallpaper Button
        fabSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                start = System.currentTimeMillis();

                Glide.with(ViewWallpaperActivity.this)
                        .asBitmap()
                        .load(Common.SELECTED_BACKGROUND.getImageLink())
                        .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setDataAndType(getlocalBitmapUri(resource), "image/*");
                        intent.putExtra("mimeType", "image/*");
                        adFloatingBtns(intent, "Set As:", null, "", null);
                        diff = System.currentTimeMillis() - start;
                        return false;
                    }
                }).submit();



                progressDialog(diff);


            }
        });

        fabInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewWallpaperActivity.this);

                View view = getLayoutInflater().inflate(R.layout.layout_view_wallpaper_info, null);
                alertDialogBuilder.setView(view);

                viewCount = view.findViewById(R.id.view_count);
                download_Count = view.findViewById(R.id.download_count);
                uploader_name = view.findViewById(R.id.uploader_name);
                uploader_image = view.findViewById(R.id.uploader_image);

                viewCount.setText(String.valueOf(viewCountTotal));
                download_Count.setText(String.valueOf(downloadCountTotal));


                loadUplaoderDetails();

                adFloatingBtns(null, "", null, "", alertDialogBuilder);


            }
        });

        //Download  Button
        fabDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Runtime Permissions for the downloading wallpaper to gallery
                runTimePermission();

            }
        });


        // Share Button
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                start = System.currentTimeMillis();


                Glide.with(ViewWallpaperActivity.this)
                        .asBitmap()
                        .load(Common.SELECTED_BACKGROUND.getImageLink())
                        .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getlocalBitmapUri(resource));
                        intent.putExtra(Intent.EXTRA_TEXT, "Most Unique and Beautiful collection of Ultra HD Wallpapers. Give you Device a New Elegant Look. Available on Play Store: https://goo.gl/yVHWkn");
                        adFloatingBtns(intent, "Share Via:", null, "", null);


                        diff = System.currentTimeMillis() - start;
                        return false;
                    }
                }).submit();


                progressDialog(diff);


            }
        });


    }

    private void progressDialog(long timeDiff) {

        final ProgressDialog dialog = new ProgressDialog(ViewWallpaperActivity.this);
        dialog.setMessage("");
        dialog.setIndeterminate(false);
        dialog.show();
        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                dialog.dismiss();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, timeDiff);

    }


    //**********************************************************************************Check If Wallpaper is Favourite************************************************************************

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_view_wallpaper_menu_item, menu);
        favMenuItem = menu.findItem(R.id.action_favorite);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //Store Current User
            currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

            //check if it favourite already
            isFirebaseFavourite();

            //When user clicks favourtie button from Favourite activity
            //Then 'IS_FAVOURITE' becomes false
            if (!Common.IS_FAVOURITES) {


                favMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                isFavourite = false;
            }

        } else {

            isFavouritesDB();
        }

        return super.onPrepareOptionsMenu(menu);
    }


    //**********************************************************************Add/Remove Favourites************************************************************************
    //*************************************************************Close activity after back Arrow pressed************************************************************************

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_favorite:

                addToFavouritesMenuItem();
                return true;
        }


        return false;
    }

    private void addToFavouritesMenuItem() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {//Use Room Database Favourites
            if (!isFavouriteLocal) {

                addToFavouritesDB();
                isFavouriteLocal = true;
                favMenuItem.setIcon(R.drawable.ic_favorite_red_24dp);
            } else {

                removeFavouritesDB();
                favMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                isFavouriteLocal = false;

            }
        } else { //Use Firebase Favourites

            if (!isFavourite) {

                addToFirebaseFavourites();

            } else {

                removeFirebaseFavourites();

            }

        }

    }

    //*************************************************************Add/Update Recents Room Database************************************************************************

    private void addOrUpdateRecents(final RecentRepositery addRecentsRepositery, final RecentRepositery updateRecentsRepositery) {

        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {


                RecentsDatabase recentsDatabase = new RecentsDatabase(
                        Common.SELECTED_BACKGROUND.getImageLink(),
                        Common.SELECTED_BACKGROUND.getCategoryId(),
                        String.valueOf(System.currentTimeMillis()),
                        Common.SELECTED_BACKGROUND_KEY
                );
                if (addRecentsRepositery != null) {
                    addRecentsRepositery.insertRecents(recentsDatabase);
                } else {
                    updateRecentsRepositery.updateRecents(recentsDatabase);
                }

                e.onComplete();

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                        Log.e("Error", throwable.getMessage());

                    }
                });

        compositeDisposable.add(disposable);

    }


    //*************************************************************Room Database Favourites************************************************************************

    private void addToFavouritesDB() {

        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {

                FavouritesDatabase favouritesDatabase = new FavouritesDatabase(
                        Common.SELECTED_BACKGROUND.getImageLink(),
                        Common.SELECTED_BACKGROUND.getCategoryId(),
                        Common.SELECTED_BACKGROUND_KEY
                );
                favouritesRepositry.insertFavourites(favouritesDatabase);
                e.onComplete();

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                        Log.e("Error", throwable.getMessage());

                    }

                });

        compositeDisposable2.add(disposable);

    }

    private void isFavouritesDB() {

        Disposable disposable = favouritesRepositry.getKey(Common.SELECTED_BACKGROUND_KEY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<FavouritesDatabase>() {
                    @Override
                    public void accept(FavouritesDatabase favouritesDatabase) {


                        if (!favouritesDatabase.getKey().isEmpty()) {
                            favouriteKey = favouritesDatabase.getKey();
                            favMenuItem.setIcon(R.drawable.ic_favorite_red_24dp);
                            isFavouriteLocal = true;
                        } else {
                            favMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                            isFavouriteLocal = false;

                        }
                    }
                });
        compositeDisposable2.add(disposable);

    }

    private void removeFavouritesDB() {

        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {

                FavouritesDatabase favouritesDatabase = new FavouritesDatabase(
                        Common.SELECTED_BACKGROUND.getImageLink(),
                        Common.SELECTED_BACKGROUND.getCategoryId(),
                        favouriteKey
                );
                favouritesRepositry.deleteFavourites(favouritesDatabase);
                e.onComplete();

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                        Log.e("Error", throwable.getMessage());

                    }

                });
        compositeDisposable2.add(disposable);

    }


    //*************************************************************Firebase Favourites************************************************************************

    private void isFirebaseFavourite() {

        favouritesRef.orderByChild("userEmail").equalTo(currentUserEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    favourites = postSnapshot.getValue(Favourites.class);


                    if (Objects.requireNonNull(favourites).getCategoryId().equals(Common.SELECTED_BACKGROUND_KEY)) {

                        favMenuItem.setIcon(R.drawable.ic_favorite_red_24dp);
                        isFavourite = true;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addToFirebaseFavourites() {

        favouritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!Objects.requireNonNull(dataSnapshot.getKey()).isEmpty()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {


                        favourites = postSnapshot.getValue(Favourites.class);


                        Objects.requireNonNull(favourites).setCategoryId(Common.SELECTED_BACKGROUND_KEY);
                        favourites.setImageLink(Common.SELECTED_BACKGROUND.getImageLink());
                        favourites.setUserEmail(currentUserEmail);


                    }
                    favouritesRef
                            .child(Common.SELECTED_BACKGROUND_KEY)
                            .setValue(favourites);


                    favMenuItem.setIcon(R.drawable.ic_favorite_red_24dp);
                    isFavourite = true;


                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeFirebaseFavourites() {

        Query q = favouritesRef.orderByChild("userEmail").equalTo(currentUserEmail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    favourites = postSnapshot.getValue(Favourites.class);

                    if (Objects.requireNonNull(favourites).getCategoryId().equals(Common.SELECTED_BACKGROUND_KEY)) {

                        favouritesRef.child(Objects.requireNonNull(postSnapshot.getKey())).removeValue();
                        isFavourite = false;
                    }

                }
                favMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    //*************************************************************Used for Share Intent************************************************************************

    private Uri getlocalBitmapUri(Bitmap bmp) {


        Uri bmpUri = null;

        try {
            File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Share Image" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmpUri;
    }


    //*************************************************************Wallpaper/Download Count************************************************************************


    private void wallpaperViewCount() {

        FirebaseDatabase.getInstance()
                .getReference(Common.WALLPAPER_LIST)
                .child(Common.SELECTED_BACKGROUND_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("viewCount")) {

                            Wallpapers wallpapers = dataSnapshot.getValue(Wallpapers.class);

                            viewCountTotal = Objects.requireNonNull(wallpapers).getViewCount() + 1;

//                            viewCount.setText(String.valueOf(viewCountTotal));

                            //Update
                            Map<String, Object> update_View = new HashMap<>();
                            update_View.put("viewCount", viewCountTotal);


                            FirebaseDatabase.getInstance()
                                    .getReference(Common.WALLPAPER_LIST)
                                    .child(Common.SELECTED_BACKGROUND_KEY)
                                    .updateChildren(update_View)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ViewWallpaperActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        } else { // if there is no viewCount in firebase then set default to 1

                            //Update
                            Map<String, Object> update_View = new HashMap<>();
                            update_View.put("viewCount", 1);

//                            viewCount.setText(String.valueOf(1));
                            viewCountTotal = 1;


                            FirebaseDatabase.getInstance()
                                    .getReference(Common.WALLPAPER_LIST)
                                    .child(Common.SELECTED_BACKGROUND_KEY)
                                    .updateChildren(update_View)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ViewWallpaperActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void loadDownloadCount() {

        FirebaseDatabase.getInstance().getReference(Common.WALLPAPER_LIST)
                .child(Common.SELECTED_BACKGROUND_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("downloadCount")) {

                            Wallpapers wallpapers = dataSnapshot.getValue(Wallpapers.class);

                            downloadCountTotal = Objects.requireNonNull(wallpapers).getDownloadCount();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //***********************************************************************************************Load Uploader Details***********************************************************************************

    private void loadUplaoderDetails() {

        FirebaseDatabase.getInstance().getReference(Common.WALLPAPER_LIST).child(Common.SELECTED_BACKGROUND_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Wallpapers wallpapers = dataSnapshot.getValue(Wallpapers.class);


                if (Objects.requireNonNull(wallpapers).getUserName() != null && wallpapers.getUserEmail() != null) {

                    uploader_name.setText(wallpapers.getUserName());

                    Glide.with(ViewWallpaperActivity.this).load(wallpapers.getUserImage()).into(uploader_image);

                } else {

                    uploader_name.setText(R.string.app_name);
                    uploader_image.setImageResource(R.drawable.splash_screen_icon);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //*************************************************************Runtime Permissions For Downloading************************************************************************
    private void runTimePermission() {

        //Check if permission has already been allowed by the user
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            //If permission not allowed then request for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, Common.PERMISSION_REQUEST_CODE);
            }

            //Permission already allowed
        } else {

            downloadImage();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Common.PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    downloadImage();


                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //*************************************************************Download Wallpaper/Set Wallpaper Count************************************************************************


    private void downloadImage() {

        start = System.currentTimeMillis();

        final String fileName = UUID.randomUUID().toString() + ".jpg";

        Glide.with(ViewWallpaperActivity.this)
                .asBitmap()
                .load(Common.SELECTED_BACKGROUND.getImageLink())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {

                        if (getApplication().getContentResolver() != null) {

                            adFloatingBtns(null, "", resource, fileName, null);
                            diff = System.currentTimeMillis() - start;

                        }
                        return false;
                    }
                }).submit();


        progressDialog(diff);

        setDownloadCount();


    }


    private void setDownloadCount() {

        FirebaseDatabase.getInstance()
                .getReference(Common.WALLPAPER_LIST)
                .child(Common.SELECTED_BACKGROUND_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("downloadCount")) {

                            Wallpapers wallpapers = dataSnapshot.getValue(Wallpapers.class);

                            downloadCountTotal = Objects.requireNonNull(wallpapers).getDownloadCount() + 1;


                            //Update
                            Map<String, Object> update_View = new HashMap<>();
                            update_View.put("downloadCount", downloadCountTotal);


                            FirebaseDatabase.getInstance()
                                    .getReference(Common.WALLPAPER_LIST)
                                    .child(Common.SELECTED_BACKGROUND_KEY)
                                    .updateChildren(update_View)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ViewWallpaperActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else { // if there is no viewCount in firebase then set default to 1

                            //Update
                            Map<String, Object> update_View = new HashMap<>();
                            update_View.put("downloadCount", 1);

                            downloadCountTotal = 1;


                            FirebaseDatabase.getInstance()
                                    .getReference(Common.WALLPAPER_LIST)
                                    .child(Common.SELECTED_BACKGROUND_KEY)
                                    .updateChildren(update_View)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ViewWallpaperActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onDestroy() {
//        Picasso.get().cancelRequest(target);
        compositeDisposable.clear();
        compositeDisposable2.clear();
        super.onDestroy();
    }


}
