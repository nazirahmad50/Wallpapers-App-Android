package com.androidhive.ultrahdwallpapers.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.androidhive.ultrahdwallpapers.Common.Common;
import com.androidhive.ultrahdwallpapers.Interface.ItemClickListener;
import com.androidhive.ultrahdwallpapers.ListWallpaperActivity;
import com.androidhive.ultrahdwallpapers.Model.Category;
import com.androidhive.ultrahdwallpapers.R;
import com.androidhive.ultrahdwallpapers.ViewHolder.CategoryViewHolder;


public class CategoryFragment extends Fragment {

    private DatabaseReference categoryRef;

    //Firebase Adapter
    private FirebaseRecyclerAdapter<Category,CategoryViewHolder> adapter;

    //RecyclerView
    private RecyclerView recyclerView;

    //Swipe Refresh layout
    private SwipeRefreshLayout swipeRefreshLayout;

    private RelativeLayout rootLayout;


    //Interestial Ad
    private InterstitialAd interstitialAd;

    private int adCounter = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);



        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);



        rootLayout = view.findViewById(R.id.root_layout);

        //Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        categoryRef = firebaseDatabase.getReference(Common.CATEGORY_BACKGROUND);

        //RecyclerView
        recyclerView = view.findViewById(R.id.category_recyclerView);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(gridLayoutManager);



        //Swipe Refresh Layout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout("");

        //load all categories by default
        loadCategory("");

        this.setHasOptionsMenu(true);

        return view;
    }

    private void checkNetworkConnection(){
        //Check Internet Connection
        if (!Common.isNetworkConnected(requireActivity())){
            Snackbar.make(rootLayout, "Please Check Your Internet Connection", Snackbar.LENGTH_SHORT).show();
            recyclerView.setVisibility(View.GONE);
        }
        else if (Common.isNetworkConnected(requireActivity())){
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    //********************************************Refresh Layout****************************

    private void swipeRefreshLayout(final String search) {

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_blue_bright);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                    checkNetworkConnection();

                    loadCategory(search);

                swipeRefreshLayout.setRefreshing(false);


            }
        });

        //Default load for the first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                    checkNetworkConnection();
                    loadCategory(search);

                swipeRefreshLayout.setRefreshing(false);


            }
        });


    }

    private void adIntent(){
        interstitialAd = new InterstitialAd(requireActivity());
        interstitialAd.setAdUnitId("/6499/example/interstitial");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                if (adCounter <= 4){
                    adCounter++;
                    Intent intent = new Intent(requireActivity(), ListWallpaperActivity.class);
                    startActivity(intent);

                }else{
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();

                        adCounter = 0;
                        Intent intent = new Intent(getActivity(), ListWallpaperActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Intent intent = new Intent(getActivity(), ListWallpaperActivity.class);
                startActivity(intent);
            }
        });
    }

    //********************************************Load Category****************************

    private void loadCategory(String search) {

        Query query;

        if (search != null) {
             query = categoryRef.orderByChild("name").startAt(search).endAt(search + "\uf8ff");
        }else{
             query = categoryRef;
        }

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
            .setQuery(query, Category.class)
            .build();


        adapter = new FirebaseRecyclerAdapter<Category, CategoryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CategoryViewHolder viewholder, int position, @NonNull final Category model) {



                Glide.with(requireContext())
                        .load(model.getImageLink())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                        .into(viewholder.category_image);


                viewholder.category_name.setText(model.getName());

                viewholder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, final int position) {



                        //We cant use intent extra for these
                        //because we have to keep changing these values they have to be called in more than one activity
                        Common.CATEGORY_ID_SELECTED = adapter.getRef(position).getKey();
                        Common.CATEGORY_NAME_SELECTED = model.getName();
                                                            Intent intent = new Intent(getActivity(), ListWallpaperActivity.class);
                                    startActivity(intent);



                    }
                });
            }

            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_category_item,parent,false);

                return new CategoryViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    //*************************************************************Search Category View***************************************************


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.fragment_category_search_item, menu);

        final MenuItem searchMenuItem = menu.findItem( R.id.search_wallpapers);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();


        //Change SearchView text color to white
        EditText searchEditText =  searchView.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                loadCategory(s.toUpperCase());
                swipeRefreshLayout(s.toUpperCase());

                return false;
            }
        });

    }



    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null){
            adapter.startListening();
        }
    }

}
