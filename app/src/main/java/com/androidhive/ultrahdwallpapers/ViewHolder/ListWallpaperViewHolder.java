package com.androidhive.ultrahdwallpapers.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.androidhive.ultrahdwallpapers.Interface.ItemClickListener;
import com.androidhive.ultrahdwallpapers.R;

public class ListWallpaperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


    public final ImageView wallpaper_image;
    public final ImageView remove_fav_image;
    public final ImageView delte_recents;


    private ItemClickListener itemClickListener;

    public ListWallpaperViewHolder(View itemView) {
        super(itemView);

        wallpaper_image = itemView.findViewById(R.id.wallpaper_image);
        remove_fav_image = itemView.findViewById(R.id.remove_fav_image);
        delte_recents = itemView.findViewById(R.id.delete_wallpaper);


        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v,getAdapterPosition());
    }


}
