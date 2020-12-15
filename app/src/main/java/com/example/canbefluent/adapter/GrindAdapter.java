package com.example.canbefluent.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.GridItemView;
import com.example.canbefluent.MyApplication;
import com.example.canbefluent.R;
import com.example.canbefluent.items.img_item;

import java.util.ArrayList;

public class GrindAdapter extends BaseAdapter {

    ArrayList<img_item> img_items = new ArrayList<img_item>();
    Context mContext = null;
    String url = MyApplication.server_url + "/profile_img/";


    public GrindAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return img_items.size();
    }

    public void addItem(img_item item) {
        img_items.add(item);
    }

    @Override
    public Object getItem(int position) {
        return img_items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        GridItemView view = null;
        if (convertView == null) {
            view = new GridItemView(mContext);
        } else {
            view = (GridItemView) convertView;
        }

        String img_url = url + img_items.get(position).getImage_name();



        if(img_items.get(position).getIs_first().equals("T")){
            Log.e("어댑터", "대표 이미지");
            view.setBackground();
        }
        view.setImage(img_url);

        return view;
    }
}
