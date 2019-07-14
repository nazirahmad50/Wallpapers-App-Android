package com.androidhive.ultrahdwallpapers.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import com.androidhive.ultrahdwallpapers.R;
import com.androidhive.ultrahdwallpapers.ViewHolder.ListWallpaperViewHolder;
import com.androidhive.ultrahdwallpapers.ViewWallpaperActivity;


public class TrendingFragment extends Fragment  {

    //RecyclerView
    private RecyclerView recyclerView;

    private DatabaseReference wallpaperRef;

    //RecyclerView Adapter
    private FirebaseRecyclerAdapter<Wallpapers, ListWallpaperViewHolder> adapter;

    private RelativeLayout root_layout;

    //Swipe Refresh layout
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);


        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        root_layout = view.findViewById(R.id.root_layout);

        //Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        wallpaperRef = firebaseDatabase.getReference(Common.WALLPAPER_LIST);

        //ReyclerView
        recyclerView = view.findViewById(R.id.trending_recyclerView);
        recyclerView.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //Because Firebase returns ascending sort list so we need to reverse recyclerView to show largest item first in the list
        recyclerView.setLayoutManager(linearLayoutManager);

        //Swipe Refresh Layout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout("viewCount");

        //Load Most Viewed as default
        loadTrending("viewCount");

        this.setHasOptionsMenu(true);

        return view;
    }

    private void checkNetworkConnection(){
        //Check Internet Connection
        if (!Common.isNetworkConnected(requireActivity())){
            Snackbar.make(root_layout, "Please Check Your Internet Connection", Snackbar.LENGTH_SHORT).show();
            recyclerView.setVisibility(View.GONE);
        }
        else if (Common.isNetworkConnected(requireActivity())){
            recyclerView.setVisibility(View.VISIBLE);
        }
    }




    //************************************************************Refresh Layout************************************************************************

    private void swipeRefreshLayout(final String search) {

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_blue_bright);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                checkNetworkConnection();
                loadTrending(search);
                swipeRefreshLayout.setRefreshing(false);


            }
        });

        //Default load for the first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                checkNetworkConnection();

                loadTrending(search);
                swipeRefreshLayout.setRefreshing(false);


            }
        });


    }


    //**********************************************************************Most Viewed/Downloaded/Favourited Menu Options************************************************************************

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_trending_menu_item, menu);



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){

            case R.id.action_viewed:
                swipeRefreshLayout.setRefreshing(true);
                loadTrending("viewCount");
                swipeRefreshLayout("viewCount");
                break;

            case R.id.action_downloaded:
                swipeRefreshLayout.setRefreshing(true);
                loadTrending("downloadCount");
                swipeRefreshLayout("downloadCount");

                break;
        }


        return false;
    }

    //************************************************************Load Trending************************************************************************

    private void loadTrending(String menuOptionSelected) {

        Query query = wallpaperRef.orderByChild(menuOptionSelected) //Default is acccending order
                //the 'limitToLast' allows the firebase to orderByChild in decending order
                .limitToLast(50); //get 50 items with biggest view count

        final FirebaseRecyclerOptions<Wallpapers> options = new FirebaseRecyclerOptions.Builder<Wallpapers>()
                .setQuery(query, Wallpapers.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Wallpapers, ListWallpaperViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ListWallpaperViewHolder viewholder, int position, @NonNull final Wallpapers model) {

                Glide.with(requireContext())
                        .load(model.getImageLink())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                        .into(viewholder.wallpaper_image);



                viewholder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, final int position) {

                        Intent intent = new Intent(getActivity(), ViewWallpaperActivity.class);
                        Common.SELECTED_BACKGROUND = model;
                        Common.SELECTED_BACKGROUND_KEY = adapter.getRef(position).getKey();

                        //Loads the Category name based on the wallpaper clicked
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
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }

    }

}


