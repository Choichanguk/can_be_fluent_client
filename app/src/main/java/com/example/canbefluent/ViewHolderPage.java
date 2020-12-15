package com.example.canbefluent;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.canbefluent.items.img_item;

public class ViewHolderPage extends RecyclerView.ViewHolder {

    private ImageView imageView;
    img_item data;
    public ViewHolderPage(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
    }
    public void onBind(img_item data, Context context){
        this.data = data;
        String url = MyApplication.server_url+ "/profile_img/" + data.getImage_name();
        Log.e("ViewHolderPage", "url: " + url);
        Glide.with(context)
                .load(url)
                .into(imageView);
//        imageView.setImageResource(data.getImg());
    }
}
