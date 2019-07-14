package com.androidhive.ultrahdwallpapers.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.androidhive.ultrahdwallpapers.Common.Common;
import com.androidhive.ultrahdwallpapers.Database.RecentsDatabase;
import com.androidhive.ultrahdwallpapers.Database.Repository.RecentRepositery;
import com.androidhive.ultrahdwallpapers.Interface.ItemClickListener;
import com.androidhive.ultrahdwallpapers.Model.Wallpapers;
import com.androidhive.ultrahdwallpapers.R;
import com.androidhive.ultrahdwallpapers.ViewHolder.ListWallpaperViewHolder;
import com.androidhive.ultrahdwallpapers.ViewWallpaperActivity;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyRecentsAdapter extends RecyclerView.Adapter<ListWallpaperViewHolder> {

    private final Context context;
    private final List<RecentsDatabase> recentsDatabases;

    private String selectedBackgroudnName;

    private final CompositeDisposable compositeDisposable;
    private final RecentRepositery recentRepositery;

    public MyRecentsAdapter(Context context, List<RecentsDatabase> recentsDatabases, RecentRepositery recentRepositery, CompositeDisposable compositeDisposable) {
        this.context = context;
        this.recentsDatabases = recentsDatabases;
        this.recentRepositery = recentRepositery;
        this.compositeDisposable = compositeDisposable;
    }

    @NonNull
    @Override
    public ListWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_view_uploads_recents_item,parent,false);



        return new ListWallpaperViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListWallpaperViewHolder viewholder,  int position) {

        Glide.with(context)
                .load(recentsDatabases.get(position).getImageLink())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                .into(viewholder.wallpaper_image);

        viewholder.delte_recents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteFromRecents(viewholder.getAdapterPosition());


            }
        });




        viewholder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {

                Intent intent = new Intent(context, ViewWallpaperActivity.class);


                Wallpapers wallpapers = new Wallpapers();
                wallpapers.setCategoryId(recentsDatabases.get(position).getCategoryId());
                wallpapers.setImageLink(recentsDatabases.get(position).getImageLink());
                wallpapers.setName(selectedBackgroudnName);


                Common.SELECTED_BACKGROUND = wallpapers;

                //Set the 'SELECTED_BACKGROUND_KEY' to the postion clicked on in the Recents Fragment
                Common.SELECTED_BACKGROUND_KEY = recentsDatabases.get(position).getKey();


                context.startActivity(intent);
            }
        });



    }




    private void deleteFromRecents(final int position) {

        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {

                RecentsDatabase recentsDatabase = new RecentsDatabase(
                        recentsDatabases.get(position).getImageLink(),
                        recentsDatabases.get(position).getCategoryId(),
                        String.valueOf(System.currentTimeMillis()),
                        recentsDatabases.get(position).getKey()
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

                });

        compositeDisposable.add(disposable);

    }

//    private void getSelectedBackgroundName(String key) {
//
//        FirebaseDatabase.getInstance()
//                .getReference(Common.WALLPAPER_LIST)
//                .child(key)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        Common.SELECTED_BACKGROUND = dataSnapshot.getValue(Wallpapers.class);
//
//                        selectedBackgroudnName = Common.SELECTED_BACKGROUND.getName();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//    }




    @Override
    public int getItemCount() {
        return recentsDatabases.size();
    }



}
