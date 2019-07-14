package com.androidhive.ultrahdwallpapers.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidhive.ultrahdwallpapers.Interface.ItemClickListener;
import com.androidhive.ultrahdwallpapers.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public final TextView category_name;
    public final ImageView category_image;

    private ItemClickListener itemClickListener;


    public CategoryViewHolder(View itemView) {
        super(itemView);

        category_name = itemView.findViewById(R.id.category_name);
        category_image = itemView.findViewById(R.id.category_image);

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
