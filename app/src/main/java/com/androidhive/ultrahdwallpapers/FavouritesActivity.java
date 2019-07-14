package com.androidhive.ultrahdwallpapers;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.androidhive.ultrahdwallpapers.Adapter.MyFavouritesAdapter;
import com.androidhive.ultrahdwallpapers.Common.Common;
import com.androidhive.ultrahdwallpapers.Database.Databases;
import com.androidhive.ultrahdwallpapers.Database.Repository.FavouritesRepositry;
import com.androidhive.ultrahdwallpapers.Database.FavouritesDatabase;
import com.androidhive.ultrahdwallpapers.Database.LocalDatabase.FavouritesLocalDatabase;
import com.androidhive.ultrahdwallpapers.Interface.ItemClickListener;
import com.androidhive.ultrahdwallpapers.Model.Favourites;
import com.androidhive.ultrahdwallpapers.ViewHolder.ListWallpaperViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FavouritesActivity extends AppCompatActivity {

    //ReyclerView
    private RecyclerView recyclerView;

    private DatabaseReference favouritesRef;

    //Firebase Adapter
    private FirebaseRecyclerAdapter<Favourites, ListWallpaperViewHolder> adapterFirebase;

    //RecycleView Adapter
    //We created seperate adapter class because we are not saving anything to firebase
    private MyFavouritesAdapter adapterDB;

    //List Used for Room DB
    private List<FavouritesDatabase> favouritesDatabasesList;

    //Room Database
    private CompositeDisposable compositeDisposable;
    private FavouritesRepositry favouritesRepositry;

    //Root layout for Snackbar
    private RelativeLayout root_layout;

    //Swipe Refresh layout
    private SwipeRefreshLayout swipeRefreshLayout;

    //Empty list image view
    private ImageView default_image;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);



        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //Interstitial Ad
         Common.setInterstitialAd(this, 1);


        //ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Favourites");
        setSupportActionBar(toolbar);

        //Enables the back arrow on top of the toolbar in order to go back by clicking on the arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        //Firebase Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        favouritesRef = firebaseDatabase.getReference(Common.FAVOURITES);

        //Empty List ImageView
        default_image = findViewById(R.id.default_image);

        //Used for Snackbar
        root_layout = findViewById(R.id.root_layout);

        //RecyclerView
        recyclerView = findViewById(R.id.favourites_recyclerView);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        //Recent database list
        favouritesDatabasesList = new ArrayList<>();

        //Init RoomDatabse Favourites
        compositeDisposable = new CompositeDisposable();
        Databases database = Databases.getLocalDatabaseInstance(this);
        favouritesRepositry = FavouritesRepositry.getInstance(FavouritesLocalDatabase.getInstance(database.iFavouritesDao()));


        //Adapter Room DB
        adapterDB = new MyFavouritesAdapter(FavouritesActivity.this, favouritesDatabasesList, favouritesRepositry, new CompositeDisposable());

        //Swipe Refresh Layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) { //Load Firebase Favourites

            loadFavourites();


        } else { //Load Room DB Favourites

            recyclerView.setAdapter(adapterDB);
            loadAllFavouritesDB();

        }






    }

    //********************************************Check Internet Connection****************************

    private void checkNetworkConnection(){
        //Check Internet Connection
        if (!Common.isNetworkConnected(FavouritesActivity.this)){
            Snackbar.make(root_layout, "Please Check Your Internet Connection", Snackbar.LENGTH_SHORT).show();
            recyclerView.setVisibility(View.GONE);
        }
        else if (Common.isNetworkConnected(FavouritesActivity.this)){
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    //********************************************Empty List****************************

    private void emptyList(int adapterCount){

        if (adapterCount == 0){

            default_image.setImageResource(R.drawable.ic_favorite_border_white_24dp);

        }else{
            default_image.setImageDrawable(null);



        }

    }

    //********************************************Refresh Layout****************************

    private void swipeRefreshLayout() {

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_blue_bright);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if (FirebaseAuth.getInstance().getCurrentUser() != null) { //User Signed In


                    checkNetworkConnection();

                    emptyList(adapterFirebase.getItemCount());

                    loadFavourites();


                } else { //User not Signed In

                    emptyList(adapterDB.getItemCount());

                    loadAllFavouritesDB();
                }

                swipeRefreshLayout.setRefreshing(false);
            }


        });

        //Default load for the first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (FirebaseAuth.getInstance().getCurrentUser() != null){//User Signed In

                    checkNetworkConnection();

                    emptyList(adapterFirebase.getItemCount());

                    loadFavourites();

                }else{//User not Signed In

                    emptyList(adapterDB.getItemCount());
                    loadAllFavouritesDB();
                }
                swipeRefreshLayout.setRefreshing(false);


            }
        });


    }

    //************************************************************Load Firebase Favourites************************************************************************

    private void loadFavourites() {

        //Query for all items that have the current users email equal to favourites nodes child "userEmail"
        Query query = favouritesRef.orderByChild("userEmail").equalTo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        FirebaseRecyclerOptions<Favourites> options = new FirebaseRecyclerOptions.Builder<Favourites>()
                .setQuery(query, Favourites.class)
                .build();

        adapterFirebase = new FirebaseRecyclerAdapter<Favourites, ListWallpaperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ListWallpaperViewHolder viewholder,  int position, @NonNull final Favourites model) {

                //Load Images
                Glide.with(FavouritesActivity.this)
                        .load(model.getImageLink())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                        .into(viewholder.wallpaper_image);


                //Remove From Favourites
                viewholder.remove_fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            favouritesRef.child(Objects.requireNonNull(adapterFirebase.getRef(viewholder.getAdapterPosition()).getKey())).removeValue();


                        }catch (IndexOutOfBoundsException e){

                            swipeRefreshLayout();


                        }


                        //This becomes false so in the ViewWallpaper the favourite button icon changes to unfavourite
                        Common.IS_FAVOURITES = false;
                    }
                });



                viewholder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent intent = new Intent(FavouritesActivity.this, ViewWallpaperActivity.class);

                        Common.SELECTED_BACKGROUND.setImageLink(model.getImageLink());
                        Common.SELECTED_BACKGROUND.setCategoryId(model.getCategoryId());

                        Common.SELECTED_BACKGROUND_KEY = adapterFirebase.getRef(position).getKey();
                        startActivity(intent);
                    }
                });
            }



            @NonNull
            @Override
            public ListWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_favourites_item, parent, false);


                emptyList(adapterFirebase.getItemCount());

                return new ListWallpaperViewHolder(itemView);
            }
        };
        adapterFirebase.startListening();
        recyclerView.setAdapter(adapterFirebase);


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (adapterFirebase != null) {
            adapterFirebase.startListening();
        }

    }


    //************************************************************Load Room Database Favourites************************************************************************

    private void loadAllFavouritesDB() {

        Disposable disposable = favouritesRepositry.getAllFavourites()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<FavouritesDatabase>>() {
                    @Override
                    public void accept(List<FavouritesDatabase> favouritesDatabases) {

                        favouritesDatabasesList.clear();
                        favouritesDatabasesList.addAll(favouritesDatabases);
                        adapterDB.notifyDataSetChanged();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                        Log.e("Error", throwable.getMessage());
                        Crashlytics.logException(new Exception(throwable.getMessage()));



                    }
                });
        compositeDisposable.add(disposable);
    }


    //*************************************************************Close activity after back Arrow pressed************************************************************************

    //Closes Activity after taping back Arrow at top of toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

          finish(); //Close activity when clicked back Arrow at top of toolbar
        }

        return super.onOptionsItemSelected(item);
    }



}
