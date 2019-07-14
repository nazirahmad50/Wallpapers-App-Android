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
import com.androidhive.ultrahdwallpapers.Database.FavouritesDatabase;
import com.androidhive.ultrahdwallpapers.Database.Repository.FavouritesRepositry;
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
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyFavouritesAdapter extends RecyclerView.Adapter<ListWallpaperViewHolder> {

    private final Context context;
    private final List<FavouritesDatabase> favouritesDatabases;

    //Used for Removing Favourites from Favourites Activity
    private final FavouritesRepositry favouritesRepositry;
    private final CompositeDisposable compositeDisposable;

    public MyFavouritesAdapter(Context context, List<FavouritesDatabase> favouritesDatabases, FavouritesRepositry favouritesRepositry, CompositeDisposable compositeDisposable) {
        this.context = context;
        this.favouritesDatabases = favouritesDatabases;
        this.favouritesRepositry = favouritesRepositry;
        this.compositeDisposable = compositeDisposable;


    }

    @NonNull
    @Override
    public ListWallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_favourites_item,parent,false);



        return new ListWallpaperViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListWallpaperViewHolder viewholder,  int position) {
        Glide.with(context)
                .load(favouritesDatabases.get(position).getImageLink())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder_loading_icon))
                .into(viewholder.wallpaper_image);

        //Remove Favourites from Room Database
        viewholder.remove_fav_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> e) {

                        FavouritesDatabase favouritesDatabase = new FavouritesDatabase(
                                favouritesDatabases.get(viewholder.getAdapterPosition()).getImageLink(),
                                favouritesDatabases.get(viewholder.getAdapterPosition()).getCategoryId(),
                                favouritesDatabases.get(viewholder.getAdapterPosition()).getKey()
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
                        }, new Action() {
                            @Override
                            public void run() {

                            }
                        });
                compositeDisposable.add(disposable);
            }
        });


        viewholder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {

                    Intent intent = new Intent(context, ViewWallpaperActivity.class);

                    Wallpapers wallpapers = new Wallpapers();
                    wallpapers.setCategoryId(favouritesDatabases.get(position).getCategoryId());
                    wallpapers.setImageLink(favouritesDatabases.get(position).getImageLink());
                    Common.SELECTED_BACKGROUND_KEY = favouritesDatabases.get(position).getKey();

                    Common.SELECTED_BACKGROUND = wallpapers;

                    context.startActivity(intent);

            }
        });
    }



    @Override
    public int getItemCount() {

        return favouritesDatabases.size();
    }

}
