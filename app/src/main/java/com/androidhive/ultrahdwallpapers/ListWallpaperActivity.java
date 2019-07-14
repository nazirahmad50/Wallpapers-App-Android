package com.androidhive.ultrahdwallpapers;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.androidhive.ultrahdwallpapers.Common.Common;
import com.androidhive.ultrahdwallpapers.Interface.ItemClickListener;
import com.androidhive.ultrahdwallpapers.Model.Wallpapers;
import com.androidhive.ultrahdwallpapers.ViewHolder.ListWallpaperViewHolder;

public class ListWallpaperActivity extends AppCompatActivity {

    private DatabaseReference wallpaperRef;

    //Firebase Adapter
    private FirebaseRecyclerAdapter<Wallpapers, ListWallpaperViewHolder> adapter;

    //RecyclerView
    private RecyclerView recyclerView;

    //Swipe Refresh layout
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_wallpaper);


        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Common.setInterstitialAd(this, 4);

        //Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        wallpaperRef = firebaseDatabase.getReference(Common.WALLPAPER_LIST);

        //ToolBar: Display Category Name
       Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Common.CATEGORY_NAME_SELECTED);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        //Enables the back arrow on top of the toolbar in order to go back by clicking on the arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //RecyclerView
        recyclerView = findViewById(R.id.list_recyclerView);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);


        //Swipe Refresh Layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout();



        loadWallpaperList();




    }


    //********************************************Check Internet Connection****************************

    private void checkNetworkConnection(){
        //Check Internet Connection
        if (!Common.isNetworkConnected(ListWallpaperActivity.this)){

           Snackbar.make(swipeRefreshLayout, "Please Check Your Internet Connection", Snackbar.LENGTH_SHORT);
            recyclerView.setVisibility(View.GONE);
        }
        else if (Common.isNetworkConnected(ListWallpaperActivity.this)){
            recyclerView.setVisibility(View.VISIBLE);
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

                checkNetworkConnection();

                loadWallpaperList();
                swipeRefreshLayout.setRefreshing(false);


            }
        });

        //Default load for the first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                checkNetworkConnection();

                loadWallpaperList();
                swipeRefreshLayout.setRefreshing(false);


            }
        });


    }

    //************************************************************Load Wallpaper List************************************************************************

    private void loadWallpaperList() {

        //Check if 'wallpaperRef' field 'categoryId' is equal to 'CategoryBackground' id that is selected
        Query query = wallpaperRef.orderByChild("categoryId").equalTo(Common.CATEGORY_ID_SELECTED);



        FirebaseRecyclerOptions<Wallpapers> options = new FirebaseRecyclerOptions.Builder<Wallpapers>()
                .setQuery(query, Wallpapers.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Wallpapers, ListWallpaperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ListWallpaperViewHolder viewholder, int position, @NonNull final Wallpapers model) {


                //Get List Wallpaper Images
                Glide.with(getApplicationContext())
                        .load(model.getImageLink())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                        .into(viewholder.wallpaper_image);




                viewholder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {

                        Intent intent = new Intent(ListWallpaperActivity.this, ViewWallpaperActivity.class);
                        Common.SELECTED_BACKGROUND = model;
                        Common.SELECTED_BACKGROUND_KEY = adapter.getRef(position).getKey();

                        startActivity(intent);

                    }
                });


            }

            @NonNull
            @Override
            public ListWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_list_wallpaper_item, parent, false);

                return new ListWallpaperViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);


    }




    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (adapter != null) {
            adapter.startListening();
        }

    }

    //*************************************************************Close activity after back Arrow pressed************************************************************************

    //Closes Activity after taping back Arrow at top of toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish(); ////Closes Activity after taping back Arrow at top of toolbar
        }

        return super.onOptionsItemSelected(item);
    }

}
