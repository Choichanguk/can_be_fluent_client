package com.example.canbefluent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.canbefluent.utils.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.items.img_item;

import java.util.ArrayList;

public class viewpager_adapter extends PagerAdapter {
    private LayoutInflater inflater;
    private Context mContext;
    private ArrayList<img_item> list;

    /**
     * 생성자
     */
    public viewpager_adapter(Context mContext, ArrayList<img_item> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (View) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.image_view, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);
        String url = MyApplication.server_url+ "/images/" + list.get(position).getImage_name();
        Glide.with(mContext)
                .load(url)
                .into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.invalidate();
    }
}
