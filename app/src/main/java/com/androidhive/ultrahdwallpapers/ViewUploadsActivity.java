package com.androidhive.ultrahdwallpapers;

import android.app.ProgressDialog;
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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.androidhive.ultrahdwallpapers.Common.Common;
import com.androidhive.ultrahdwallpapers.Database.Repository.RecentRepositery;
import com.androidhive.ultrahdwallpapers.Database.Databases;
import com.androidhive.ultrahdwallpapers.Database.LocalDatabase.RecentsLocalDatabase;
import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;
import com.androidhive.ultrahdwallpapers.Interface.ItemClickListener;
import com.androidhive.ultrahdwallpapers.Model.Wallpapers;
import com.androidhive.ultrahdwallpapers.ViewHolder.ListWallpaperViewHolder;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewUploadsActivity extends AppCompatActivity {

    //RecyclerView
    private RecyclerView recyclerView;

    private DatabaseReference viewUploadsRef;

    //RecyclerView Adapter
    private FirebaseRecyclerAdapter<Wallpapers, ListWallpaperViewHolder> adapter;

    private Wallpapers wallpapers;

    private CompositeDisposable compositeDisposable;
    private RecentRepositery recentRepositery;

    private RelativeLayout root_layout;

    //Swipe Refresh layout
    private SwipeRefreshLayout swipeRefreshLayout;

    private ImageView default_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_uploads);



        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Common.setInterstitialAd(this, 1);


        //Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        viewUploadsRef = firebaseDatabase.getReference(Common.WALLPAPER_LIST);

        //ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("View Uploads");
        setSupportActionBar(toolbar);

        //Enables the back arrow on top of the toolbar in order to go back by clicking on the arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //ReyclerView
        recyclerView = findViewById(R.id.view_uploads_recyclerView);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(ViewUploadsActivity.this,2);

        //Because Firebase returns ascending sort list so we need to reverse recyclerView to show largest item first in the list
        recyclerView.setLayoutManager(gridLayoutManager);

        default_image = findViewById(R.id.default_image);

        root_layout = findViewById(R.id.root_layout);

        //Init RoomDatabse
        compositeDisposable = new CompositeDisposable();
        Databases database = Databases.getLocalDatabaseInstance(this);
        recentRepositery = RecentRepositery.getInstance(RecentsLocalDatabase.getInstance(database.iRecentsLocalDatabase()));

        //Swipe Refresh Layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout();


        loadUploads();
    }

    private void checkNetworkConnection(){
        //Check Internet Connection
        if (!Common.isNetworkConnected(ViewUploadsActivity.this)){
            Snackbar.make(root_layout, "Please Check Your Internet Connection", Snackbar.LENGTH_SHORT).show();
            recyclerView.setVisibility(View.GONE);
        }
        else if (Common.isNetworkConnected(ViewUploadsActivity.this)){
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void emptyList() {

        if (adapter.getItemCount() == 0) {

            default_image.setImageResource(R.drawable.ic_menu_gallery_white);

        } else {
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

                checkNetworkConnection();

                emptyList();

                loadUploads();

                swipeRefreshLayout.setRefreshing(false);


            }
        });

        //Default load for the first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                checkNetworkConnection();

                emptyList();

                loadUploads();

                swipeRefreshLayout.setRefreshing(false);


            }
        });


    }

    //************************************************Load Uploads*************************************

    private void loadUploads() {

        Query query = viewUploadsRef.orderByChild("userEmail").equalTo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        FirebaseRecyclerOptions<Wallpapers> options = new FirebaseRecyclerOptions.Builder<Wallpapers>()
                .setQuery(query,Wallpapers.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Wallpapers, ListWallpaperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ListWallpaperViewHolder viewholder,  int position, @NonNull final Wallpapers model) {

                Glide.with(ViewUploadsActivity.this)
                        .load(model.getImageLink())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                        .into(viewholder.wallpaper_image);

                viewholder.delte_recents.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeWallpaper(adapter.getRef(viewholder.getAdapterPosition()).getKey());
                    }
                });



                viewholder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {

                        Intent intent = new Intent(ViewUploadsActivity.this,ViewWallpaperActivity.class);
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
                        .inflate(R.layout.layout_view_uploads_recents_item,parent,false);

                int height = parent.getMeasuredHeight()/2;
                itemView.setMinimumHeight(height);

                emptyList();

                return new ListWallpaperViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }



    //************************************************Remove Wallpaper*************************************

    private void removeWallpaper(final String key) {

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AlertDialogStyle);
        progressDialog.setMessage("Removing Wallpaper...");
        progressDialog.show();

        viewUploadsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot post: dataSnapshot.getChildren()){

                    if (Objects.requireNonNull(post.getKey()).equals(key)){

                        wallpapers = post.getValue(Wallpapers.class);

                    }
                }
                //Remove from Room Database Recents
                deleteFromRecents(key);


                //Remove from firebase Database
                viewUploadsRef.child(key).removeValue();

                //Remove from Firebase Storage
                FirebaseStorage.getInstance().getReference().child("images/"+wallpapers.getStorageFile()).delete();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void deleteFromRecents(final String key) {

        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {

                RecentsDatabase recentsDatabase = new RecentsDatabase(
                        wallpapers.getImageLink(),
                        wallpapers.getCategoryId(),
                        String.valueOf(System.currentTimeMillis()),
                        key
                );
                recentRepositery.deleteRecents(recentsDatabase);
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
                }, new Action() {
                    @Override
                    public void run() {

                    }
                });
compositeDisposable.add(disposable);
    }


    //Closes Activity after taping back Arrow at top of toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            finish(); //Close activity when clicked back Arrow at top of toolbar
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null){
            adapter.startListening();
        }

    }
}
