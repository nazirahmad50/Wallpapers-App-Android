package com.androidhive.ultrahdwallpapers.Fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.androidhive.ultrahdwallpapers.Adapter.MyRecentsAdapter;
import com.androidhive.ultrahdwallpapers.Common.Common;
import com.androidhive.ultrahdwallpapers.Database.Databases;
import com.androidhive.ultrahdwallpapers.Database.Repository.RecentRepositery;
import com.androidhive.ultrahdwallpapers.Database.LocalDatabase.RecentsLocalDatabase;
import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;
import com.androidhive.ultrahdwallpapers.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class RecentsFragment extends Fragment {

    //ReyclerView
    private RecyclerView recyclerView;

    //RecycleView Adapter
    //We created seperate adapter class because we are not saving anything to firebase
    private MyRecentsAdapter adapter;


    private List<RecentsDatabase> recentsDatabaseList;


    //Room Database
    private CompositeDisposable compositeDisposable;
    private RecentRepositery recentRepositery;

    private RelativeLayout root_layout;

    //Swipe Refresh layout
    private SwipeRefreshLayout swipeRefreshLayout;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recents, container, false);


        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        root_layout = view.findViewById(R.id.root_layout);

        recyclerView = view.findViewById(R.id.recents_recyclerView);
        recyclerView.setHasFixedSize(true);

        //Init RoomDatabse
        compositeDisposable = new CompositeDisposable();
        Databases database = Databases.getLocalDatabaseInstance(getContext());
        recentRepositery = RecentRepositery.getInstance(RecentsLocalDatabase.getInstance(database.iRecentsLocalDatabase()));


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        //Recendatabase list
        recentsDatabaseList = new ArrayList<>();

        //Adapter
        adapter = new MyRecentsAdapter(getContext(), recentsDatabaseList, recentRepositery, new CompositeDisposable());


        recyclerView.setAdapter(adapter);


        //Swipe Refresh Layout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout();

        loadAllRecents();

        this.setHasOptionsMenu(true);

        return  view;
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
                loadAllRecents();

                swipeRefreshLayout.setRefreshing(false);


            }
        });

        //Default load for the first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

              checkNetworkConnection();

                loadAllRecents();

                swipeRefreshLayout.setRefreshing(false);


            }
        });


    }

    private void loadAllRecents() {

        Disposable disposable = recentRepositery.getAllRecents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<RecentsDatabase>>() {
                    @Override
                    public void accept(List<RecentsDatabase> recentsDatabases) {

                        recentsDatabaseList.clear();
                        recentsDatabaseList.addAll(recentsDatabases);
                        adapter.notifyDataSetChanged();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                        Log.e("Error",throwable.getMessage());

                    }
                });
        compositeDisposable.add(disposable);



    }



    //**********************************************************************Remove all Recents***********************************************************************
    //*************************************************************Close activity after back Arrow pressed************************************************************************

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_recents_menu_item, menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_delete_all){

            if (recentsDatabaseList.size() > 0) {

                Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> e) {

                        recentRepositery.deleteAllRecents();

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

            }else{
                Toast.makeText(getContext(), "Recents List Empty", Toast.LENGTH_SHORT).show();
            }

        }

        return false;
    }


    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }


}
